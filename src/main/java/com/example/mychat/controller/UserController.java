package com.example.mychat.controller;

import com.example.mychat.dto.FriendRequestDTO;
import com.example.mychat.dto.LoginDTO;
import com.example.mychat.dto.MessageDTO;
import com.example.mychat.dto.UserDTO;
import com.example.mychat.result.CommonResult;
import com.example.mychat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/getFriends")
    public CommonResult<List<UserDTO>> getFriends(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<UserDTO> friends = userService.getFriends(userId);
        return CommonResult.ok(friends);
    }

    @GetMapping("/getFriendInfoByUsername")
    public CommonResult<List<UserDTO>> getFriendInfoByUsername(@RequestParam("username") String username, HttpSession session) {
        List<UserDTO> friendInfo = userService.getFriendInfoByUsername(username, session);
        return CommonResult.ok(friendInfo);
    }

    @GetMapping("/getUserInfoByUsername")
    public CommonResult<List<UserDTO>> getUserInfoByUsername(@RequestParam("username") String username, HttpSession session) {
        List<UserDTO> userDTOList = userService.getUserInfoByUsername(username, session);
        return CommonResult.ok(userDTOList);
    }

    @PostMapping("/register")
    public CommonResult<Void> register(@RequestBody UserDTO userDTO) {
        userService.register(userDTO);
        return CommonResult.ok(null);
    }

    @PostMapping("/login")
    public CommonResult<UserDTO> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        UserDTO user = userService.login(loginDTO, request);
        return CommonResult.ok(user);
    }

    @GetMapping("/checkUsername")
    public CommonResult<Void> checkUsername(@RequestParam String username) {
        userService.checkUsername(username);
        return CommonResult.ok(null);
    }

    @PostMapping("/sendFriendRequest/{friendId}")
    public CommonResult<Void> sendFriendRequest(@PathVariable("friendId") Integer friendId, HttpSession session) {
        userService.sendFriendRequest(friendId, session);
        return CommonResult.ok(null);
    }

    @GetMapping("/getFriendRequestsBySend")
    public CommonResult<List<FriendRequestDTO>> getFriendRequestsBySend(HttpSession session) {
        List<FriendRequestDTO> friendRequests = userService.getFriendRequestsBySend(session);
        return CommonResult.ok(friendRequests);
    }

    @GetMapping("/getFriendRequestsByReceive")
    public CommonResult<List<FriendRequestDTO>> getFriendRequestsByReceive(HttpSession session) {
        List<FriendRequestDTO> friendRequests = userService.getFriendRequestsByReceive(session);
        return CommonResult.ok(friendRequests);
    }

    @PutMapping("/updateFriendRequest")
    public CommonResult<Void> updateFriendRequest(@RequestParam("requestId") Integer requestId, @RequestParam("status") Integer status) {
        userService.updateFriendRequest(requestId, status);
        return CommonResult.ok(null);
    }

    @GetMapping("/getChatHistory/{friendId}")
    public CommonResult<List<MessageDTO>> getChatHistory(@PathVariable("friendId") Integer friendId, HttpSession session) {
        List<MessageDTO> chatHistory = userService.getChatHistory(friendId, session);
        return CommonResult.ok(chatHistory);
    }

    @PostMapping("/sendMessage")
    public CommonResult<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO, HttpSession session) {
        MessageDTO message = userService.sendMessage(messageDTO, session);
        return CommonResult.ok(message);
    }

    @GetMapping("/logout")
    public void logout(HttpSession session) {
        userService.logout(session);
    }
}
