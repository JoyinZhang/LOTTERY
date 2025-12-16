package com.lottery.mapper;

import com.lottery.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动Mapper接口
 *
 * @author lottery
 * @since 2024-12-16
 */
@Mapper
public interface ActivityMapper {

    /**
     * 插入活动记录
     */
    int insert(Activity activity);

    /**
     * 根据活动编码查询
     */
    Activity selectByCode(@Param("activityCode") String activityCode);

    /**
     * 根据ID查询
     */
    Activity selectById(@Param("id") Long id);

    /**
     * 更新活动状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询待开奖的定时活动
     */
    List<Activity> selectPendingTimedActivities(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据创建者查询活动列表
     */
    List<Activity> selectByCreator(@Param("creatorOpenid") String creatorOpenid, 
                                   @Param("offset") Integer offset, 
                                   @Param("limit") Integer limit);

    /**
     * 统计创建者的活动数量
     */
    int countByCreator(@Param("creatorOpenid") String creatorOpenid);
}
