package com.smartsurvey.module.incentive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.incentive.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE user_id = #{userId} AND type = 'reward' AND status = 'success'")
    BigDecimal getTotalEarnings(@Param("userId") Long userId);
}
