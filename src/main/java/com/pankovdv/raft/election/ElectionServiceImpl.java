package com.pankovdv.raft.election;

import com.pankovdv.raft.context.Context;
import com.pankovdv.raft.election.dto.ElectionRequestDto;
import com.pankovdv.raft.election.dto.ElectionResponseDto;
import com.pankovdv.raft.journal.operation.OperationsLog;
import com.pankovdv.raft.network.NetworkProperties;
import com.pankovdv.raft.network.RestService;
import com.pankovdv.raft.node.State;
import com.pankovdv.raft.node.neighbour.Neighbour;
import com.pankovdv.raft.node.neighbour.NeighbourService;
import com.pankovdv.raft.node.neighbour.Neighbours;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElectionServiceImpl implements ElectionService {

    @Autowired
    private Context context;
    @Autowired
    private RestService restService;
    @Autowired
    private NetworkProperties networkProperties;
    @Autowired
    private OperationsLog journal;
    @Autowired
    private NeighbourService neighbourService;

    @Override
    public void processElection() {
        Integer candidateId = context.getId();
        log.info("Node #{} Start election", candidateId);
        context.setState(State.CANDIDATE);
        Long term = context.getTermService().incCurrentTerm(candidateId);
        context.setVotedFor(candidateId);

        Neighbours neighbours = neighbourService.getNeighbours();

        Boolean win = processElection(neighbours, term);
        if (win) {
            log.info("Node #{} I have WIN the election!", context.getId());
            context.setState(State.LEADER);
        } else {
            log.info("Node #{} I have LOSE the election!", context.getId());
            context.setState(State.FOLLOWER);
        }
    }

    private Boolean processElection(Neighbours neighbours, Long term) {
        long voteGrantedCount = 1L;
        long voteRevokedCount = 0L;

        while (checkCurrentElectionStatus()) {
            List<ElectionResponseDto> votes = getVoteFromAllNeighbours(neighbours.getNeighbours(), term);

            for (ElectionResponseDto resp : votes) {
                if (resp == null) {
                    break;
                }
                if (resp.getTerm() > context.getTermService().getCurrentTerm()) {
                    context.setTermGreaterThenCurrent(resp.getTerm());
                    return false;
                }
                if (resp.isVoteGranted()) {
                    log.info("Node #{} Vote granted from {}", context.getId(), resp.getId());
                    neighbours.get(resp.getId()).setVoteGranted(new AtomicBoolean(true));
                    voteGrantedCount++;
                } else {
                    log.info("Node #{} Vote revoked from {}", context.getId(), resp.getId());
                    voteRevokedCount++;
                }
            }
            if (voteGrantedCount >= neighbours.getQuorum()) {
                return true;
            }
            if (voteRevokedCount >= neighbours.getQuorum()) {
                return false;
            }
            delay();
        }
        return false;
    }

    private boolean checkCurrentElectionStatus() {
        //return term.equals(context.getCurrentJournalTerm()) && context.getState().equals(State.CANDIDATE);
        return context.getState().equals(State.CANDIDATE);
    }

    private List<ElectionResponseDto> getVoteFromAllNeighbours(List<Neighbour> neighbours, Long term) {
        log.debug("Node #{} Forward vote request to Neighbours. Term {}. Neighbours count: {}", context.getId(), term, neighbours.size());

        List<CompletableFuture<ElectionResponseDto>> answerFutureList =
                neighbours.stream().map(neighbour -> getVoteFromOneNeighbour(neighbour, term)).toList();

        return CompletableFuture
                .allOf(answerFutureList.toArray(new CompletableFuture[0]))
                .thenApply(v -> answerFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                .join();
    }

    private CompletableFuture<ElectionResponseDto> getVoteFromOneNeighbour(Neighbour neighbour, Long term) {
        return CompletableFuture.supplyAsync(() -> {

            log.info("Node #{} Send vote request to #{}", context.getId(), neighbour.getId());

            ElectionRequestDto request = ElectionRequestDto
                    .builder()
                    .term(term)
                    .candidateId(context.getId())
                    .lastLogIndex(journal.getLastIndex())
                    .lastLogTerm(journal.getLastTerm())
                    .build();

            ResponseEntity<ElectionResponseDto> response = restService.sendPostRequest(
                    restService.buildUrl(neighbour.getPort(), "/api/v1/vote-for-me"),
                    request,
                    ElectionResponseDto.class
            );

            if (response == null) {
                neighbourService.deleteNeighbour(neighbour);
                return null;
            }

            return response.getBody();
        });
    }

    private void delay() {
        try {
            log.info("Node #{} Preparing to retry vote request", context.getId());
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public ElectionResponseDto vote(ElectionRequestDto request) {
        log.info("Node #{} Get vote request from {} with term {}. Current term: {}. Voted for: {}",
                context.getId(),
                request.getCandidateId(),
                request.getTerm(),
                context.getTermService().getCurrentTerm(),
                context.getVotedFor());

        boolean termCheck = false;
        if (request.getTerm() < context.getTermService().getCurrentTerm()) {
            return ElectionResponseDto.builder().voteGranted(false).id(context.getId()).term(context.getTermService().getCurrentTerm()).build();
        }

        if (request.getTerm().equals(context.getTermService().getCurrentTerm())) {
            termCheck = (context.getVotedFor() == null || context.getVotedFor().equals(request.getCandidateId()));
        }
        if (request.getTerm() > context.getTermService().getCurrentTerm()) {
            termCheck = true;
            context.setTermGreaterThenCurrent(request.getTerm());
        }

        boolean logCheck = !((journal.getLastTerm() > request.getLastLogTerm()) ||
                ((journal.getLastTerm().equals(request.getLastLogTerm())) && (journal.getLastIndex() > request.getLastLogIndex())));


        boolean voteGranted = termCheck && logCheck;

        if (voteGranted) {
            context.setVotedFor(request.getCandidateId());
            log.info("Node #{} Give vote for {}", context.getId(), request.getCandidateId());
        } else {
            log.info("Node #{} Reject vote for {}", context.getId(), request.getCandidateId());
        }
        return ElectionResponseDto.builder().voteGranted(voteGranted).id(context.getId()).term(context.getTermService().getCurrentTerm()).build();
    }
}