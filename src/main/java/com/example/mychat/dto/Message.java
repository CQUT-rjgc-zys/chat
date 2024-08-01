package com.example.mychat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    // 0：好友上线消息，1：好友下线消息，2：聊天消息，3：收到好友申请消息，4：申请通过消息
    private int type;
}
