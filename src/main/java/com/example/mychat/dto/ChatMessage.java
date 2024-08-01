package com.example.mychat.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

/**
 * 收到聊天消息 消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends Message {

    private Integer sender;

    private String senderName;

    private String content;

    private String sendTime;
}
