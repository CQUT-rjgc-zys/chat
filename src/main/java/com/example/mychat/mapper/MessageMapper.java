package com.example.mychat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mychat.entity.MessageEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {

    @Select("SELECT * FROM messages WHERE senderID IN (#{senderId}, #{receiverId}) AND receiverID IN (#{senderId}, #{receiverId}) ORDER BY sendTime")
    List<MessageEntity> selectListByTwoUsers(@Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId);
}
