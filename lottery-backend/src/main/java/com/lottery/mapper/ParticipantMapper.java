package com.lottery.mapper;

import com.lottery.entity.Participant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 参与记录Mapper接口
 *
 * @author lottery
 * @since 2024-12-16
 */
@Mapper
public interface ParticipantMapper {

    /**
     * 插入参与记录
     */
    int insert(Participant participant);

    /**
     * 批量插入参与记录
     */
    int batchInsert(@Param("list") List<Participant> participants);

    /**
     * 根据活动ID和OpenID查询
     */
    Participant selectByActivityAndOpenid(@Param("activityId") Long activityId, 
                                         @Param("openid") String openid);

    /**
     * 根据活动ID查询所有参与者
     */
    List<Participant> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 统计活动参与人数
     */
    int countByActivityId(@Param("activityId") Long activityId);

    /**
     * 更新中奖状态
     */
    int updateWinnerStatus(@Param("id") Long id, @Param("isWinner") Integer isWinner);

    /**
     * 批量更新中奖状态
     */
    int batchUpdateWinnerStatus(@Param("ids") List<Long> ids);
}
