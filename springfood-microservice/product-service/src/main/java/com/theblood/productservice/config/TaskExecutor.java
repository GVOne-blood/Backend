package com.theblood.productservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Slf4j
public class TaskExecutor {
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Long maxTimeout = 10000L;

    public void execute(List<Runnable> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            log.info(" list tasks is empty");
            return;
        }

        Long start = System.currentTimeMillis();
        List<Future<Runnable>> results = new ArrayList<>();

        for (Runnable task : tasks) {
            Future f = executorService.submit(task);
            results.add(f);
        }

        while (true) {
            boolean finishAllTask = true;
            for (Future f : results) {
                finishAllTask = finishAllTask && f.isDone();
            }
            if (System.currentTimeMillis() - start > maxTimeout) {
                log.info(" reach max timeout");
                finishAllTask = true;
            }
            if (finishAllTask) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (ConcurrencyFailureException e) {
                log.error("Race condition between " + e.getCause());
                log.error(e.getMessage());
            } catch (InterruptedException e) {
                log.error("Thread is interrupted" + e.getMessage());
            }
        }
        log.info(" finish all task in {} ms", System.currentTimeMillis() - start);
    }
}
