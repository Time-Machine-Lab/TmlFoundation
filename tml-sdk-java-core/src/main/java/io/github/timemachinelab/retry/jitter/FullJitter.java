package io.github.timemachinelab.retry.jitter;

import java.util.concurrent.ThreadLocalRandom;

public class FullJitter implements JitterAlgorithm {

    private ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public long calculateDelay(long baseDelay,long previousDelay) {
        return random.nextLong(0,baseDelay+1);

    }
}
