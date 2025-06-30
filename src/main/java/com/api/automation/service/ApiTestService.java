package com.api.automation.service;

import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;

import java.util.List;
import java.util.Map;

public interface ApiTestService {
    /**
     * 添加API测试
     */
    ApiTest addApiTest(ApiTest apiTest);

    /**
     * 更新API测试
     */
    ApiTest updateApiTest(String id, ApiTest apiTest);

    /**
     * 删除API测试
     */
    void deleteApiTest(String id);

    /**
     * 获取所有API测试
     */
    List<ApiTest> getAllApiTests();

    /**
     * 根据ID获取API测试
     */
    ApiTest getApiTestById(String id);

    /**
     * 执行单个API测试，并记录执行历史
     */
    ApiTest executeApiTest(String id);

    /**
     * 批量执行API测试
     */
    void executeBatchApiTests(List<String> ids);

    /**
     * 执行所有启用的API测试
     */
    void executeAllEnabledApiTests();

    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取某个用例的历史执行记录
     */
    List<ApiTestRecord> getHistoryByTestId(String testId);

    /**
     * 初始化示例数据
     */
    void initializeSampleData();

    /**
     * 分页查询API测试（支持筛选和排序）
     */
    Map<String, Object> getApiTestsByPage(int page, int size, String search, String status, String module, String sortBy, String sortOrder);
} 