package io.github.timemachinelab.log.interceptor;

import io.github.timemachinelab.log.config.TmlLogConstant;
import io.github.timemachinelab.log.context.TmlLogTraceContext;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web 请求 TraceId 过滤器，默认基于 TTL 实现跨线程传递
 *
 * @author glser
 * @since 2026/01/17
 */
public class TmlLogWebTrace extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        TmlLogTraceContext tmlLogTraceContext = TmlLogTraceContext.Holder.get();
        try {
            // 首先从请求头获取 traceId
            String traceId = request.getHeader(TmlLogConstant.TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = tmlLogTraceContext.generateTraceId();
            }
            // 将 traceId 放入 MDC
            tmlLogTraceContext.set(tmlLogTraceContext.getTraceIdKey(), traceId);
            // 响应结果也放入 traceId，后期 elk 查询
            response.setHeader(tmlLogTraceContext.getTraceIdHeader(), traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，防止线程复用导致数据污染
            tmlLogTraceContext.clear();
        }
    }
}
