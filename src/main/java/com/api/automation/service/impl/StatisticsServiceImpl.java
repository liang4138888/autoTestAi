package com.api.automation.service.impl;

import com.api.automation.mapper.ApiTestMapper;
import com.api.automation.mapper.ApiTestRecordMapper;
import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;
import com.api.automation.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private ApiTestMapper apiTestMapper;
    
    @Autowired
    private ApiTestRecordMapper apiTestRecordMapper;

    @Override
    public Map<String, Object> getDetailedStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取各状态统计
        List<Map<String, Object>> statusStats = apiTestMapper.countByStatus();
        statistics.put("statusStats", statusStats);
        
        // 获取最近执行的测试
        List<ApiTest> recentTests = apiTestMapper.selectRecentlyExecuted(10);
        statistics.put("recentTests", recentTests);
        
        // 获取执行次数最多的测试
        List<ApiTest> mostExecuted = apiTestMapper.selectMostExecuted(5);
        statistics.put("mostExecuted", mostExecuted);
        
        // 获取最近的执行记录
        List<ApiTestRecord> recentRecords = apiTestRecordMapper.selectRecentRecords(20);
        statistics.put("recentRecords", recentRecords);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getStatisticsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取时间范围内的测试
        List<ApiTest> testsInRange = apiTestMapper.selectByTimeRange(startTime, endTime);
        statistics.put("testsInRange", testsInRange);
        
        // 获取时间范围内的执行记录
        List<ApiTestRecord> recordsInRange = apiTestRecordMapper.selectByTimeRange(startTime, endTime);
        statistics.put("recordsInRange", recordsInRange);
        
        // 获取每日执行统计
        List<Map<String, Object>> dailyStats = apiTestRecordMapper.countDailyExecutions(startTime, endTime);
        statistics.put("dailyStats", dailyStats);
        
        return statistics;
    }

    @Override
    public List<ApiTest> getLowSuccessRateTests(double threshold) {
        return apiTestMapper.selectByLowSuccessRate(threshold);
    }

    @Override
    public List<ApiTest> getMostExecutedTests(int limit) {
        return apiTestMapper.selectMostExecuted(limit);
    }

    @Override
    public List<ApiTest> getSlowestTests(int limit) {
        return apiTestMapper.selectSlowestApiTests(limit);
    }

    @Override
    public List<Map<String, Object>> getDailyExecutionStats(LocalDateTime startDate, LocalDateTime endDate) {
        return apiTestRecordMapper.countDailyExecutions(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getTestExecutionStats() {
        return apiTestRecordMapper.countExecutionsByTest();
    }

    @Override
    public List<ApiTestRecord> getRecentExecutions(int limit) {
        return apiTestRecordMapper.selectRecentRecords(limit);
    }

    @Override
    public Map<String, Object> getTestDetailedStats(String testId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取测试基本信息
        ApiTest test = apiTestMapper.selectById(testId);
        stats.put("test", test);
        
        // 获取成功率统计
        Map<String, Object> successRate = apiTestRecordMapper.calculateSuccessRate(testId);
        stats.put("successRate", successRate);
        
        // 获取平均执行时间
        Map<String, Object> avgTime = apiTestRecordMapper.calculateAverageExecutionTime(testId);
        stats.put("avgExecutionTime", avgTime);
        
        // 获取最近执行记录
        List<ApiTestRecord> recentRecords = apiTestRecordMapper.selectRecentRecordsByTestId(testId, 10);
        stats.put("recentRecords", recentRecords);
        
        // 获取失败记录
        List<ApiTestRecord> failedRecords = apiTestRecordMapper.selectFailedRecords(testId);
        stats.put("failedRecords", failedRecords);
        
        return stats;
    }

    @Override
    public Map<String, Object> getPerformanceAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // 获取执行时间超过阈值的记录（比如超过5秒）
        List<ApiTestRecord> slowRecords = apiTestRecordMapper.selectSlowExecutionRecords(5000);
        analysis.put("slowExecutions", slowRecords);
        
        // 获取平均执行时间最长的测试
        List<ApiTest> slowestTests = apiTestMapper.selectSlowestApiTests(10);
        analysis.put("slowestTests", slowestTests);
        
        // 获取执行时间超过阈值的测试
        List<ApiTest> slowTests = apiTestMapper.selectByExecutionTimeThreshold(5000);
        analysis.put("slowTests", slowTests);
        
        return analysis;
    }

    @Override
    public List<ApiTest> getHighFailureRateTests(int limit) {
        // 这里可以通过自定义SQL查询失败率最高的测试
        // 暂时返回成功率低于50%的测试
        return apiTestMapper.selectByLowSuccessRate(50.0);
    }
} 