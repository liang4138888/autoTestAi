package com.api.automation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ibatis.type.JdbcType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_test")
public class ApiTest {
    @Id
    @Column(length = 36)
    private String id;

    private String name;
    @Column(columnDefinition = "TEXT")
    private String curlCommand;
    @Column(length = 1000)
    private String description;
    private String status; // SUCCESS, FAILED, RUNNING
    @Column(columnDefinition = "TEXT")
    private String result;
    private Integer responseCode;
    @Column(length = 1000)
    private String errorMessage;
    private String module;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastRunTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long executionTime; // 执行时间(毫秒)
    private Boolean enabled = true; // 是否启用

    // 构造函数
    public ApiTest() {
        this.id = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
    }

    public ApiTest(String name, String curlCommand, String description) {
        this();
        this.name = name;
        this.curlCommand = curlCommand;
        this.description = description;
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurlCommand() {
        return curlCommand;
    }

    public void setCurlCommand(String curlCommand) {
        this.curlCommand = curlCommand;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
