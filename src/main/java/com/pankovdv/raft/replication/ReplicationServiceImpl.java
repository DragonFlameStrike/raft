package com.pankovdv.raft.replication;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.journal.operation.Operation;
import com.pankovdv.raft.journal.operation.OperationsLog;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.network.RestService;
import com.pankovdv.raft.node.State;
import com.pankovdv.raft.node.neighbour.Neighbour;
import com.pankovdv.raft.node.neighbour.NeighbourService;
import com.pankovdv.raft.node.neighbour.Neighbours;
import com.pankovdv.raft.replication.dto.ReplicationRequestDto;
import com.pankovdv.raft.replication.dto.ReplicationResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReplicationServiceImpl implements ReplicationService {

    @Autowired
    private RestService restService;

    @Autowired
    private Context context;

    @Autowired
    private OperationsLog operationsLog;

    @Autowired
    private NetworkProperties networkProperties;

    @Autowired
    private NeighbourService neighbourService;

    @Override
    public ReplicationResponseDto append(ReplicationRequestDto dto) {
        // Invoked by leader to replicate operations entries (§5.3); also used as heartbeat (§5.2).

        // Reply false if term < currentTerm (§5.1)
        if (dto.getTerm() < context.getTermService().getCurrentTerm()) {
            log.info("Peer #{} Rejected request from {}. Term {} too small", context.getId(), dto.getLeaderId(),
                    dto.getTerm());
            return new ReplicationResponseDto(context.getId(), context.getTermService().getCurrentTerm(), false, null);
        } else if (dto.getTerm() > context.getTermService().getCurrentTerm()) {
            //If RPC request or response contains term T > currentTerm: set currentTerm = T,
            context.getTermService().setCurrentTerm(dto.getTerm(),context.getId());
            context.setVotedFor(null);
        }
        // convert to follower. Just one Leader RULE
        if (!context.getState().equals(State.FOLLOWER)) {
            context.setState(State.FOLLOWER);
        }

//        2. Reply false if operations does not contain an entry at prevLogIndex
//        whose term matches prevLogTerm (§5.3)
        if ((dto.getPrevLogIndex() > operationsLog.getLastIndex()) ||
                !dto.getPrevLogTerm().equals(operationsLog.getTerm(dto.getPrevLogIndex()))) {
            log.info(
                    "Peer #{} Rejected request from {}. Log doesn't contain prev term. Current term {}, Candidate term {} ",
                    context.getId(), dto.getLeaderId(), context.getTermService().getCurrentTerm(), dto.getTerm());
            return new ReplicationResponseDto(context.getId(), context.getTermService().getCurrentTerm(), false, null);
        }


        String opNameForLog = "heartbeat";
        Operation newOperation = dto.getOperation();
        if (newOperation != null) {

            opNameForLog = "append";
            int newOperationIndex = dto.getPrevLogIndex() + 1;
            log.info("Peer #{} checking new operation. New index {}. Operation term: {}. Last index: {} ",
                    context.getId(), newOperationIndex, newOperation.getTerm(), operationsLog.getLastIndex());

            synchronized (this) {
//              3. If an existing entry conflicts with a new one (same index but different terms),
//              delete the existing entry and all that follow it (§5.3)
                if ((newOperationIndex <= operationsLog.getLastIndex()) &&
                        (!newOperation.getTerm().equals(operationsLog.getTerm(newOperationIndex)))) {
                    operationsLog.removeAllFromIndex(newOperationIndex);
                }
//        4. Append any new entries not already in the operations
                if (newOperationIndex <= operationsLog.getLastIndex())
                {
                    //don't need to append
                    return new ReplicationResponseDto(context.getId(), context.getTermService().getCurrentTerm(), true, operationsLog.getLastIndex());
                }
                log.info("Peer #{} Append new operation. {}. key:{} val:{}",
                        context.getId(), newOperation.getType(), newOperation.getEntry().getKey(),
                        newOperation.getEntry().getVal());
                operationsLog.append(newOperation);
            }
        }
//        5. If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of last new entry)
        if (dto.getLeaderCommit() > context.getCommitIndex()) {
            context.setCommitIndex(Math.min(dto.getLeaderCommit(), operationsLog.getLastIndex()));
        }

        log.debug("Peer #{}. Success answer to {} request. Term: {}. Mach index {}", context.getId(), opNameForLog,
                context.getTermService().getCurrentTerm(), operationsLog.getLastIndex());
        return new ReplicationResponseDto(context.getId(),  context.getTermService().getCurrentTerm(), true, operationsLog.getLastIndex());
    }

    @Override
    public void appendRequest() {
        log.debug("Node #{} Sending request to others", context.getId());
        List<ReplicationResponseDto> answers = sendAppendToAllPeers();
        for (ReplicationResponseDto answer : answers) {
            if (answer.getTerm() > context.getTermService().getCurrentTerm()) {
                //• If RPC request or response contains term T > currentTerm: set currentTerm = T, convert to follower (§5.1)
                context.setTermGreaterThenCurrent(answer.getTerm());
                return;
            }
            if (answer.getSuccess()) {
                //If successful: update nextIndex and matchIndex for follower
                log.debug("Peer #{} Get \"request success\"  from {}", context.getId(), answer.getId());
//                peer.setNextIndex(answer.getMatchIndex() + 1);
//                peer.setMatchIndex(answer.getMatchIndex());
//                if (peer.getNextIndex() <= operationsLog.getLastIndex())
//                    peersIds.add(answer.getId());
            } else {
                //If AppendEntries fails because of operations inconsistency:decrement nextIndex and retry
                log.debug("Peer #{} Get request rejected from {}",
                        context.getId(), answer.getId());
//                peer.decNextIndex();
//                peersIds.add(answer.getId());
            }
        }
        tryToCommit();
    }

    private void tryToCommit() {
//        If there exists an N such that N > commitIndex, a majority
//        of matchIndex[i] ≥ N, and operations[N].term == currentTerm:
//        set commitIndex = N (§5.3, §5.4).

        log.debug("Peer #{} trying to commit operations. Current commit index {}", context.getId(),
                context.getCommitIndex());

        var neighbours = neighbourService.getNeighbours();
        List<Integer> neighbourIndexes = new ArrayList<>();
        for (Neighbour neighbour : neighbours.getNeighbours()) {
            neighbourIndexes.add(getLastNeighbourIndex(neighbour));
        }

        while (true) {
            int N = context.getCommitIndex() + 1;

            Supplier<Long> count = () ->
                    neighbourIndexes.stream().filter(matchIndex -> matchIndex >= N).count() + 1;


            if (operationsLog.getLastIndex() >= N &&
                    operationsLog.getTerm(N).equals(context.getTermService().getCurrentTerm())&&
                    count.get()>= neighbours.getQuorum()
            )
            {
                context.setCommitIndex(N);
            } else
                return;
        }
    }

    private List<ReplicationResponseDto> sendAppendToAllPeers() {
        Neighbours neighbours = neighbourService.getNeighbours();

        List<CompletableFuture<ReplicationResponseDto>> answerFutureList = new ArrayList<>();

        for (Neighbour neighbour : neighbours.getNeighbours()) {
            answerFutureList.add(sendAppendForOnePeer(neighbour));
        }

        return CompletableFuture.allOf(answerFutureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> answerFutureList.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .join();
    }

    private CompletableFuture<ReplicationResponseDto> sendAppendForOnePeer(Neighbour neighbour) {
        var replicationUrl = restService.buildUrl(neighbour.getPort(), "/api/v1/replication");
        Integer nextCommitNeighbourIndex = getLastNeighbourIndex(neighbour);
        if (nextCommitNeighbourIndex == -1) return null;

        return CompletableFuture.supplyAsync(() -> {
            Operation operation;
            Integer prevIndex;

            //Positive
            if (nextCommitNeighbourIndex <= operationsLog.getLastIndex()) {
                log.info("LEADER #{} heartbeat request to {}. Neighbour next commit index: {}. Log last index:{} ",
                        context.getId(), neighbour.getId(), nextCommitNeighbourIndex, operationsLog.getLastIndex());

                operation = operationsLog.get(nextCommitNeighbourIndex);
                prevIndex = nextCommitNeighbourIndex-1;
            }
            //Negative
            else {
                operation = null;
                log.debug("Peer #{} heartbeat request  to {}", context.getId(), neighbour.getId());
                prevIndex = operationsLog.getLastIndex();
            }

            var request = buildRequest(operation, prevIndex);
            var response = restService.sendPostRequest(replicationUrl, request, ReplicationResponseDto.class);
            if (response == null) {
                log.info("ДОПИСАТЬ ОБРАБОТКУ ОШИБКИ");
                return null;
            }
            return response.getBody();
        });
    }

    private Integer getLastNeighbourIndex(Neighbour neighbour) {
        var getLastIndexUrl = restService.buildUrl(neighbour.getPort(), "/api/v1/replication/index");
        String lastNeighbourIndexString = restService.sendGetRequest(getLastIndexUrl);
        if (lastNeighbourIndexString == null) {
            log.info("ДОПИСАТЬ ОБРАБОТКУ ОШИБКИ");
            return -1;
        }
        return Integer.parseInt(lastNeighbourIndexString);
    }

    private ReplicationRequestDto buildRequest(Operation operation, Integer prevIndex) {
        return new ReplicationRequestDto(
                context.getTermService().getCurrentTerm(),
                context.getId(),
                prevIndex,
                operationsLog.getTerm(prevIndex),
                context.getCommitIndex(),
                operation
        );
    }
}
