package com.api.automation.service.impl;

import com.api.automation.model.ApiTest;
import com.api.automation.service.CurlExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CurlExecutorServiceImpl implements CurlExecutorService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurlExecutorServiceImpl.class);

    /**
     * 异步执行curl命令
     */
    @Override
    public CompletableFuture<ApiTest> executeCurlAsync(ApiTest apiTest) {
        return CompletableFuture.supplyAsync(() -> {
            return executeCurl(apiTest);
        });
    }

    /**
     * 执行curl命令
     */
    @Override
    public ApiTest executeCurl(ApiTest apiTest) {
        long startTime = System.currentTimeMillis();
        apiTest.setStatus("RUNNING");
        apiTest.setLastRunTime(LocalDateTime.now());
        
        try {
            logger.info("开始执行API测试: {}", apiTest.getName());
            logger.info("Curl命令: {}", apiTest.getCurlCommand());

            // 执行curl命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", apiTest.getCurlCommand());
            
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            
            // 解析结果
            String result = output.toString().trim();
            String errorMessage = errorOutput.toString().trim();
            
            // 保存结果（限制长度避免数据库字段溢出）
            String resultToSave = result;
            if (resultToSave != null && resultToSave.length() > 9500) {
                resultToSave = resultToSave.substring(0, 9500) + "\n\n[结果已截断，完整结果请查看执行记录]";
            }
            apiTest.setResult(resultToSave);
            apiTest.setErrorMessage(errorMessage);
            
            // 检查是否成功（exitCode=0表示curl命令执行成功）
            if (exitCode == 0) {
                apiTest.setStatus("SUCCESS");
                apiTest.setResponseCode(0);
                logger.info("API测试成功: {}", apiTest.getName());
            } else {
                apiTest.setStatus("FAILED");
                apiTest.setResponseCode(exitCode);
                logger.error("API测试失败: {}, 退出码: {}, 错误信息: {}", 
                    apiTest.getName(), exitCode, errorMessage);
            }
            
        } catch (IOException | InterruptedException e) {
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            apiTest.setStatus("FAILED");
            apiTest.setErrorMessage("执行异常: " + e.getMessage());
            apiTest.setResponseCode(-1);
            logger.error("API测试执行异常: {}", apiTest.getName(), e);
        }
        
        return apiTest;
    }

    /**
     * 批量执行curl命令
     */
    @Override
    public void executeBatchCurl(List<ApiTest> apiTests) {
        logger.info("开始批量执行 {} 个API测试", apiTests.size());
        
        apiTests.stream()
            .filter(ApiTest::getEnabled)
            .forEach(apiTest -> {
                executeCurlAsync(apiTest).thenAccept(result -> {
                    logger.info("批量执行完成: {} - {}", result.getName(), result.getStatus());
                });
            });
    }
} 