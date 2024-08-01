package com.example.mychat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友上线 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OnlineMessage extends Message {

    private Integer userId; // 登录用户id

    private String username; // 登录用户名
}
