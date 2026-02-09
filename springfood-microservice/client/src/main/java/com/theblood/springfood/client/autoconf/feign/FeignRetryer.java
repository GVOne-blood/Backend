package com.theblood.springfood.client.autoconf.feign;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom retryer for Feign clients with exponential backoff
 */
public class FeignRetryer implements Retryer {

    private static final Logger logger = LoggerFactory.getLogger(FeignRetryer.class);

    private final long period;
    private final long maxPeriod;
    private final int maxAttempts;
    private int attempt;
    private long nextInterval;

    public FeignRetryer(long period, long maxPeriod, int maxAttempts) {
        this.period = period;
        this.maxPeriod = maxPeriod;
        this.maxAttempts = maxAttempts;
        this.attempt = 1;
        this.nextInterval = period;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            logger.warn("Max retry attempts ({}) reached for request", maxAttempts);
            throw e;
        }

        logger.debug("Retrying request, attempt {} of {}", attempt, maxAttempts);

        try {
            Thread.sleep(nextInterval);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            throw e;
        }

        // Exponential backoff
        nextInterval = Math.min(nextInterval * 2, maxPeriod);
    }

    @Override
    public Retryer clone() {
        return new FeignRetryer(period, maxPeriod, maxAttempts);
    }

    /**
     * No retry implementation
     */
    public static class NoRetry implements Retryer {
        @Override
        public void continueOrPropagate(RetryableException e) {
            throw e;
        }

        @Override
        public Retryer clone() {
            return this;
        }
    }
}
