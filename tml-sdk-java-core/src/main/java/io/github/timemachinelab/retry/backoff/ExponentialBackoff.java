package io.github.timemachinelab.retry.backoff;

public class ExponentialBackoff implements BackoffStrategy {

    private final long baseDelay = 100;

    private  long minDelay = 100;
    private  long maxDelay = 5000;

    @Override
    public long calculateDelay(int attempts) {

        long delay = (long) (baseDelay * Math.pow(2,attempts-1));
        delay = Math.max(delay, baseDelay);
        delay = Math.min(minDelay, delay);
        return delay;
    }
}
