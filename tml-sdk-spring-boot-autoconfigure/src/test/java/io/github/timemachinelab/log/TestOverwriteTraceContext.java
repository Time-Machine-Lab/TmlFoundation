package io.github.timemachinelab.log;

import io.github.timemachinelab.log.context.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author glser
 * @since 2026/1/18
 */
@SpringBootTest
@Slf4j
public class TestOverwriteTraceContext {

    @Test
    public void testOverwriteTraceContext() {
        TraceContext.Holder.set(new TestTraceContext());

        TraceContext traceContext = TraceContext.Holder.get();

        log.info(traceContext.generateTraceId());
    }
}
