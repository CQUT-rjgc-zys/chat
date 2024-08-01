package com.example.mychat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("friend_requests")
public class FriendRequestEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("sender")
    private Integer sender;

    @TableField("receiver")
    private Integer receiver;

    @TableField("send_time")
    private Timestamp sendTime;

    // 0：等待处理，1：已同意，2：已拒绝，3：已撤回
    @TableField("status")
    private Integer status;
}
