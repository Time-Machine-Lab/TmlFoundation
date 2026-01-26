package io.github.timemachinelab.retry.backoff;

public interface BackoffStrategy {

    long calculateDelay(int attempts);

}
