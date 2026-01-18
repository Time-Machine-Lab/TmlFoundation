package io.github.timemachinelab.log;

import io.github.timemachinelab.log.context.DefaultTraceContext;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author glser
 * @since 2026/1/18
 */
@SpringBootTest
@Slf4j
@ActiveProfiles("log-test")
public class TestOverwriteTmlLogTraceContext {

    @Test
    public void testOverwriteTraceContext() {
        TmlLogTraceContext.Holder.set(new DefaultTraceContext());

        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();

        log.info(tmlLogTraceContext.generateTraceId());

        tmlLogTraceContext.set("testKey", "测试json能不能自动输出");

        log.info("处理支付订单");
    }
}
