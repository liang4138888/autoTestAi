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
        
        logger.info("=== 开始执行API测试 ===");
        logger.info("测试ID: {}", apiTest.getId());
        logger.info("测试名称: {}", apiTest.getName());
        logger.info("Curl命令: {}", apiTest.getCurlCommand());
        
        try {
            // 执行curl命令
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", apiTest.getCurlCommand());
            
            logger.debug("启动进程执行curl命令");
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            // 读取标准输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    logger.debug("标准输出: {}", line);
                }
            }
            
            // 读取错误输出
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                    logger.warn("错误输出: {}", line);
                }
            }
            
            // 等待进程完成
            logger.debug("等待进程完成...");
            int exitCode = process.waitFor();
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            
            logger.info("进程执行完成，退出码: {}, 执行时间: {}ms", exitCode, apiTest.getExecutionTime());
            
            // 解析结果
            String result = output.toString().trim();
            String errorMessage = errorOutput.toString().trim();
            
            // 记录详细结果
            logger.debug("执行结果长度: {} 字符", result.length());
            if (result.length() > 0) {
                logger.debug("执行结果前500字符: {}", result.substring(0, Math.min(500, result.length())));
            }
            
            if (errorMessage.length() > 0) {
                logger.warn("错误信息: {}", errorMessage);
            }
            
            // 保存结果（限制长度避免数据库字段溢出）
            String resultToSave = result;
            if (resultToSave != null && resultToSave.length() > 9500) {
                resultToSave = resultToSave.substring(0, 9500) + "\n\n[结果已截断，完整结果请查看执行记录]";
                logger.warn("执行结果过长，已截断到9500字符");
            }
            apiTest.setResult(resultToSave);
            apiTest.setErrorMessage(errorMessage);
            
            // 检查是否成功（exitCode=0表示curl命令执行成功）
            if (exitCode == 0) {
                apiTest.setStatus("SUCCESS");
                apiTest.setResponseCode(0);
                logger.info("=== API测试执行成功 ===");
                logger.info("测试名称: {}", apiTest.getName());
                logger.info("执行时间: {}ms", apiTest.getExecutionTime());
            } else {
                apiTest.setStatus("FAILED");
                apiTest.setResponseCode(exitCode);
                logger.error("=== API测试执行失败 ===");
                logger.error("测试名称: {}", apiTest.getName());
                logger.error("退出码: {}", exitCode);
                logger.error("错误信息: {}", errorMessage);
                logger.error("执行时间: {}ms", apiTest.getExecutionTime());
            }
            
        } catch (IOException e) {
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            apiTest.setStatus("FAILED");
            apiTest.setErrorMessage("IO异常: " + e.getMessage());
            apiTest.setResponseCode(-1);
            logger.error("=== API测试执行IO异常 ===");
            logger.error("测试名称: {}", apiTest.getName());
            logger.error("异常类型: IOException");
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈:", e);
        } catch (InterruptedException e) {
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            apiTest.setStatus("FAILED");
            apiTest.setErrorMessage("中断异常: " + e.getMessage());
            apiTest.setResponseCode(-2);
            logger.error("=== API测试执行中断异常 ===");
            logger.error("测试名称: {}", apiTest.getName());
            logger.error("异常类型: InterruptedException");
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈:", e);
            // 恢复中断状态
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            apiTest.setExecutionTime(endTime - startTime);
            apiTest.setStatus("FAILED");
            apiTest.setErrorMessage("未知异常: " + e.getMessage());
            apiTest.setResponseCode(-3);
            logger.error("=== API测试执行未知异常 ===");
            logger.error("测试名称: {}", apiTest.getName());
            logger.error("异常类型: {}", e.getClass().getSimpleName());
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈:", e);
        }
        
        logger.info("=== API测试执行结束 ===");
        logger.info("测试名称: {}", apiTest.getName());
        logger.info("最终状态: {}", apiTest.getStatus());
        logger.info("响应码: {}", apiTest.getResponseCode());
        logger.info("执行时间: {}ms", apiTest.getExecutionTime());
        
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
                }).exceptionally(throwable -> {
                    logger.error("批量执行异常: {} - {}", apiTest.getName(), throwable.getMessage(), throwable);
                    return null;
                });
            });
    }
} 