package com.api.automation.controller;

import com.api.automation.model.ApiTest;
import com.api.automation.model.ApiTestRecord;
import com.api.automation.service.ApiTestService;
import com.api.automation.service.WebSocketService;
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
    
    @Autowired
    private ApiTestService apiTestService;
    
    @Autowired
    private WebSocketService webSocketService;

    /**
     * 获取所有API测试
     */
    @GetMapping
    public ResponseEntity<List<ApiTest>> getAllApiTests() {
        return ResponseEntity.ok(apiTestService.getAllApiTests());
    }

    /**
     * 根据ID获取API测试
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiTest> getApiTestById(@PathVariable String id) {
        ApiTest apiTest = apiTestService.getApiTestById(id);
        if (apiTest != null) {
            return ResponseEntity.ok(apiTest);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建API测试
     */
    @PostMapping
    public ResponseEntity<ApiTest> createApiTest(@RequestBody ApiTest apiTest) {
        ApiTest created = apiTestService.addApiTest(apiTest);
        return ResponseEntity.ok(created);
    }

    /**
     * 更新API测试
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiTest> updateApiTest(@PathVariable String id, @RequestBody ApiTest apiTest) {
        try {
            ApiTest updated = apiTestService.updateApiTest(id, apiTest);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除API测试
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApiTest(@PathVariable String id) {
        apiTestService.deleteApiTest(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 执行单个API测试
     */
    @PostMapping("/{id}/execute")
    public ResponseEntity<ApiTest> executeApiTest(@PathVariable String id) {
        try {
            ApiTest result = apiTestService.executeApiTest(id);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 批量执行API测试
     */
    @PostMapping("/batch-execute")
    public ResponseEntity<Map<String, Object>> executeBatchApiTests(@RequestBody List<String> ids) {
        webSocketService.sendBatchExecutionStart(ids.size());
        apiTestService.executeBatchApiTests(ids);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "批量执行已开始");
        response.put("count", ids.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 执行所有启用的API测试
     */
    @PostMapping("/execute-all")
    public ResponseEntity<Map<String, Object>> executeAllEnabledApiTests() {
        List<ApiTest> enabledTests = apiTestService.getAllApiTests().stream()
            .filter(ApiTest::getEnabled)
            .collect(Collectors.toList());
        
        webSocketService.sendBatchExecutionStart(enabledTests.size());
        apiTestService.executeAllEnabledApiTests();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "所有启用的API测试执行已开始");
        response.put("count", enabledTests.size());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = apiTestService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 初始化示例数据
     */
    @PostMapping("/init-sample-data")
    public ResponseEntity<Map<String, Object>> initializeSampleData() {
        apiTestService.initializeSampleData();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "示例数据初始化完成");
        response.put("count", apiTestService.getAllApiTests().size());
        return ResponseEntity.ok(response);
    }

    /**
     * 查询某个用例的历史执行记录
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ApiTestRecord>> getTestHistory(@PathVariable String id) {
        return ResponseEntity.ok(apiTestService.getHistoryByTestId(id));
    }
} 