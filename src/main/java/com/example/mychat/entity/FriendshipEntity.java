package com.example.mychat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("friendships")
public class FriendshipEntity {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("mainID")
    private Integer mainID;

    @TableField("secondaryID")
    private Integer secondaryID;
}
