package com.example.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mychat.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    @Update("update users set status = #{status} where id = #{id}")
    void updateStatusById(@Param("id") Integer id, @Param("status") Boolean status);

    @Select("select username from users where id = #{id}")
    String getUsernameById(@Param("id")Integer id);
}
