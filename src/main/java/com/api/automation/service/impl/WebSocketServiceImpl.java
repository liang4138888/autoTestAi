package com.api.automation.service.impl;

import com.api.automation.model.ApiTest;
import com.api.automation.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketServiceImpl implements WebSocketService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServiceImpl.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送API测试更新
     */
    @Override
    public void sendApiTestUpdate(ApiTest apiTest) {
        try {
            messagingTemplate.convertAndSend("/topic/api-test-update", apiTest);
            logger.debug("发送API测试更新: {}", apiTest.getName());
        } catch (Exception e) {
            logger.error("发送API测试更新失败", e);
        }
    }

    /**
     * 发送统计信息更新
     */
    @Override
    public void sendStatisticsUpdate(Map<String, Object> statistics) {
        try {
            messagingTemplate.convertAndSend("/topic/statistics-update", statistics);
            logger.debug("发送统计信息更新");
        } catch (Exception e) {
            logger.error("发送统计信息更新失败", e);
        }
    }

    /**
     * 发送批量执行开始通知
     */
    @Override
    public void sendBatchExecutionStart(int count) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "BATCH_START");
            message.put("count", count);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/batch-execution", message);
            logger.info("发送批量执行开始通知: {} 个测试", count);
        } catch (Exception e) {
            logger.error("发送批量执行通知失败", e);
        }
    }

    /**
     * 发送批量执行完成通知
     */
    @Override
    public void sendBatchExecutionComplete(int successCount, int failedCount) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "BATCH_COMPLETE");
            message.put("successCount", successCount);
            message.put("failedCount", failedCount);
            message.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend("/topic/batch-execution", message);
            logger.info("发送批量执行完成通知: 成功={}, 失败={}", successCount, failedCount);
        } catch (Exception e) {
            logger.error("发送批量执行完成通知失败", e);
        }
    }
} 