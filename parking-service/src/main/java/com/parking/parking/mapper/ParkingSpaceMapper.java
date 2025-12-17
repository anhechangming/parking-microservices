package com.parking.parking.mapper;

import com.parking.parking.entity.ParkingSpace;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 车位Mapper接口
 *
 * @author Parking System
 */
@Mapper
public interface ParkingSpaceMapper {

    @Select("SELECT * FROM parking_space WHERE park_id = #{parkId}")
    ParkingSpace findById(@Param("parkId") Long parkId);

    @Select("SELECT * FROM parking_space WHERE park_status = '0' ORDER BY park_num ASC")
    List<ParkingSpace> findAvailable();

    @Select("SELECT COUNT(*) FROM parking_space WHERE park_num = #{parkNum}")
    int countByParkNum(@Param("parkNum") String parkNum);

    @Insert("INSERT INTO parking_space(park_num, park_type, park_status, remark) " +
            "VALUES(#{parkNum}, #{parkType}, #{parkStatus}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "parkId", keyColumn = "park_id")
    int insert(ParkingSpace parkingSpace);

    @Update("UPDATE parking_space SET park_num=#{parkNum}, park_type=#{parkType}, " +
            "park_status=#{parkStatus}, remark=#{remark} WHERE park_id=#{parkId}")
    int update(ParkingSpace parkingSpace);

    @Delete("DELETE FROM parking_space WHERE park_id = #{parkId}")
    int deleteById(@Param("parkId") Long parkId);

    // 分页查询方法（复杂查询，在XML中实现）
    List<ParkingSpace> findByPage(@Param("offset") int offset,
                                   @Param("limit") int limit,
                                   @Param("keyword") String keyword,
                                   @Param("status") String status);

    int countByKeyword(@Param("keyword") String keyword,
                       @Param("status") String status);
}
