package com.api.automation.controller;

import com.api.automation.service.BugReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author liang
 * @className BugReportController
 * @description TODO
 * @date 2025/8/27 21:44
 */
@RestController
@RequestMapping("/api/bugs")
@CrossOrigin(origins = "*")
public class BugReportController {
    @Autowired
    private BugReportService bugReportService;

    /**
     * 倒入数据
     */
    @GetMapping("/add")
    public Map<String, Object> add() {
        return bugReportService.createData();
    }

}
