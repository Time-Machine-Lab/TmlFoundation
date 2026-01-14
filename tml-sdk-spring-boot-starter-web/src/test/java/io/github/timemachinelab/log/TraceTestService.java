package io.github.timemachinelab.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TraceId 测试用 Service
 */
@Service
@Slf4j
public class TraceTestService {

    @Autowired
    private TraceTestRepository traceTestRepository;

    public String process() {
        log.info("[Service] 开始业务处理");
        
        // 模拟业务逻辑
        String data = traceTestRepository.queryData();
        
        log.info("[Service] 业务处理完成，获取数据: {}", data);
        return data;
    }
}
