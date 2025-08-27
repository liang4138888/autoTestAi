package com.api.automation.mapper;

import com.api.automation.model.BugReport;

public interface BugReportMapper {
    int deleteByPrimaryKey(String id);

    int insert(BugReport record);

    int insertSelective(BugReport record);

    BugReport selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BugReport record);

    int updateByPrimaryKeyWithBLOBs(BugReport record);

    int updateByPrimaryKey(BugReport record);
}