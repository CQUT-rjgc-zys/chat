package com.example.mychat.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mychat.dto.FriendRequestDTO;
import com.example.mychat.dto.LoginDTO;
import com.example.mychat.dto.MessageDTO;
import com.example.mychat.dto.UserDTO;
import com.example.mychat.entity.UserEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

public interface UserService extends IService<UserEntity> {
    UserDTO login(LoginDTO loginDTO, HttpServletRequest request);

    void register(UserDTO userDTO);

    void checkUsername(String username);

    List<UserDTO> getUserInfoByUsername(String username, HttpSession session);

    List<UserDTO> getFriendInfoByUsername(String username, HttpSession session);

    List<UserDTO> getFriends(Integer usrId);

    void logout(HttpSession session);

    void sendFriendRequest(Integer friendId, HttpSession session);

    List<FriendRequestDTO> getFriendRequestsBySend(HttpSession session);

    List<FriendRequestDTO> getFriendRequestsByReceive(HttpSession session);

    void updateFriendRequest(Integer requestId, Integer status);

    List<MessageDTO> getChatHistory(Integer friendId, HttpSession session);

    MessageDTO sendMessage(MessageDTO messageDTO, HttpSession session);
}
