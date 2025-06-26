package com.api.automation.service.impl;

import com.api.automation.mapper.ApiTestMapper;
import com.api.automation.mapper.ApiTestRecordMapper;
import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;
import com.api.automation.service.ApiTestService;
import com.api.automation.service.CurlExecutorService;
import com.api.automation.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiTestServiceImpl implements ApiTestService {
    @Autowired
    private ApiTestMapper apiTestMapper;
    @Autowired
    private ApiTestRecordMapper apiTestRecordMapper;
    @Autowired
    private CurlExecutorService curlExecutorService;
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 添加API测试
     */
    @Override
    public ApiTest addApiTest(ApiTest apiTest) {
        apiTestMapper.insert(apiTest);
        return apiTest;
    }

    /**
     * 更新API测试
     */
    @Override
    public ApiTest updateApiTest(String id, ApiTest apiTest) {
        ApiTest existingTest = apiTestMapper.selectById(id);
        if (existingTest != null) {
            apiTest.setId(id);
            apiTestMapper.updateById(apiTest);
            return apiTest;
        }
        throw new RuntimeException("API测试不存在: " + id);
    }

    /**
     * 删除API测试
     */
    @Override
    public void deleteApiTest(String id) {
        apiTestMapper.deleteById(id);
    }

    /**
     * 获取所有API测试
     */
    @Override
    public List<ApiTest> getAllApiTests() {
        return apiTestMapper.selectList(null);
    }

    /**
     * 根据ID获取API测试
     */
    @Override
    public ApiTest getApiTestById(String id) {
        return apiTestMapper.selectById(id);
    }

    /**
     * 执行单个API测试，并记录执行历史
     */
    @Override
    public ApiTest executeApiTest(String id) {
        ApiTest apiTest = getApiTestById(id);
        if (apiTest == null) {
            throw new RuntimeException("API测试不存在: " + id);
        }
        ApiTest result = curlExecutorService.executeCurl(apiTest);
        apiTestMapper.updateById(result);
        // 记录执行历史
        apiTestRecordMapper.insert(new ApiTestRecord(result));
        webSocketService.sendApiTestUpdate(result);
        return result;
    }

    /**
     * 批量执行API测试
     */
    @Override
    public void executeBatchApiTests(List<String> ids) {
        List<ApiTest> testsToExecute = ids.stream()
            .map(this::getApiTestById)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        testsToExecute.forEach(test -> executeApiTest(test.getId()));
    }

    /**
     * 执行所有启用的API测试
     */
    @Override
    public void executeAllEnabledApiTests() {
        List<ApiTest> enabledTests = getAllApiTests().stream()
            .filter(ApiTest::getEnabled)
            .collect(Collectors.toList());
        enabledTests.forEach(test -> executeApiTest(test.getId()));
    }

    /**
     * 获取统计信息
     */
    @Override
    public Map<String, Object> getStatistics() {
        List<ApiTest> all = getAllApiTests();
        Map<String, Object> stats = new HashMap<>();
        long total = all.size();
        long success = all.stream().filter(test -> "SUCCESS".equals(test.getStatus())).count();
        long failed = all.stream().filter(test -> "FAILED".equals(test.getStatus())).count();
        long running = all.stream().filter(test -> "RUNNING".equals(test.getStatus())).count();
        long notExecuted = all.stream().filter(test -> test.getStatus() == null).count();
        
        stats.put("total", total);
        stats.put("success", success);
        stats.put("failed", failed);
        stats.put("running", running);
        stats.put("notExecuted", notExecuted);
        stats.put("successRate", total > 0 ? (double) success / total * 100 : 0);
        return stats;
    }

    /**
     * 获取某个用例的历史执行记录
     */
    @Override
    public List<ApiTestRecord> getHistoryByTestId(String testId) {
        return apiTestRecordMapper.selectByTestIdOrderByRunTimeDesc(testId);
    }

    /**
     * 初始化示例数据
     */
    @Override
    public void initializeSampleData() {
        // 使用MyBatis Plus的count方法
        Long count = apiTestMapper.selectCount(null);
        if (count == 0) {
            addApiTest(new ApiTest(
                "健康检查接口",
                "curl -X GET http://localhost:8080/api/tests/statistics",
                "测试系统健康检查接口，验证API服务是否正常运行"
            ));
            addApiTest(new ApiTest(
                "创建API测试",
                "curl -X POST http://localhost:8080/api/tests -H 'Content-Type: application/json' -d '{\"name\":\"测试接口\",\"curlCommand\":\"curl -X GET http://example.com\",\"description\":\"测试描述\"}'",
                "测试创建新的API测试用例功能"
            ));
            addApiTest(new ApiTest(
                "模拟认证接口",
                "curl -X GET http://localhost:8080/api/tests -H 'Authorization: Bearer test-token'",
                "测试需要认证的API接口，验证认证头处理"
            ));
            addApiTest(new ApiTest(
                "公共API测试",
                "curl -X GET https://httpbin.org/json",
                "测试外部公共API接口，验证网络连接和JSON响应"
            ));
            addApiTest(new ApiTest(
                "404错误测试",
                "curl -X GET http://localhost:8080/api/nonexistent",
                "测试404错误处理，验证错误响应格式"
            ));
        }
    }
} 