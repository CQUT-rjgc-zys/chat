package com.example.mychat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
}
