package com.api.automation.mapper;

import com.api.automation.model.ApiTestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API测试记录数据访问层
 */
@Mapper
public interface ApiTestRecordMapper extends BaseMapper<ApiTestRecord> {
    
    /**
     * 根据测试ID查询执行记录，按执行时间倒序
     */
    List<ApiTestRecord> selectByTestIdOrderByRunTimeDesc(@Param("testId") String testId);
    
    /**
     * 查询指定测试的最近N次执行记录
     */
    List<ApiTestRecord> selectRecentRecordsByTestId(@Param("testId") String testId, @Param("limit") int limit);
    
    /**
     * 查询指定时间范围内的执行记录
     */
    List<ApiTestRecord> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 统计指定测试的成功率
     */
    Map<String, Object> calculateSuccessRate(@Param("testId") String testId);
    
    /**
     * 查询执行失败的记录
     */
    List<ApiTestRecord> selectFailedRecords(@Param("testId") String testId);
    
    /**
     * 查询执行时间超过阈值的记录
     */
    List<ApiTestRecord> selectSlowExecutionRecords(@Param("threshold") long threshold);
    
    /**
     * 统计每日执行次数
     */
    List<Map<String, Object>> countDailyExecutions(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    /**
     * 查询平均执行时间
     */
    Map<String, Object> calculateAverageExecutionTime(@Param("testId") String testId);
    
    /**
     * 删除指定测试的历史记录
     */
    int deleteByTestId(@Param("testId") String testId);
    
    /**
     * 查询指定状态的所有记录
     */
    List<ApiTestRecord> selectByStatus(@Param("status") String status);
    
    /**
     * 统计各测试的执行次数
     */
    List<Map<String, Object>> countExecutionsByTest();
    
    /**
     * 查询最近的执行记录（所有测试）
     */
    List<ApiTestRecord> selectRecentRecords(@Param("limit") int limit);
} 