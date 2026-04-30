package com.smartsurvey.module.survey.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.survey.entity.Survey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SurveyMapper extends BaseMapper<Survey> {
    @Update("UPDATE surveys SET remaining_quota = remaining_quota - 1 WHERE id = #{id} AND remaining_quota > 0")
    int decrementQuota(@Param("id") Long id);
}
