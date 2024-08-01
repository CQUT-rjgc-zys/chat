package com.example.mychat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收到好友申请 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyMessage extends Message {

    private String senderName; // 申请者ID

    private Integer receiver; // 被申请者ID
}
