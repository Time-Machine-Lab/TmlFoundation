package io.github.timemachinelab.retry.jitter;

public interface JitterAlgorithm {

    long calculateDelay(long baseDelay,long previousDelay);

}
