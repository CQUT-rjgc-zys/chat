package com.example.mychat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 好友申请被处理 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApplyDisposeMessage extends Message {

    private String senderName; //申请者id

    private boolean agree; //是否同意
}
