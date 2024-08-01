package com.example.mychat.components;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.mychat.common.ErrorCode;
import com.example.mychat.dto.*;
import com.example.mychat.exception.MyCustomException;
import com.example.mychat.mapper.UserMapper;
import com.example.mychat.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 * 注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 */
@Component
@ServerEndpoint("/websocket/{userId}")
@Slf4j
public class WebSocket {

    @Autowired
    private static UserService userService;

    @Autowired
    private static UserMapper userMapper;

    @Autowired
    public void setUserService(UserService userService) {
        WebSocket.userService = userService;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        WebSocket.userMapper = userMapper;
    }

    private static ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocket>> userwebSocketMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Integer, Integer> count = new ConcurrentHashMap<>();

    private Integer userId;

    /*
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 连接建立成功调用的方法
     *
     * @param session 可选的参数。session为与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        log.info("用户：" + userId + "上线了");
        List<UserDTO> friends = userService.getFriends(userId);
        for (UserDTO friend : friends) {
            if (isUserOnline(friend.getId())) {
                OnlineMessage message = new OnlineMessage();
                message.setType(0);
                message.setUserId(userId);
                message.setUsername(userMapper.getUsernameById(userId));
                pushMessageToFriend(friend.getId(), message);
            }
        }
        this.session = session;
        this.userId = userId;

        if (!exitUser(userId)) {
            initUserInfo(userId);
        } else {
            CopyOnWriteArraySet<WebSocket> webSocketSet = getUserSocketSet(userId);
            webSocketSet.add(this);
            userCountIncrease(userId);
        }
        log.info("有新连接加入！当前在线人数为" + getCurrUserCount(userId));
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        CopyOnWriteArraySet<WebSocket> webSocketTestSet = userwebSocketMap.get(userId);
        List<UserDTO> friends = userService.getFriends(userId);
        for (UserDTO friend : friends) {
            if (isUserOnline(friend.getId())) {
                OfflineMessage message = new OfflineMessage();
                message.setType(1);
                message.setUserId(userId);
                pushMessageToFriend(friend.getId(), message);
            }
        }
        //从set中删除
        webSocketTestSet.remove(this);
        //在线数减1
        userCountDecrement(userId);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     * @param session 可选的参数
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        // 解析消息
        JSONObject jsonMessage = JSON.parseObject(message);
        int messageType = jsonMessage.getInteger("type");

        String jsonString = null;
        WebSocket webSocket = null;
        // 判断消息类型
        if (messageType == 2) { // 收到聊天消息
            int sender = jsonMessage.getInteger("sender");
            String senderName = jsonMessage.getString("senderName");
            int receiver = jsonMessage.getInteger("receiver");
            String sendTime = jsonMessage.getString("sendTime");
            String content = jsonMessage.getString("content");
            CopyOnWriteArraySet<WebSocket> webSocketSet = userwebSocketMap.get(receiver);
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(2);
            chatMessage.setSender(sender);
            chatMessage.setSenderName(senderName);
            chatMessage.setContent(content);
            chatMessage.setSendTime(sendTime);
            jsonString = JSON.toJSONString(chatMessage);
            // 发送消息
            if (webSocketSet != null) {
                for (WebSocket item : webSocketSet) {
                    webSocket = item;
                }
            }
        } else if (messageType == 3) { // 收到好友申请消息
            // 获取消息内容
            int sender = jsonMessage.getInteger("sender");
            String senderName = userMapper.getUsernameById(sender);

            int receiver = jsonMessage.getInteger("receiver");

            // 在这里实现你的逻辑，比如根据 receiverId 找到对应的 WebSocket 连接集合
            CopyOnWriteArraySet<WebSocket> webSocketSet = userwebSocketMap.get(receiver);
            ApplyMessage applyMessage = new ApplyMessage();
            applyMessage.setType(3);
            applyMessage.setSenderName(senderName);
            jsonString = JSON.toJSONString(applyMessage);
            // 发送消息
            if (webSocketSet != null) {
                for (WebSocket item : webSocketSet) {
                    webSocket = item;
                }
            }
        } else if (messageType == 4) { // 收到好友同意消息
            int receiver = jsonMessage.getInteger("receiver");
            String senderName = jsonMessage.getString("senderName");
            boolean agree = jsonMessage.getBoolean("agree");
            CopyOnWriteArraySet<WebSocket> webSocketSet = userwebSocketMap.get(receiver);
            ApplyDisposeMessage applyDisposeMessage = new ApplyDisposeMessage();
            applyDisposeMessage.setType(4);
            applyDisposeMessage.setSenderName(senderName);
            applyDisposeMessage.setAgree(agree);
            jsonString = JSON.toJSONString(applyDisposeMessage);
            // 发送消息
            if (webSocketSet != null) {
                for (WebSocket item : webSocketSet) {
                    webSocket = item;
                }
            }
        }
        try {
            if (webSocket != null) {
                webSocket.sendMessage(jsonString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 发生错误时调用
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }

    // 判断用户是否在线
    private boolean isUserOnline(Integer userId) {
        return userwebSocketMap.containsKey(userId);
    }

    /**
     * 将消息推送给好友
     */
    private void pushMessageToFriend(Integer friendId, Message message) {
        Set<WebSocket> webSocketSet = userwebSocketMap.get(friendId);
        if (webSocketSet != null) {
            for (WebSocket webSocket : webSocketSet) {
                try {
                    String messageJson = JSON.toJSONString(message);
                    webSocket.session.getBasicRemote().sendText(messageJson);
                } catch (IOException e) {
                    log.error("向好友" + friendId + "发送消息失败：" + e.getMessage());
                    throw new MyCustomException(ErrorCode.SYSTEM_ERROR, "向好友" + friendId + "发送消息失败：" + e.getMessage());
                }
            }
        }
    }

    /**
     * 发送消息
     */
    public void sendMessage(String message) throws IOException {
        log.info("服务端推送" + userId + "的消息:" + message);
        this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 我是在有代办消息时 调用此接口 向指定用户发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String userId, String message) throws IOException {
        System.out.println("服务端推送" + userId + "的消息:" + message);
        CopyOnWriteArraySet<WebSocket> webSocketSet = userwebSocketMap.get(userId);
        //群发消息
        for (WebSocket item : webSocketSet) {
            try {
                item.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }


    public boolean exitUser(Integer userId) {
        return userwebSocketMap.containsKey(userId);
    }

    public CopyOnWriteArraySet<WebSocket> getUserSocketSet(Integer userId) {
        return userwebSocketMap.get(userId);
    }

    public void userCountIncrease(Integer userId) {
        if (count.containsKey(userId)) {
            count.put(userId, count.get(userId) + 1);
        }
    }


    public void userCountDecrement(Integer userId) {
        if (count.containsKey(userId)) {
            count.put(userId, count.get(userId) - 1);
        }
    }

    public void removeUserCount(Integer userId) {
        count.remove(userId);
    }

    public Integer getCurrUserCount(Integer userId) {
        return count.get(userId);
    }

    private void initUserInfo(Integer userId) {
        CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();
        webSocketSet.add(this);
        userwebSocketMap.put(userId, webSocketSet);
        count.put(userId, 1);
    }

}

