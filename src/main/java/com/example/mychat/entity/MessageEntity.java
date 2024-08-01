package com.example.mychat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("messages")
public class MessageEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("senderID")
    private Integer senderID;

    @TableField("receiverID")
    private Integer receiverID;

    @TableField("content")
    private String content;

    @TableField("sendTime")
    private Timestamp sendTime;
}
