package io.github.timemachinelab.log.mdc;

import org.slf4j.spi.MDCAdapter;
import org.springframework.core.Ordered;

/**
 * mdcAdapter 选择器接口
 *
 * @author glser
 * @since 2026/1/17
 */
public interface TmlLogMdc extends Ordered {

    String name();

    MDCAdapter adapter();

    default boolean isSupported() {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
