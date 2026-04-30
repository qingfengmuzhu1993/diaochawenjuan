package com.smartsurvey.module.response.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.response.entity.Answer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
    @Select("SELECT * FROM answers WHERE response_id = #{responseId}")
    List<Answer> selectByResponseId(@Param("responseId") Long responseId);
}
