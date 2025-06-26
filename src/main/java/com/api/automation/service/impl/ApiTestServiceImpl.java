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
        // 检查是否已有数据
        if (apiTestMapper.selectCount(null) > 0) {
            return;
        }
        
        // 创建示例数据
        List<ApiTest> sampleTests = Arrays.asList(
            new ApiTest("用户登录API", "curl -X POST http://localhost:8080/api/login -H 'Content-Type: application/json' -d '{\"username\":\"test\",\"password\":\"123456\"}'", "测试用户登录功能"),
            new ApiTest("获取用户信息", "curl -X GET http://localhost:8080/api/user/1 -H 'Authorization: Bearer token123'", "测试获取用户信息功能"),
            new ApiTest("创建用户", "curl -X POST http://localhost:8080/api/user -H 'Content-Type: application/json' -d '{\"name\":\"张三\",\"email\":\"zhangsan@example.com\"}'", "测试创建用户功能"),
            new ApiTest("更新用户", "curl -X PUT http://localhost:8080/api/user/1 -H 'Content-Type: application/json' -d '{\"name\":\"李四\",\"email\":\"lisi@example.com\"}'", "测试更新用户功能"),
            new ApiTest("删除用户", "curl -X DELETE http://localhost:8080/api/user/1 -H 'Authorization: Bearer token123'", "测试删除用户功能")
        );
        
        sampleTests.forEach(test -> {
            test.setStatus("NOT_EXECUTED");
            apiTestMapper.insert(test);
        });
    }

    /**
     * 分页查询API测试（支持筛选和排序）
     */
    @Override
    public Map<String, Object> getApiTestsByPage(int page, int size, String search, String status, String sortBy, String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 构建查询条件
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        params.put("search", search);
        params.put("status", status);
        params.put("sortBy", sortBy);
        params.put("sortOrder", sortOrder);
        
        // 查询数据
        List<ApiTest> tests = apiTestMapper.selectByPage(params);
        int total = apiTestMapper.selectCountByPage(params);
        
        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);
        
        result.put("content", tests);
        result.put("totalElements", total);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("hasNext", page < totalPages);
        result.put("hasPrevious", page > 1);
        
        return result;
    }
} 