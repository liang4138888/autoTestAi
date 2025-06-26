package com.api.automation.controller;

import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;
import com.api.automation.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取详细统计信息
     */
    @GetMapping("/detailed")
    public Map<String, Object> getDetailedStatistics() {
        return statisticsService.getDetailedStatistics();
    }

    /**
     * 根据时间范围获取统计信息
     */
    @GetMapping("/time-range")
    public Map<String, Object> getStatisticsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return statisticsService.getStatisticsByTimeRange(startTime, endTime);
    }

    /**
     * 获取成功率低于指定值的API测试
     */
    @GetMapping("/low-success-rate")
    public List<ApiTest> getLowSuccessRateTests(@RequestParam(defaultValue = "80.0") double threshold) {
        return statisticsService.getLowSuccessRateTests(threshold);
    }

    /**
     * 获取执行次数最多的API测试
     */
    @GetMapping("/most-executed")
    public List<ApiTest> getMostExecutedTests(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.getMostExecutedTests(limit);
    }

    /**
     * 获取平均执行时间最长的API测试
     */
    @GetMapping("/slowest")
    public List<ApiTest> getSlowestTests(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.getSlowestTests(limit);
    }

    /**
     * 获取每日执行统计
     */
    @GetMapping("/daily-stats")
    public List<Map<String, Object>> getDailyExecutionStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return statisticsService.getDailyExecutionStats(startDate, endDate);
    }

    /**
     * 获取各测试的执行统计
     */
    @GetMapping("/test-stats")
    public List<Map<String, Object>> getTestExecutionStats() {
        return statisticsService.getTestExecutionStats();
    }

    /**
     * 获取最近的执行记录
     */
    @GetMapping("/recent-executions")
    public List<ApiTestRecord> getRecentExecutions(@RequestParam(defaultValue = "20") int limit) {
        return statisticsService.getRecentExecutions(limit);
    }

    /**
     * 获取指定测试的详细统计
     */
    @GetMapping("/test/{testId}")
    public Map<String, Object> getTestDetailedStats(@PathVariable String testId) {
        return statisticsService.getTestDetailedStats(testId);
    }

    /**
     * 获取性能分析数据
     */
    @GetMapping("/performance")
    public Map<String, Object> getPerformanceAnalysis() {
        return statisticsService.getPerformanceAnalysis();
    }

    /**
     * 获取失败率最高的测试
     */
    @GetMapping("/high-failure-rate")
    public List<ApiTest> getHighFailureRateTests(@RequestParam(defaultValue = "10") int limit) {
        return statisticsService.getHighFailureRateTests(limit);
    }
} 