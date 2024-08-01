package com.example.mychat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友下线 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OfflineMessage extends Message {

    private Integer userId; // 下线用户的id

}
