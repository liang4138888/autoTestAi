package com.api.automation.mapper;

import com.api.automation.model.ApiTest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * API测试数据访问层
 */
@Mapper
public interface ApiTestMapper extends BaseMapper<ApiTest> {

    /**
     * 根据状态查询API测试列表
     */
    List<ApiTest> selectByStatus(@Param("status") String status);

    /**
     * 根据名称模糊查询
     */
    List<ApiTest> selectByNameLike(@Param("name") String name);

    /**
     * 查询最近执行的API测试
     */
    List<ApiTest> selectRecentlyExecuted(@Param("limit") int limit);

    /**
     * 查询执行时间超过指定阈值的API测试
     */
    List<ApiTest> selectByExecutionTimeThreshold(@Param("threshold") long threshold);

    /**
     * 统计各状态的API测试数量
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 查询指定时间范围内执行的API测试
     */
    List<ApiTest> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询成功率低于指定值的API测试
     */
    List<ApiTest> selectByLowSuccessRate(@Param("successRate") double successRate);

    /**
     * 批量更新API测试状态
     */
    int batchUpdateStatus(@Param("ids") List<String> ids, @Param("status") String status);

    /**
     * 查询执行次数最多的API测试
     */
    List<ApiTest> selectMostExecuted(@Param("limit") int limit);

    /**
     * 查询平均执行时间最长的API测试
     */
    List<ApiTest> selectSlowestApiTests(@Param("limit") int limit);

    /**
     * 根据描述关键词查询
     */
    List<ApiTest> selectByDescriptionKeyword(@Param("keyword") String keyword);
}
