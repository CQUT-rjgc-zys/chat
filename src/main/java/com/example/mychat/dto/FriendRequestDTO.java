package com.example.mychat.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class FriendRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer sender;

    private String senderName;

    private String receiverName;

    private Timestamp sendTime;

    // 0：等待处理，1：已同意，2：已拒绝，3：已撤回
    private Integer status;
}
