package com.api.automation.service;

import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {
    
    /**
     * 获取详细的统计信息
     */
    Map<String, Object> getDetailedStatistics();
    
    /**
     * 获取指定时间范围的统计信息
     */
    Map<String, Object> getStatisticsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 获取成功率低于指定值的API测试
     */
    List<ApiTest> getLowSuccessRateTests(double threshold);
    
    /**
     * 获取执行次数最多的API测试
     */
    List<ApiTest> getMostExecutedTests(int limit);
    
    /**
     * 获取平均执行时间最长的API测试
     */
    List<ApiTest> getSlowestTests(int limit);
    
    /**
     * 获取每日执行统计
     */
    List<Map<String, Object>> getDailyExecutionStats(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取各测试的执行统计
     */
    List<Map<String, Object>> getTestExecutionStats();
    
    /**
     * 获取最近的执行记录
     */
    List<ApiTestRecord> getRecentExecutions(int limit);
    
    /**
     * 获取指定测试的详细统计
     */
    Map<String, Object> getTestDetailedStats(String testId);
    
    /**
     * 获取性能分析数据
     */
    Map<String, Object> getPerformanceAnalysis();
    
    /**
     * 获取失败率最高的测试
     */
    List<ApiTest> getHighFailureRateTests(int limit);
} 