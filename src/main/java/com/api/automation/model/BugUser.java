package com.api.automation.model;

public class BugUser {
    private String id;

    private String bugId;

    private String userType;

    private Long iamUserId;

    private String iamUserRealName;

    private String cause;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBugId() {
        return bugId;
    }

    public void setBugId(String bugId) {
        this.bugId = bugId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getIamUserId() {
        return iamUserId;
    }

    public void setIamUserId(Long iamUserId) {
        this.iamUserId = iamUserId;
    }

    public String getIamUserRealName() {
        return iamUserRealName;
    }

    public void setIamUserRealName(String iamUserRealName) {
        this.iamUserRealName = iamUserRealName;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }
}