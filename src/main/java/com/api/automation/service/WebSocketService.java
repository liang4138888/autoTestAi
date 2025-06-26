package com.api.automation.service;

import com.api.automation.model.ApiTest;

import java.util.Map;

public interface WebSocketService {
    /**
     * 发送API测试更新
     */
    void sendApiTestUpdate(ApiTest apiTest);

    /**
     * 发送统计信息更新
     */
    void sendStatisticsUpdate(Map<String, Object> statistics);

    /**
     * 发送批量执行开始通知
     */
    void sendBatchExecutionStart(int count);

    /**
     * 发送批量执行完成通知
     */
    void sendBatchExecutionComplete(int successCount, int failedCount);
} 