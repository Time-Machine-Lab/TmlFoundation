package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.config.TmlLog;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TraceId 过滤器
 * 为每个 HTTP 请求生成唯一的 traceId，并放入 MDC 中供日志使用
 */
public class TraceIdWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 首先从请求头获取 traceId
            String traceId = request.getHeader(TmlLog.TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = TmlLog.generateTraceId();
            }
            // 将 traceId 放入 MDC（key 是 "traceId"，与 log4j2 配置的 %X{traceId} 对应）
            MDC.put(TmlLog.TRACE_ID, traceId);
            // 响应结果也放入 traceId，后期 elk 查询
            response.setHeader(TmlLog.TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，防止线程复用导致数据污染
            MDC.remove(TmlLog.TRACE_ID);
        }
    }
}
