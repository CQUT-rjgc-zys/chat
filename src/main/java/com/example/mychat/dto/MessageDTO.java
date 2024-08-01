package com.example.mychat.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private int senderID;
    private int receiverID;
    private String content;
    private Timestamp sendTime;
}
