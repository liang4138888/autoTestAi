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
