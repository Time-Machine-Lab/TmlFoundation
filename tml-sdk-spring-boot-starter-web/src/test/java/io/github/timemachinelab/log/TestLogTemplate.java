package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLogProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test-log")
@Slf4j
public class TestLogTemplate {

    @Autowired
    private TmlLogProperties logProperties;

    @Test
    void testLogProperties() {
        assertEquals("./app/logs", logProperties.getPath());
        assertEquals("testLog", logProperties.getFileName());
        assertEquals("INFO", logProperties.getLevel());
        assertEquals("prod", logProperties.getEnv());
    }

    @Test
    void testLog() {
        log.info("info test");
        log.warn("warn test");
        log.error("error test");
    }
}
