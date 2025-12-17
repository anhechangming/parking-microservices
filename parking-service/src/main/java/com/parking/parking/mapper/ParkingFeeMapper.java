package com.parking.parking.mapper;

import com.parking.parking.entity.ParkingFee;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 停车费Mapper接口
 *
 * @author Parking System
 */
@Mapper
public interface ParkingFeeMapper {

    @Select("SELECT * FROM fee_park WHERE park_fee_id = #{parkFeeId}")
    ParkingFee findById(@Param("parkFeeId") Long parkFeeId);

    @Select("SELECT * FROM fee_park WHERE user_id = #{userId} ORDER BY pay_park_month DESC")
    List<ParkingFee> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM fee_park WHERE user_id = #{userId} AND pay_park_status = '0' ORDER BY pay_park_month DESC")
    List<ParkingFee> findUnpaidByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM fee_park WHERE user_id = #{userId} AND park_id = #{parkId} AND pay_park_month = #{month}")
    int countByUserIdAndParkIdAndMonth(@Param("userId") Long userId,
                                        @Param("parkId") Long parkId,
                                        @Param("month") String month);

    @Insert("INSERT INTO fee_park(user_id, park_id, pay_park_month, pay_park_money, pay_park_status, remark) " +
            "VALUES(#{userId}, #{parkId}, #{payParkMonth}, #{payParkMoney}, #{payParkStatus}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "parkFeeId", keyColumn = "park_fee_id")
    int insert(ParkingFee parkingFee);

    @Update("UPDATE fee_park SET pay_park_status=#{payParkStatus}, pay_park_time=#{payParkTime} " +
            "WHERE park_fee_id=#{parkFeeId}")
    int update(ParkingFee parkingFee);

    @Delete("DELETE FROM fee_park WHERE park_fee_id = #{parkFeeId}")
    int deleteById(@Param("parkFeeId") Long parkFeeId);

    // 分页查询方法（复杂查询，在XML中实现）
    List<ParkingFee> findByPage(@Param("offset") int offset,
                                 @Param("limit") int limit,
                                 @Param("userId") Long userId,
                                 @Param("payStatus") String payStatus);

    int countByConditions(@Param("userId") Long userId,
                          @Param("payStatus") String payStatus);
}
