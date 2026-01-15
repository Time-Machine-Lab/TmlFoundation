package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLog;
import io.github.timemachinelab.log.interceptor.TraceIdWebFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-trace")
@Slf4j
public class TraceIdFilterTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        // 手动构建 MockMvc 并添加 TraceIdWebFilter
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilter(new TraceIdWebFilter())
                .build();
    }

    /**
     * 测试自动生成 traceId
     */
    @Test
    void testAutoGenerateTraceId() throws Exception {
        MvcResult result = mockMvc.perform(get("/trace/test"))
                .andExpect(status().isOk())
                .andExpect(header().exists(TmlLog.TRACE_ID_HEADER))
                .andReturn();

        String traceId = result.getResponse().getHeader(TmlLog.TRACE_ID_HEADER);
        assertNotNull(traceId);
        // UUID 去掉横线后是 32 位
        assertEquals(32, traceId.length());
        log.info("自动生成的 traceId: {}", traceId);
    }

    /**
     * 测试从请求头传递 traceId
     */
    @Test
    void testPassTraceIdFromHeader() throws Exception {
        String customTraceId = "custom-trace-id-12345";

        MvcResult result = mockMvc.perform(get("/trace/test")
                        .header(TmlLog.TRACE_ID_HEADER, customTraceId))
                .andExpect(status().isOk())
                .andExpect(header().string(TmlLog.TRACE_ID_HEADER, customTraceId))
                .andReturn();

        log.info("传递的 traceId: {}", customTraceId);
    }

    /**
     * 测试请求结束后 MDC 被清理
     */
    @Test
    void testMdcCleanedAfterRequest() throws Exception {
        mockMvc.perform(get("/trace/test"))
                .andExpect(status().isOk());

        // 请求结束后，当前线程的 MDC 应该没有 traceId
        assertNull(MDC.get(TmlLog.TRACE_ID));
    }

    /**
     * 测试多次请求生成不同的 traceId
     */
    @Test
    void testDifferentTraceIdForEachRequest() throws Exception {
        MvcResult result1 = mockMvc.perform(get("/trace/test"))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/trace/test"))
                .andExpect(status().isOk())
                .andReturn();

        String traceId1 = result1.getResponse().getHeader(TmlLog.TRACE_ID_HEADER);
        String traceId2 = result2.getResponse().getHeader(TmlLog.TRACE_ID_HEADER);

        assertNotEquals(traceId1, traceId2);
        log.info("第一次请求 traceId: {}, 第二次请求 traceId: {}", traceId1, traceId2);
    }

    /**
     * 测试一次完整链路请求的 traceId 传递
     * Controller -> Service -> Repository 整条链路日志应该有相同的 traceId
     */
    @Test
    void testTraceIdInFullChain() throws Exception {
        log.info("========== 开始链路追踪测试 ==========");
        
        MvcResult result = mockMvc.perform(get("/trace/chain"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(TmlLog.TRACE_ID_HEADER))
                .andReturn();

        String traceId = result.getResponse().getHeader(TmlLog.TRACE_ID_HEADER);
        assertNotNull(traceId);
        assertEquals(32, traceId.length(), "traceId 应为 32 位");
        
        log.info("========== 链路追踪测试完成，traceId: {} ==========", traceId);
        log.info("请查看上方日志，Controller/Service/Repository 的日志应该都带有相同的 traceId: {}", traceId);
    }
}
