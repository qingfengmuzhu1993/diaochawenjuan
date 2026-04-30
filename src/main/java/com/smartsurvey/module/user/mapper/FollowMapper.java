package com.smartsurvey.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.user.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface FollowMapper extends BaseMapper<Follow> {

    @Select("SELECT u.id, u.username, u.avatar_url, u.bio FROM users u " +
            "INNER JOIN follows f ON u.id = f.follower_id WHERE f.followee_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<Map<String, Object>> getFollowers(@Param("userId") Long userId);

    @Select("SELECT u.id, u.username, u.avatar_url, u.bio FROM users u " +
            "INNER JOIN follows f ON u.id = f.followee_id WHERE f.follower_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<Map<String, Object>> getFollowing(@Param("userId") Long userId);
}
