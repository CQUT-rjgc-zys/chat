package com.example.mychat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class FriendshipDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private int id;
    private int mainID;
    private int secondaryID;
}
