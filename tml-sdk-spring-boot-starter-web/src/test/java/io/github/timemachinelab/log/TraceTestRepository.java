package io.github.timemachinelab.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * TraceId 测试用 Repository
 */
@Repository
@Slf4j
public class TraceTestRepository {

    public String queryData() {
        log.info("[Repository] 查询数据库");
        
        // 模拟数据库查询
        String result = "mock-data-from-db";
        
        log.info("[Repository] 查询完成，返回数据");
        return result;
    }
}
