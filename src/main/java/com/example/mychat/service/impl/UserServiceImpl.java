package com.example.mychat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mychat.common.ErrorCode;
import com.example.mychat.dto.FriendRequestDTO;
import com.example.mychat.dto.LoginDTO;
import com.example.mychat.dto.MessageDTO;
import com.example.mychat.dto.UserDTO;
import com.example.mychat.entity.FriendRequestEntity;
import com.example.mychat.entity.FriendshipEntity;
import com.example.mychat.entity.MessageEntity;
import com.example.mychat.entity.UserEntity;
import com.example.mychat.exception.MyCustomException;
import com.example.mychat.mapper.FriendRequestMapper;
import com.example.mychat.mapper.FriendshipMapper;
import com.example.mychat.mapper.MessageMapper;
import com.example.mychat.mapper.UserMapper;
import com.example.mychat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public UserDTO login(LoginDTO loginDTO, HttpServletRequest request) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        log.info("登录用户信息: " + username + "，password: " + password);
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.eq("username", username);
        UserEntity userEntity = this.getOne(userEntityQueryWrapper);
        if (userEntity == null) {
            throw new MyCustomException(ErrorCode.LOGIN_ERROR, "不存在此用户信息");
        }
        String passwordInDb = userEntity.getPassword();
        if (!password.equals(passwordInDb)) {
            throw new MyCustomException(ErrorCode.LOGIN_ERROR, "密码错误");
        }
        userEntity.setStatus(true);
        updateById(userEntity);
        HttpSession session = request.getSession();
        session.setAttribute("username", username);
        session.setAttribute("userId", userEntity.getId());
        log.info("用户：{}登录成功", username);
        UserDTO user = new UserDTO();
        user.setId(userEntity.getId());
        user.setUsername(userEntity.getUsername());
        return user;
    }

    @Override
    public void register(UserDTO userDTO) {
        String username = userDTO.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new MyCustomException(ErrorCode.PARAM_ERROR, "不存在此用户信息");
        }
        String password = userDTO.getPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new MyCustomException(ErrorCode.PARAM_ERROR, "密码错误");
        }
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.eq("username", username);
        UserEntity userEntity = this.getOne(userEntityQueryWrapper);
        if (userEntity != null) {
            throw new MyCustomException(ErrorCode.PARAM_ERROR, "用户名已存在");
        }
        UserEntity save = new UserEntity();
        save.setUsername(username);
        save.setPassword(password);
        save.setHeadPortrait(0);
        save.setStatus(false);
        save(save);
        log.info("用户：{}注册成功", username);
    }

    @Override
    public void checkUsername(String username) {
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.eq("username", username);
        UserEntity userEntity = this.getOne(userEntityQueryWrapper);
        if (userEntity == null) {
            throw new MyCustomException(ErrorCode.PARAM_ERROR, "不存在此用户信息");
        }
    }

    @Override
    public List<UserDTO> getUserInfoByUsername(String username, HttpSession session) {
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.like("username", username);
        List<UserEntity> userEntities = this.list(userEntityQueryWrapper);
        if (userEntities.isEmpty()) {
            return Collections.emptyList();
        }
        Integer userId = (Integer) session.getAttribute("userId");
        List<UserDTO> friends = getFriends(userId);
        List<Integer> friendIds = friends.stream().map(UserDTO::getId).collect(Collectors.toList());
        return userEntities.stream()
                .filter(entity -> !friendIds.contains(entity.getId())) // 过滤掉在好友列表中的用户
                .map(entity -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(entity.getId());
                    userDTO.setUsername(entity.getUsername());
                    userDTO.setHeadPortrait(entity.getHeadPortrait());
                    userDTO.setStatus(entity.getStatus());
                    return userDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getFriendInfoByUsername(String username, HttpSession session) {
        QueryWrapper<FriendshipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mainID", session.getAttribute("userId"));
        List<FriendshipEntity> friendshipEntities = friendshipMapper.selectList(queryWrapper);

        if (friendshipEntities.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> friendIds = friendshipEntities.stream()
                .map(FriendshipEntity::getSecondaryID).collect(Collectors.toList());
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.in("id", friendIds);
        List<UserEntity> userEntities = this.list(userEntityQueryWrapper);
        return userEntities.stream()
                .filter(entity -> entity.getUsername().contains(username))
                .map(entity -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(entity.getId());
                    userDTO.setUsername(entity.getUsername());
                    userDTO.setHeadPortrait(entity.getHeadPortrait());
                    userDTO.setStatus(entity.getStatus());
                    return userDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getFriends(Integer userId) {
        QueryWrapper<FriendshipEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mainID", userId);
        List<FriendshipEntity> friendshipEntities = friendshipMapper.selectList(queryWrapper);
        if (friendshipEntities.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> friendIds = friendshipEntities.stream()
                .map(FriendshipEntity::getSecondaryID).collect(Collectors.toList());
        QueryWrapper<UserEntity> userEntityQueryWrapper = new QueryWrapper<>();
        userEntityQueryWrapper.in("id", friendIds);
        List<UserEntity> userEntities = this.list(userEntityQueryWrapper);
        return userEntities.stream().map(entity -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(entity.getId());
            userDTO.setUsername(entity.getUsername());
            userDTO.setHeadPortrait(entity.getHeadPortrait());
            userDTO.setStatus(entity.getStatus());
            return userDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public void logout(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        userMapper.updateStatusById(userId, false);
        session.invalidate();
    }

    @Override
    public void sendFriendRequest(Integer friendId, HttpSession session) {
        Integer sender = (Integer) session.getAttribute("userId");
        List<FriendRequestEntity> existInfos0 = friendRequestMapper.checkIfExist(sender, friendId);
        if (existInfos0.size() > 0) {
            for (FriendRequestEntity existInfo : existInfos0) {
                if (existInfo.getStatus() == 0) {
                    throw new MyCustomException(ErrorCode.PARAM_ERROR, "请勿重复发送请求");
                }
            }
        }
        List<FriendRequestEntity> existInfos1 = friendRequestMapper.checkIfExist(friendId, sender);
        if (existInfos1.size() > 0) {
            for (FriendRequestEntity existInfo : existInfos1) {
                // 如果对方先发送了好友请求，且还未被处理，则不产生新的申请记录，直接通过原有的申请
                if (existInfo.getStatus() == 0) {
                    throw new MyCustomException(ErrorCode.PARAM_ERROR, "对方已申请添加您为好友，请前往处理");
                }
            }
        }
        FriendRequestEntity friendRequestEntity = new FriendRequestEntity();
        friendRequestEntity.setSender(sender);
        friendRequestEntity.setReceiver(friendId);
        friendRequestEntity.setStatus(0);
        friendRequestEntity.setSendTime(new Timestamp(System.currentTimeMillis()));
        friendRequestMapper.insert(friendRequestEntity);
    }

    @Override
    public List<FriendRequestDTO> getFriendRequestsBySend(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<FriendRequestEntity> friendRequestEntities = friendRequestMapper.selectListBySenderId(userId);
        return getFriendRequestDTOS(friendRequestEntities);
    }

    @Override
    public List<FriendRequestDTO> getFriendRequestsByReceive(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<FriendRequestEntity> friendRequestEntities = friendRequestMapper.selectListByReceiverId(userId);
        return getFriendRequestDTOS(friendRequestEntities);
    }

    @Override
    public void updateFriendRequest(Integer requestId, Integer status) {
        friendRequestMapper.updateStatusById(requestId, status);
        if (status == 1) {
            FriendRequestEntity friendRequestEntity = friendRequestMapper.selectById(requestId);
            FriendshipEntity friendshipEntity1 = new FriendshipEntity();
            friendshipEntity1.setMainID(friendRequestEntity.getSender());
            friendshipEntity1.setSecondaryID(friendRequestEntity.getReceiver());
            friendshipMapper.insert(friendshipEntity1);
            FriendshipEntity friendshipEntity2 = new FriendshipEntity();
            friendshipEntity2.setMainID(friendRequestEntity.getReceiver());
            friendshipEntity2.setSecondaryID(friendRequestEntity.getSender());
            friendshipMapper.insert(friendshipEntity2);
        }
    }

    @Override
    public List<MessageDTO> getChatHistory(Integer friendId, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        List<MessageEntity> messageEntities = messageMapper.selectListByTwoUsers(userId, friendId);
        return messageEntities.stream().map(entity -> {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setId(entity.getId());
            messageDTO.setSenderID(entity.getSenderID());
            messageDTO.setReceiverID(entity.getReceiverID());
            messageDTO.setContent(entity.getContent());
            messageDTO.setSendTime(entity.getSendTime());
            return messageDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public MessageDTO sendMessage(MessageDTO messageDTO, HttpSession session) {
        MessageEntity messageEntity = new MessageEntity();
        BeanUtils.copyProperties(messageDTO, messageEntity);
        messageEntity.setSendTime(new Timestamp(System.currentTimeMillis()));
        messageMapper.insert(messageEntity);
        messageDTO.setId(messageEntity.getId());
        messageDTO.setSendTime(messageEntity.getSendTime());
        return messageDTO;
    }

    private List<FriendRequestDTO> getFriendRequestDTOS(List<FriendRequestEntity> friendRequestEntities) {
        return friendRequestEntities.stream().map(entity -> {
            FriendRequestDTO friendRequestDTO = new FriendRequestDTO();
            friendRequestDTO.setId(entity.getId());
            friendRequestDTO.setSender(entity.getSender());
            friendRequestDTO.setSenderName(userMapper.getUsernameById(entity.getSender()));
            friendRequestDTO.setReceiverName(userMapper.getUsernameById(entity.getReceiver()));
            friendRequestDTO.setSendTime(entity.getSendTime());
            friendRequestDTO.setStatus(entity.getStatus());
            return friendRequestDTO;
        }).collect(Collectors.toList());
    }
}
