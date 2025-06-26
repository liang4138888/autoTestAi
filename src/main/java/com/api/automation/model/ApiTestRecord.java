package com.api.automation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_test_record")
public class ApiTestRecord {
    @Id
    @Column(length = 36)
    private String id;

    private String testId;
    private String name;
    @Column(length = 1000)
    private String curlCommand;
    @Column(length = 1000)
    private String description;
    private String status; // SUCCESS, FAILED, RUNNING
    @Column(columnDefinition = "TEXT")
    private String result;
    private Integer responseCode;
    @Column(length = 1000)
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime runTime;
    private Long executionTime; // 执行时间(毫秒)

    public ApiTestRecord() {
        this.id = UUID.randomUUID().toString();
        this.runTime = LocalDateTime.now();
    }

    public ApiTestRecord(ApiTest test) {
        this();
        this.testId = test.getId();
        this.name = test.getName();
        this.curlCommand = test.getCurlCommand();
        this.description = test.getDescription();
        this.status = test.getStatus();
        this.result = test.getResult();
        this.responseCode = test.getResponseCode();
        this.errorMessage = test.getErrorMessage();
        this.executionTime = test.getExecutionTime();
    }

    // Getter和Setter方法 ... 保持不变
    // ... existing code ...
}
