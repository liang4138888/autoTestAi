package com.api.automation.mapper;

import com.api.automation.model.BugReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BugReportMapper {
    int deleteByPrimaryKey(String id);

    int insert(BugReport record);

    int insertSelective(BugReport record);

    BugReport selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(BugReport record);

    int updateByPrimaryKeyWithBLOBs(BugReport record);

    int updateByPrimaryKey(BugReport record);

    /**
     * 统计Bug提交人员数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getSubmitterStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计Bug负责开发人员数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getDeveloperStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计Bug状态分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getStatusStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计Bug严重程度分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getSeverityStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计Bug优先级分布
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getPriorityStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 统计Bug趋势数据
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果列表
     */
    List<Map<String, Object>> getBugTrendStatistics(@Param("startTime") String startTime, @Param("endTime") String endTime);
}