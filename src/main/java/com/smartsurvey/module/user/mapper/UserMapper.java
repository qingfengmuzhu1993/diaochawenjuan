package com.smartsurvey.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Update("UPDATE users SET reputation = GREATEST(0, reputation + #{delta}) WHERE id = #{userId}")
    int updateReputation(@Param("userId") Long userId, @Param("delta") int delta);
}
