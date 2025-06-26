package com.api.automation.service;

import com.api.automation.model.ApiTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CurlExecutorService {
    /**
     * 异步执行curl命令
     */
    CompletableFuture<ApiTest> executeCurlAsync(ApiTest apiTest);

    /**
     * 执行curl命令
     */
    ApiTest executeCurl(ApiTest apiTest);

    /**
     * 批量执行curl命令
     */
    void executeBatchCurl(List<ApiTest> apiTests);
} 