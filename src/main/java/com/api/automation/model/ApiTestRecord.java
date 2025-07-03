package com.api.automation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "api_test_record")
public class ApiTestRecord {
//    @Id
//    @Column(length = 36)
//    private String id;
//
//    private String testId;
//    private String name;
//    @Column(columnDefinition = "TEXT")
//    private String curlCommand;
//    @Column(length = 1000)
//    private String description;
//    private String status; // SUCCESS, FAILED, RUNNING
//    @Column(columnDefinition = "TEXT")
//    private String result;
//    private Integer responseCode;
//    @Column(length = 1000)
//    private String errorMessage;
//
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime runTime;
//    private Long executionTime; // 执行时间(毫秒)

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 1000)
    private String description;
    @Column(length = 1000)
    private String errorMessage;

    private Long executionTime;

    private String name;

    private Integer responseCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime runTime;

    private String status;

    private String testId;

    private String module;

    @Column(columnDefinition = "TEXT")
    private String curlCommand;

    @Column(columnDefinition = "TEXT")
    private String result;


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
        this.module = test.getModule();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public LocalDateTime getRunTime() {
        return runTime;
    }

    public void setRunTime(LocalDateTime runTime) {
        this.runTime = runTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getCurlCommand() {
        return curlCommand;
    }

    public void setCurlCommand(String curlCommand) {
        this.curlCommand = curlCommand;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
