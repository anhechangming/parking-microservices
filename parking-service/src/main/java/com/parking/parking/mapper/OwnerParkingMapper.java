package com.parking.parking.mapper;

import com.parking.parking.entity.OwnerParking;
import org.apache.ibatis.annotations.*;

/**
 * 业主车位关联Mapper接口
 *
 * @author Parking System
 */
@Mapper
public interface OwnerParkingMapper {

    @Select("SELECT * FROM owner_parking WHERE user_id = #{userId} AND payment_status = '1'")
    OwnerParking findByUserIdAndActive(@Param("userId") Long userId);

    @Select("SELECT * FROM owner_parking WHERE park_id = #{parkId} AND payment_status = '1'")
    OwnerParking findByParkIdAndActive(@Param("parkId") Long parkId);

    @Select("SELECT * FROM owner_parking WHERE user_id = #{userId} ORDER BY id DESC LIMIT 1")
    OwnerParking findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM owner_parking WHERE user_id = #{userId} AND payment_status = '1'")
    int countByUserIdAndActive(@Param("userId") Long userId);

    @Insert("INSERT INTO owner_parking(user_id, park_id, car_num, entry_time, payment_status) " +
            "VALUES(#{userId}, #{parkId}, #{carNum}, #{entryTime}, #{paymentStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(OwnerParking ownerParking);

    @Update("UPDATE owner_parking SET payment_status=#{paymentStatus}, car_num=#{carNum}, exit_time=#{exitTime} WHERE id=#{id}")
    int update(OwnerParking ownerParking);
}
