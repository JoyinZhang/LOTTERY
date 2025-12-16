package com.lottery.mapper;

import com.lottery.entity.Winner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 中奖记录Mapper接口
 *
 * @author lottery
 * @since 2024-12-16
 */
@Mapper
public interface WinnerMapper {

    /**
     * 插入中奖记录
     */
    int insert(Winner winner);

    /**
     * 批量插入中奖记录
     */
    int batchInsert(@Param("list") List<Winner> winners);

    /**
     * 根据活动ID查询中奖名单
     */
    List<Winner> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 统计活动中奖人数
     */
    int countByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据活动ID和OpenID查询
     */
    Winner selectByActivityAndOpenid(@Param("activityId") Long activityId, 
                                    @Param("openid") String openid);
}
