package com.api.automation.service;

import java.util.Map;

/**
 * @author liang
 * @className BugReportService
 * @description Bug报告管理和统计分析服务接口
 * @date 2025/8/27 21:47
 */
public interface BugReportService {
    
    /**
     * 创建/导入Bug数据
     */
    Map<String, Object> createData();
    
    /**
     * 获取Bug提交人员统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getSubmitterStatistics(String startTime, String endTime);
    
    /**
     * 获取Bug负责开发人员统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getDeveloperStatistics(String startTime, String endTime);
    
    /**
     * 获取Bug状态分布统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getStatusStatistics(String startTime, String endTime);
    
    /**
     * 获取Bug严重程度分布统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getSeverityStatistics(String startTime, String endTime);
    
    /**
     * 获取Bug优先级分布统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getPriorityStatistics(String startTime, String endTime);
    
    /**
     * 获取Bug趋势统计
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getBugTrendStatistics(String startTime, String endTime);
    
    /**
     * 获取综合统计报表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getSummaryStatistics(String startTime, String endTime);
}
