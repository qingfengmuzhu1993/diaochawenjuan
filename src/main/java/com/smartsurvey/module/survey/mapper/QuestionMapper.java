package com.smartsurvey.module.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.survey.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    @Select("SELECT * FROM questions WHERE survey_id = #{surveyId} ORDER BY order_index")
    List<Question> selectBySurveyId(@Param("surveyId") Long surveyId);
}
