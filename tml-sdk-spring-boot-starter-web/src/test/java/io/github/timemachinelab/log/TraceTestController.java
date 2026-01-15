package io.github.timemachinelab.log;

import io.github.timemachinelab.log.config.TmlLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TraceId 测试用 Controller
 */
@RestController
@RequestMapping("/trace")
@Slf4j
public class TraceTestController {

    @Autowired
    private TraceTestService traceTestService;

    @GetMapping("/test")
    public Map<String, String> test() {
        String traceId = MDC.get(TmlLog.TRACE_ID);
        log.info("处理请求，当前 traceId: {}", traceId);
        
        Map<String, String> result = new HashMap<>();
        result.put("traceId", traceId);
        result.put("message", "success");
        return result;
    }

    /**
     * 模拟一次完整链路请求：Controller -> Service -> Repository
     */
    @GetMapping("/chain")
    public Map<String, Object> chain() {
        log.info("[Controller] 接收到请求，开始处理");
        
        Map<String, Object> result = new HashMap<>();
        result.put("traceId", MDC.get(TmlLog.TRACE_ID));
        result.put("data", traceTestService.process());
        
        log.info("[Controller] 请求处理完成，返回结果");
        return result;
    }
}
