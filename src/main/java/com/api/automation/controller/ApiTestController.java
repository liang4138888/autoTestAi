package com.api.automation.controller;

import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;
import com.api.automation.service.ApiTestService;
import com.api.automation.service.WebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class ApiTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiTestController.class);
    
    @Autowired
    private ApiTestService apiTestService;
    
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 获取所有API测试
     */
    @GetMapping
    public ResponseEntity<List<ApiTest>> getAllApiTests() {
        logger.debug("获取所有API测试");
        try {
            List<ApiTest> tests = apiTestService.getAllApiTests();
            logger.info("成功获取 {} 个API测试", tests.size());
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            logger.error("获取所有API测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 根据ID获取API测试
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiTest> getApiTestById(@PathVariable String id) {
        logger.debug("根据ID获取API测试: {}", id);
        try {
            ApiTest apiTest = apiTestService.getApiTestById(id);
            if (apiTest != null) {
                logger.info("成功获取API测试: {}", apiTest.getName());
                return ResponseEntity.ok(apiTest);
            } else {
                logger.warn("API测试不存在: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("获取API测试失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 创建API测试
     */
    @PostMapping
    public ResponseEntity<ApiTest> createApiTest(@RequestBody ApiTest apiTest) {
        logger.info("创建API测试: {}", apiTest.getName());
        try {
            ApiTest created = apiTestService.addApiTest(apiTest);
            logger.info("成功创建API测试: {}", created.getName());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            logger.error("创建API测试失败: {}", apiTest.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新API测试
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiTest> updateApiTest(@PathVariable String id, @RequestBody ApiTest apiTest) {
        logger.info("更新API测试: {}", id);
        try {
            ApiTest updated = apiTestService.updateApiTest(id, apiTest);
            logger.info("成功更新API测试: {}", updated.getName());
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.warn("API测试不存在: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("更新API测试失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 删除API测试
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiTest(@PathVariable String id) {
        logger.info("删除API测试: {}", id);
        try {
            apiTestService.deleteApiTest(id);
            logger.info("成功删除API测试: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("删除API测试失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 执行单个API测试
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiTest> executeApiTest(@PathVariable String id) {
        logger.info("=== 开始执行单个API测试 ===");
        logger.info("测试ID: {}", id);
        
        try {
            // 先检查测试是否存在
            ApiTest existingTest = apiTestService.getApiTestById(id);
            if (existingTest == null) {
                logger.warn("API测试不存在: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            logger.info("测试名称: {}", existingTest.getName());
            logger.info("Curl命令: {}", existingTest.getCurlCommand());
            
            // 执行测试
            ApiTest result = apiTestService.executeApiTest(id);
            
            logger.info("=== API测试执行完成 ===");
            logger.info("测试名称: {}", result.getName());
            logger.info("执行状态: {}", result.getStatus());
            logger.info("响应码: {}", result.getResponseCode());
            logger.info("执行时间: {}ms", result.getExecutionTime());
            
            if ("FAILED".equals(result.getStatus()) && result.getErrorMessage() != null) {
                logger.error("测试执行失败，错误信息: {}", result.getErrorMessage());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            logger.error("=== API测试执行异常 ===");
            logger.error("测试ID: {}", id);
            logger.error("异常类型: {}", e.getClass().getSimpleName());
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈:", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("=== API测试执行未知异常 ===");
            logger.error("测试ID: {}", id);
            logger.error("异常类型: {}", e.getClass().getSimpleName());
            logger.error("异常信息: {}", e.getMessage());
            logger.error("异常堆栈:", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量执行API测试
     */
    @PostMapping("/batch-execute")
    public ResponseEntity<Map<String, Object>> executeBatchApiTests(@RequestBody List<String> ids) {
        logger.info("=== 开始批量执行API测试 ===");
        logger.info("测试数量: {}", ids.size());
        logger.info("测试ID列表: {}", ids);
        
        try {
            webSocketService.sendBatchExecutionStart(ids.size());
            apiTestService.executeBatchApiTests(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "批量执行已开始");
            response.put("count", ids.size());
            
            logger.info("批量执行已启动，测试数量: {}", ids.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("批量执行API测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 执行所有启用的API测试
     */
    @PostMapping("/execute-all")
    public ResponseEntity<Map<String, Object>> executeAllEnabledApiTests() {
        logger.info("=== 开始执行所有启用的API测试 ===");
        
        try {
            List<ApiTest> enabledTests = apiTestService.getAllApiTests().stream()
                .filter(ApiTest::getEnabled)
                .collect(Collectors.toList());
            
            logger.info("启用的测试数量: {}", enabledTests.size());
            enabledTests.forEach(test -> logger.debug("启用测试: {} - {}", test.getId(), test.getName()));
            
            webSocketService.sendBatchExecutionStart(enabledTests.size());
            apiTestService.executeAllEnabledApiTests();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "所有启用的API测试执行已开始");
            response.put("count", enabledTests.size());
            
            logger.info("所有启用测试执行已启动，测试数量: {}", enabledTests.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("执行所有启用API测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        logger.debug("获取统计信息");
        try {
            Map<String, Object> statistics = apiTestService.getStatistics();
            logger.info("成功获取统计信息: {}", statistics);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 初始化示例数据
     */
    @PostMapping("/init-sample-data")
    public ResponseEntity<Map<String, Object>> initializeSampleData() {
        logger.info("初始化示例数据");
        try {
            apiTestService.initializeSampleData();
            int count = apiTestService.getAllApiTests().size();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "示例数据初始化完成");
            response.put("count", count);
            
            logger.info("示例数据初始化完成，共 {} 个测试", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("初始化示例数据失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 查询某个用例的历史执行记录
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ApiTestRecord>> getTestHistory(@PathVariable String id) {
        logger.debug("获取测试历史记录: {}", id);
        try {
            List<ApiTestRecord> history = apiTestService.getHistoryByTestId(id);
            logger.info("成功获取测试历史记录: {}, 记录数: {}", id, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("获取测试历史记录失败: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 分页查询API测试（支持筛选和排序）
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getApiTestsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        logger.debug("分页查询API测试: page={}, size={}, search={}, status={}, sortBy={}, sortOrder={}", 
                    page, size, search, status, sortBy, sortOrder);
        
        try {
            Map<String, Object> result = apiTestService.getApiTestsByPage(page, size, search, status, sortBy, sortOrder);
            logger.info("分页查询成功: 总数={}, 当前页={}, 页大小={}", 
                       result.get("totalElements"), result.get("currentPage"), result.get("pageSize"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("分页查询API测试失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 