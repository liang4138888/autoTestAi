package com.api.automation.service.impl;

import com.api.automation.service.BugReportService;
import com.api.automation.mapper.BugReportMapper;
import com.api.automation.mapper.BugUserMapper;
import com.api.automation.model.BugReport;
import com.api.automation.model.BugUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liang
 * @className BugReportServiceImpl
 * @description TODO
 * @date 2025/8/27 21:47
 */
@Service
public class BugReportServiceImpl implements BugReportService {

    private static final Logger logger = LoggerFactory.getLogger(BugReportServiceImpl.class);
    
    @Autowired
    private BugReportMapper bugReportMapper;
    
    @Autowired
    private BugUserMapper bugUserMapper;

    @Override
    public Map<String, Object> createData() {
        String curl = "curl --location 'https://itwork-test.yonghui.cn/api/test-workbench/v1/bug/list_by_options' \\\n" +
                "--header 'sec-ch-ua-platform: \"macOS\"' \\\n" +
                "--header 'Authorization: bearer f713a92d-d3aa-4257-9023-87f36338e79a' \\\n" +
                "--header 'Referer: https://itwork-test.yonghui.cn/bug-manage' \\\n" +
                "--header 'sec-ch-ua: \"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"' \\\n" +
                "--header 'sec-ch-ua-mobile: ?0' \\\n" +
                "--header 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36' \\\n" +
                "--header 'Accept: application/json, text/plain, */*' \\\n" +
                "--header 'Content-Type: application/json' \\\n" +
                "--data '{\"bugUserList\":[{\"userType\":\"Test\",\"iamUserId\":24306},{\"userType\":\"Test\",\"iamUserId\":21199},{\"userType\":\"Test\",\"iamUserId\":25710},{\"userType\":\"Test\",\"iamUserId\":23435},{\"userType\":\"Test\",\"iamUserId\":19326},{\"userType\":\"Test\",\"iamUserId\":18954},{\"userType\":\"Test\",\"iamUserId\":18901},{\"userType\":\"Test\",\"iamUserId\":23120}],\"startTime\":\"2025-08-01 00:00:00\",\"endTime\":\"2025-08-31 23:59:59\",\"pageNo\":0,\"pageSize\":1000}'";

        Map<String, Object> result = new HashMap<>();
        AtomicInteger bugReportCount = new AtomicInteger(0);
        AtomicInteger bugUserCount = new AtomicInteger(0);
        
        try {
            logger.info("开始执行Bug数据同步...");
            
            // 1. 执行curl命令获取数据
            String jsonResponse = executeCurl(curl);
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                logger.error("执行curl命令失败，返回数据为空");
                result.put("success", false);
                result.put("message", "执行curl命令失败");
                return result;
            }

            // 2. 解析JSON数据
            JSONObject responseJson = JSON.parseObject(jsonResponse);
            JSONArray contentArray = responseJson.getJSONArray("content");
            
            if (contentArray == null) {
                logger.warn("响应数据中没有找到content数组");
                result.put("success", false);
                result.put("message", "响应数据格式错误");
                return result;
            }

            logger.info("获取到 {} 条Bug记录", contentArray.size());

            // 3. 处理每个Bug记录
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            for (int i = 0; i < contentArray.size(); i++) {
                JSONObject bugJson = contentArray.getJSONObject(i);
                
                // 创建BugReport对象
                BugReport bugReport = new BugReport();
                bugReport.setId(String.valueOf(bugJson.getLong("id")));
                bugReport.setProjectId(bugJson.getLong("projectId"));
                bugReport.setProjectName(bugJson.getString("projectName"));
                bugReport.setTitle(bugJson.getString("title"));
                bugReport.setDemandId(bugJson.getLong("demandId"));
                bugReport.setDemandName(bugJson.getString("demandName"));
                bugReport.setIterationId(bugJson.getLong("iterationId"));
                bugReport.setIterationName(bugJson.getString("iterationName"));
                bugReport.setComponentId(bugJson.getLong("componentId"));
                bugReport.setComponentName(bugJson.getString("componentName"));
                bugReport.setTestPlanId(bugJson.getLong("testPlanId"));
                bugReport.setTestPlanName(bugJson.getString("testPlanName"));
                bugReport.setFoundStage(bugJson.getString("foundStage"));
                bugReport.setSeverity(bugJson.getString("severity"));
                bugReport.setPriority(bugJson.getString("priority"));
                bugReport.setBugReason(bugJson.getString("bugReason"));
                bugReport.setStatus(bugJson.getString("status"));
                bugReport.setComplementStatus(bugJson.getString("complementStatus"));
                bugReport.setReopen(bugJson.getInteger("reopen"));
                bugReport.setRetesting(bugJson.getInteger("retesting"));
                bugReport.setOrganizationId(bugJson.getLong("organizationId"));
                bugReport.setBlockLength(bugJson.getInteger("blockLength"));
                bugReport.setCreatedBy(bugJson.getLong("createdBy"));
                bugReport.setLastUpdatedBy(bugJson.getLong("lastUpdatedBy"));
                bugReport.setCreatedUserName(bugJson.getString("createdUserName"));
                bugReport.setBugDesc(bugJson.getString("bugDesc"));
                bugReport.setDeleted(false);

                // 处理日期字段
                try {
                    String creationDateStr = bugJson.getString("creationDate");
                    if (creationDateStr != null) {
                        bugReport.setCreationDate(dateFormat.parse(creationDateStr));
                    }
                    
                    String lastUpdateDateStr = bugJson.getString("lastUpdateDate");
                    if (lastUpdateDateStr != null) {
                        bugReport.setLastUpdateDate(dateFormat.parse(lastUpdateDateStr));
                    }
                } catch (Exception e) {
                    logger.warn("解析日期字段失败: {}", e.getMessage());
                }

                // 保存BugReport
                try {
                    bugReportMapper.insertSelective(bugReport);
                    bugReportCount.incrementAndGet();
                    logger.debug("保存BugReport成功: {}", bugReport.getId());
                } catch (Exception e) {
                    logger.error("保存BugReport失败: {}, 错误: {}", bugReport.getId(), e.getMessage());
                }

                // 4. 处理BugUser列表
                JSONArray bugUserArray = bugJson.getJSONArray("bugUserList");
                if (bugUserArray != null) {
                    for (int j = 0; j < bugUserArray.size(); j++) {
                        JSONObject bugUserJson = bugUserArray.getJSONObject(j);
                        
                        BugUser bugUser = new BugUser();
                        bugUser.setId(UUID.randomUUID().toString());
                        bugUser.setBugId(String.valueOf(bugJson.getLong("id")));
                        bugUser.setUserType(bugUserJson.getString("userType"));
                        bugUser.setIamUserId(bugUserJson.getLong("iamUserId"));
                        bugUser.setIamUserRealName(bugUserJson.getString("iamUserRealName"));
                        bugUser.setCause(bugUserJson.getString("cause"));

                        // 保存BugUser
                        try {
                            bugUserMapper.insertSelective(bugUser);
                            bugUserCount.incrementAndGet();
                            logger.debug("保存BugUser成功: {}", bugUser.getId());
                        } catch (Exception e) {
                            logger.error("保存BugUser失败: {}, 错误: {}", bugUser.getId(), e.getMessage());
                        }
                    }
                }
            }

            logger.info("Bug数据同步完成，共处理 {} 条BugReport记录，{} 条BugUser记录", 
                       bugReportCount.get(), bugUserCount.get());

            result.put("success", true);
            result.put("message", "数据同步成功");
            result.put("bugReportCount", bugReportCount.get());
            result.put("bugUserCount", bugUserCount.get());
            
        } catch (Exception e) {
            logger.error("Bug数据同步失败", e);
            result.put("success", false);
            result.put("message", "数据同步失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> getSubmitterStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug提交人员统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getSubmitterStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug提交人员统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug提交人员统计获取成功，共 {} 个提交人", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug提交人员统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDeveloperStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug负责开发人员统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getDeveloperStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug负责开发人员统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug负责开发人员统计获取成功，共 {} 个开发人员", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug负责开发人员统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getStatusStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug状态分布统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getStatusStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug状态分布统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug状态分布统计获取成功，共 {} 种状态", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug状态分布统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getSeverityStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug严重程度分布统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getSeverityStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug严重程度分布统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug严重程度分布统计获取成功，共 {} 种严重程度", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug严重程度分布统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getPriorityStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug优先级分布统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getPriorityStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug优先级分布统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug优先级分布统计获取成功，共 {} 种优先级", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug优先级分布统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getBugTrendStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取Bug趋势统计数据，时间范围: {} 到 {}", startTime, endTime);
            
            List<Map<String, Object>> statistics = bugReportMapper.getBugTrendStatistics(startTime, endTime);
            
            result.put("success", true);
            result.put("message", "获取Bug趋势统计成功");
            result.put("data", statistics);
            result.put("total", statistics.size());
            
            logger.info("Bug趋势统计获取成功，共 {} 个时间点", statistics.size());
            
        } catch (Exception e) {
            logger.error("获取Bug趋势统计失败", e);
            result.put("success", false);
            result.put("message", "获取统计数据失败: " + e.getMessage());
            result.put("data", new ArrayList<>());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getSummaryStatistics(String startTime, String endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            logger.info("开始获取综合统计报表数据，时间范围: {} 到 {}", startTime, endTime);
            
            // 获取各项统计数据
            Map<String, Object> submitterStats = getSubmitterStatistics(startTime, endTime);
            Map<String, Object> developerStats = getDeveloperStatistics(startTime, endTime);
            Map<String, Object> statusStats = getStatusStatistics(startTime, endTime);
            Map<String, Object> severityStats = getSeverityStatistics(startTime, endTime);
            Map<String, Object> priorityStats = getPriorityStatistics(startTime, endTime);
            Map<String, Object> trendStats = getBugTrendStatistics(startTime, endTime);
            
            // 汇总数据
            Map<String, Object> summaryData = new HashMap<>();
            summaryData.put("submitterStatistics", submitterStats.get("data"));
            summaryData.put("developerStatistics", developerStats.get("data"));
            summaryData.put("statusStatistics", statusStats.get("data"));
            summaryData.put("severityStatistics", severityStats.get("data"));
            summaryData.put("priorityStatistics", priorityStats.get("data"));
            summaryData.put("trendStatistics", trendStats.get("data"));
            
            // 计算总体统计信息
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> submitterData = (List<Map<String, Object>>) submitterStats.get("data");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> statusData = (List<Map<String, Object>>) statusStats.get("data");
            
            int totalBugs = 0;
            if (statusData != null) {
                for (Map<String, Object> item : statusData) {
                    Object count = item.get("count");
                    if (count instanceof Number) {
                        totalBugs += ((Number) count).intValue();
                    }
                }
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> developerData = (List<Map<String, Object>>) developerStats.get("data");
            
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalBugs", totalBugs);
            overview.put("totalSubmitters", submitterData != null ? submitterData.size() : 0);
            overview.put("totalDevelopers", developerData != null ? developerData.size() : 0);
            
            Map<String, Object> timeRange = new HashMap<>();
            timeRange.put("startTime", startTime != null ? startTime : "不限");
            timeRange.put("endTime", endTime != null ? endTime : "不限");
            overview.put("timeRange", timeRange);
            
            summaryData.put("overview", overview);
            
            result.put("success", true);
            result.put("message", "获取综合统计报表成功");
            result.put("data", summaryData);
            
            logger.info("综合统计报表获取成功，总Bug数: {}", totalBugs);
            
        } catch (Exception e) {
            logger.error("获取综合统计报表失败", e);
            result.put("success", false);
            result.put("message", "获取综合统计报表失败: " + e.getMessage());
            result.put("data", new HashMap<>());
        }
        return result;
    }

    /**
     * 执行curl命令并返回响应结果
     */
    private String executeCurl(String curlCommand) {
        try {
            logger.debug("执行curl命令: {}", curlCommand);
            
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", curlCommand);
            
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            StringBuilder errorOutput = new StringBuilder();
            
            // 读取标准输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // 读取错误输出
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.debug("curl命令执行成功");
                return output.toString().trim();
            } else {
                logger.error("curl命令执行失败，退出码: {}, 错误信息: {}", exitCode, errorOutput.toString());
                return null;
            }
            
        } catch (IOException | InterruptedException e) {
            logger.error("执行curl命令异常", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return null;
        }
    }
}
