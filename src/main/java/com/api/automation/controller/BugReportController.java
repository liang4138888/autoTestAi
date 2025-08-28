package com.api.automation.controller;

import com.api.automation.service.BugReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author liang
 * @className BugReportController
 * @description Bug报告管理和统计分析控制器
 * @date 2025/8/27 21:44
 */
@RestController
@RequestMapping("/api/bugs")
@CrossOrigin(origins = "*")
public class BugReportController {
    @Autowired
    private BugReportService bugReportService;

    /**
     * 导入数据
     */
    @GetMapping("/add")
    public Map<String, Object> add() {
        return bugReportService.createData();
    }

    /**
     * 获取Bug提交人员统计 - 指定时间段内每个人提交的Bug数量
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 统计结果
     */
    @GetMapping("/statistics/submitter")
    public Map<String, Object> getSubmitterStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getSubmitterStatistics(startTime, endTime);
    }

    /**
     * 获取Bug负责开发人员统计 - 指定时间段内每个开发人员被提了多少Bug
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 统计结果
     */
    @GetMapping("/statistics/developer")
    public Map<String, Object> getDeveloperStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getDeveloperStatistics(startTime, endTime);
    }

    /**
     * 获取Bug状态分布统计
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)  
     * @return 统计结果
     */
    @GetMapping("/statistics/status")
    public Map<String, Object> getStatusStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getStatusStatistics(startTime, endTime);
    }

    /**
     * 获取Bug严重程度分布统计
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 统计结果
     */
    @GetMapping("/statistics/severity")
    public Map<String, Object> getSeverityStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getSeverityStatistics(startTime, endTime);
    }

    /**
     * 获取Bug优先级分布统计
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 统计结果
     */
    @GetMapping("/statistics/priority")
    public Map<String, Object> getPriorityStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getPriorityStatistics(startTime, endTime);
    }

    /**
     * 获取Bug趋势统计 - 按日期统计Bug数量变化
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 统计结果
     */
    @GetMapping("/statistics/trend")
    public Map<String, Object> getBugTrendStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getBugTrendStatistics(startTime, endTime);
    }

    /**
     * 获取综合报表数据
     * @param startTime 开始时间 (yyyy-MM-dd HH:mm:ss)
     * @param endTime 结束时间 (yyyy-MM-dd HH:mm:ss)
     * @return 综合统计结果
     */
    @GetMapping("/statistics/summary")
    public Map<String, Object> getSummaryStatistics(
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        return bugReportService.getSummaryStatistics(startTime, endTime);
    }
}
