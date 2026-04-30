package com.smartsurvey.module.response.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.response.entity.Response;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ResponseMapper extends BaseMapper<Response> {
    @Select("SELECT COUNT(*) FROM responses WHERE survey_id = #{surveyId} AND user_id = #{userId} AND status != 'expired'")
    Long countBySurveyAndUser(@Param("surveyId") Long surveyId, @Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT user_id) FROM responses WHERE device_fingerprint = #{fingerprint} AND survey_id = #{surveyId}")
    int countAccountsByFingerprint(@Param("fingerprint") String fingerprint, @Param("surveyId") Long surveyId);

    @Select("SELECT COUNT(*) FROM responses WHERE user_id = #{userId} AND status = 'approved'")
    long countApprovedByUser(@Param("userId") Long userId);
}
