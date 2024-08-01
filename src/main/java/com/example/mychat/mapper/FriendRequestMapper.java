package com.example.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mychat.entity.FriendRequestEntity;
import com.example.mychat.entity.FriendshipEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FriendRequestMapper extends BaseMapper<FriendRequestEntity> {

    @Select("SELECT * FROM friend_requests WHERE sender = #{sender} AND receiver = #{receiver}")
    List<FriendRequestEntity> checkIfExist(@Param("sender") Integer sender, @Param("receiver") Integer receiver);

    @Select("SELECT * FROM friend_requests WHERE sender = #{userId} ORDER BY status, send_time DESC")
    List<FriendRequestEntity> selectListBySenderId(@Param("userId") Integer userId);

    @Select("SELECT * FROM friend_requests WHERE receiver = #{userId} ORDER BY status, send_time DESC")
    List<FriendRequestEntity> selectListByReceiverId(@Param("userId") Integer userId);

    @Update("UPDATE friend_requests SET status = #{status} WHERE id = #{requestId}")
    void updateStatusById(@Param("requestId") Integer requestId, @Param("status") Integer status);
}
