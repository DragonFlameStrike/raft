package com.pankovdv.raft;

import com.pankovdv.raft.config.RaftProperties;
import com.pankovdv.raft.context.Context;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public abstract class CustomTimer {

    @Autowired
    protected Context context;

    @Autowired
    protected RaftProperties properties;

    private final Timer timer = new Timer();

    abstract protected Integer getTimeout();

    abstract protected String getActionName();

    abstract protected Runnable getAction();

    abstract protected boolean isRun();

    @Getter
    private final AtomicInteger counter = new AtomicInteger(0);


    public void reset() {
        counter.set(0);
    }


    @PostConstruct
    private void start() {

        timer.schedule(new TimerTask() {
            @Override

            public void run() {
                if (isRun()) {
                    counter.incrementAndGet();
                    if (counter.get() >= getTimeout()) {
                        log.info("");
                        log.info("");
                        log.debug("Node #{} Time to next {}: {} sec", context.getId(), getActionName(), getTimeout() - counter.get());
                        counter.set(0);
                        getAction().run();
                    }
                } else
                    counter.set(0);
            }
        }, 0, 1000);

    }


}
