package com.api.automation.mapper;

import com.api.automation.model.BugUser;

public interface BugUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(BugUser record);

    int insertSelective(BugUser record);

    BugUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BugUser record);

    int updateByPrimaryKey(BugUser record);
}