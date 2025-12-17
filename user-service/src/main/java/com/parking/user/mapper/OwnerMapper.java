package com.parking.user.mapper;

import com.parking.user.entity.Owner;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 业主Mapper接口
 *
 * @author Parking System
 */
@Mapper
public interface OwnerMapper {

    @Select("SELECT * FROM live_user WHERE login_name = #{loginName}")
    Owner findByLoginName(@Param("loginName") String loginName);

    @Select("SELECT * FROM live_user WHERE user_id = #{userId}")
    Owner findById(@Param("userId") Long userId);

    @Select("SELECT * FROM live_user WHERE status = '0' ORDER BY create_time DESC")
    List<Owner> findAll();

    @Select("SELECT COUNT(*) FROM live_user WHERE login_name = #{loginName}")
    int countByLoginName(@Param("loginName") String loginName);

    @Insert("INSERT INTO live_user(login_name, password, username, phone, sex, id_card, status) " +
            "VALUES(#{loginName}, #{password}, #{username}, #{phone}, #{sex}, #{idCard}, '0')")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insert(Owner owner);

    @Update("UPDATE live_user SET username=#{username}, phone=#{phone}, sex=#{sex}, " +
            "id_card=#{idCard} WHERE user_id=#{userId}")
    int update(Owner owner);

    @Delete("DELETE FROM live_user WHERE user_id = #{userId}")
    int deleteById(@Param("userId") Long userId);

    @Update("UPDATE live_user SET password=#{password} WHERE user_id=#{userId}")
    int updatePassword(@Param("userId") Long userId, @Param("password") String password);

    // 分页查询方法
    List<Owner> findByPage(@Param("offset") int offset,
                           @Param("limit") int limit,
                           @Param("keyword") String keyword);

    int countByKeyword(@Param("keyword") String keyword);
}
