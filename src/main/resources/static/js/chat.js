var friends = null;
// 用于记录当前正在聊天的好友
var chattingWith = null;

function getChatHistory(id, headPortrait) {
    fetch('/user/getChatHistory/' + id)
       .then(response => response.json())
       .then(result => {
           if (result.code === 200) {
               var chatMessages = result.data;
               var messages = document.getElementById("chat-message-list-ul");
               messages.innerHTML = "";
               showChatHistory(chatMessages, headPortrait);
           }
       })
       .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function showChatHistory(chatMessages, headPortrait) {
    var messages = document.getElementById("chat-message-list-ul");

    chatMessages.forEach(message => {
        const li = document.createElement("li");

        var messageDiv = document.createElement("div");
        var avatarImg = document.createElement("img");
        avatarImg.classList.add("chat-avatar");
        if (headPortrait === 0) {
            avatarImg.src = "/images/photos/defaultPhoto.png"
        } else {
            avatarImg.src = combinePath(headPortrait);
        }
        messageDiv.appendChild(avatarImg);

        var messageContent = document.createElement("div");
        messageContent.textContent = message.content;

        var messageTime = document.createElement("div");
        messageTime.textContent = formatMessageTime(message.sendTime);

        var userId = localStorage.getItem("userId");
        // 根据发送者决定消息的样式
        if (message.senderID === parseInt(userId)) {
            messageDiv.classList.add("chat-message-sender");
            li.classList.add("send");
            messageContent.classList.add("chat-message-div-send");
            messageTime.classList.add("chat-time-div-send");
        } else {
            messageDiv.classList.add("chat-message-receiver");
            li.classList.add("receive");
            messageContent.classList.add("chat-message-div-receive");
            messageTime.classList.add("chat-time-div-receive");
        }

        messageDiv.appendChild(messageContent);

        li.appendChild(messageDiv);
        li.appendChild(messageTime);

        messages.appendChild(li);
    });
}

function formatMessageTime(timestamp) {
    const messageTime = new Date(timestamp);
    const now = new Date();

    // 如果是今天之内
    if (isToday(now, messageTime)) {
        return formatTime(messageTime);
    }

    // 如果是昨天之内
    const yesterday = new Date(now);
    yesterday.setDate(now.getDate() - 1);
    if (isSameDate(yesterday, messageTime)) {
        return "昨天 " + formatTime(messageTime);
    }

    // 如果是前一周内
    const oneWeekAgo = new Date(now);
    oneWeekAgo.setDate(now.getDate() - 7);
    if (messageTime >= oneWeekAgo) {
        const days = ["星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"];
        return days[messageTime.getDay()] + " " + formatTime(messageTime);
    }

    // 如果是前一年内
    const oneYearAgo = new Date(now);
    oneYearAgo.setFullYear(now.getFullYear() - 1);
    if (messageTime >= oneYearAgo) {
        return formatDate(messageTime) + " " + formatTime(messageTime);
    }

    // 如果是一年前
    return formatFullDate(messageTime) + " " + formatTime(messageTime);
}

// 检查是否是同一天
function isSameDate(date1, date2) {
    return date1.getFullYear() === date2.getFullYear() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getDate() === date2.getDate();
}

// 检查是否是今天
function isToday(date1, date2) {
    return isSameDate(date1, date2);
}

// 格式化日期为 MM/DD
function formatDate(date) {
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return month + "/" + day;
}

// 格式化完整日期为 YYYY/MM/DD
function formatFullDate(date) {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return year + "/" + month + "/" + day;
}

// 格式化时间为 HH:MM
function formatTime(date) {
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return hours + ":" + minutes;
}

function sendMessage() {
    var messageInput = document.getElementById("message-input");
    var messageContent = messageInput.value.trim(); // 获取文本区域中的消息，并去除首尾空格

    // 如果消息为空，则不执行发送操作
    if (messageContent === "") {
        alert("消息不能为空！");
        return;
    }

    var userId = localStorage.getItem("userId");

    // 构造一个消息对象，包括内容、发送者ID和发送时间
    var newMessage = {
        id: null,
        senderID: parseInt(userId),
        receiverID: chattingWith.id,
        content: messageContent,
        sendTime: null
    };

    fetch('/user/sendMessage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(newMessage)
    })
       .then(response => response.json())
       .then(result => {
            if (result.code === 200) {
                newMessage.sendTime = result.data.sendTime;
                // 在消息展示框中添加新消息
                appendMessage(newMessage);
                // 清空文本区域
                messageInput.value = "";
                // 通知对方消息已发送
                const message = {
                    type: "2",
                    sender: parseInt(userId),
                    senderName: localStorage.getItem("username"),
                    receiver: chattingWith.id,
                    content: messageContent,
                    sendTime: result.data.sendTime
                }
                socket.send(JSON.stringify(message));
            }
        })
       .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function appendMessage(message) {
    const messages = document.getElementById("chat-message-list-ul");
    const li = document.createElement("li");
    const messageDiv = document.createElement("div");
    const avatarImg = document.createElement("img");
    avatarImg.classList.add("chat-avatar");

    var headPortrait = parseInt(localStorage.getItem("headPortrait"));
    if (isNaN(headPortrait) || headPortrait === 0) {
        avatarImg.src = "/images/photos/defaultPhoto.png";
    } else {
        avatarImg.src = combinePath(headPortrait);
    }
    messageDiv.appendChild(avatarImg);

    const messageContent = document.createElement("div");
    messageContent.textContent = message.content;

    const messageTime = document.createElement("div");
    messageTime.textContent = formatMessageTime(message.sendTime);

    var userId = localStorage.getItem("userId");
    // 根据发送者决定消息的样式
    if (message.senderID === parseInt(userId)) {
        messageDiv.classList.add("chat-message-sender");
        li.classList.add("send");
        messageContent.classList.add("chat-message-div-send");
        messageTime.classList.add("chat-time-div-send");
    } else {
        messageDiv.classList.add("chat-message-receiver");
        li.classList.add("receive");
        messageContent.classList.add("chat-message-div-receive");
        messageTime.classList.add("chat-time-div-receive");
    }

    messageDiv.appendChild(messageContent);

    li.appendChild(messageDiv);
    li.appendChild(messageTime);

    messages.appendChild(li);
}

function populateFriendList() {
    fetch('/user/getFriends')
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                friends = result.data;
                showFriends(friends)
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function showFriends(friends) {
    // 更新在线好友数量
    var onlineCountSpan = document.getElementById("online-count");

    if (friends.length === 0) {
        var li = document.createElement("li");
        li.textContent = "暂无好友";
        li.classList.add("data-empty")
        document.getElementById("friend-list-ul").appendChild(li);
        onlineCountSpan.textContent = 0;
        return;
    }

    var onlineCount = 0; // 在线好友计数器
    var friendList = document.getElementById("friend-list-ul");
    friends.forEach(function (friend) {
        var li = document.createElement("li");
        li.classList.add("friend-item");

        li.dataset.friendId = friend.id;

        var infoDiv = document.createElement("div");
        infoDiv.classList.add("friend-info");

        var avatarImg = document.createElement("img");
        avatarImg.classList.add("friend-avatar");
        if (friend.headPortrait === 0) {
            avatarImg.src = "/images/photos/defaultPhoto.png"
        } else {
            avatarImg.src = combinePath(friend.headPortrait);
        }
        avatarImg.alt = friend.username + " Avatar";
        infoDiv.appendChild(avatarImg);

        var nameDiv = document.createElement("div");
        nameDiv.classList.add("friend-name");
        nameDiv.textContent = friend.username;
        infoDiv.appendChild(nameDiv);

        li.appendChild(infoDiv);

        var statusSpan = document.createElement("span");
        statusSpan.classList.add("friend-status");
        statusSpan.textContent = friend.status ? "在线" : "离线";
        statusSpan.style.color = friend.status ? "green" : "red";
        li.appendChild(statusSpan);

        li.addEventListener('click', function () {
            chattingWith = friend;
            var username = this.children[0].textContent
            document.getElementById('chat').style.display = 'flex';
            document.getElementById('chat-with-user').textContent = username;
            getChatHistory(friend.id, friend.headPortrait);
        })

        friendList.appendChild(li);

        // 如果好友在线，增加在线好友计数
        if (friend.status) {
            onlineCount++;
        }
    });

    onlineCountSpan.textContent = onlineCount;
}

function combinePath(index) {
    var src = "/images/photos/photo.png";
    // 获取文件名和扩展名
    var filename = src.substring(src.lastIndexOf("/") + 1, src.lastIndexOf("."));
    var extension = src.substring(src.lastIndexOf("."));

    // 拼接新路径
    return src.substring(0, src.lastIndexOf("/")) + "/" + filename + index + extension;
}

function showLoginUserInfo() {
    var username = document.getElementById("username");
    username.textContent = localStorage.getItem("username");
}

// 在页面加载完成后填充好友列表
window.onload = function () {
    showLoginUserInfo();
    populateFriendList();
};

window.onbeforeunload = function () {
    fetch('/user/logout')
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                localStorage.clear();
                console.log("Logout success.");
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        })
}

var backImage = document.getElementById("backImage");
var originalSrc = backImage.src;

// 存储新的 src 属性值
var hoverSrc = "/images/backHover.png"; // 替换成你想要的新图片路径

// 监听鼠标悬停事件
backImage.addEventListener("mouseenter", function () {
    backImage.src = hoverSrc; // 鼠标悬停时更换图片
});

// 监听鼠标离开事件
backImage.addEventListener("mouseleave", function () {
    backImage.src = originalSrc; // 鼠标离开时恢复原始图片
});

// 监听点击事件
backImage.addEventListener("click", function () {
    chattingWith = null;
    document.getElementById('chat').style.display = 'none';
})

// 显示对话框
function openDialog(dialogType) {
    var overlay = document.getElementById(dialogType + "-overlay");
    var dialog = document.getElementById(dialogType + "-dialog");
    if (dialogType === "search") {
        var searchInput = document.getElementById("search-input");
        searchInput.value = "";
    } else if (dialogType === "apply") {
        displayApplyInfos();
    }
    overlay.style.display = "block";
    dialog.style.display = "block";
}

// 关闭对话框
function closeDialog(dialogType) {
    var overlay = document.getElementById(dialogType + "-overlay");
    var dialog = document.getElementById(dialogType + "-dialog");
    overlay.style.display = "none";
    dialog.style.display = "none";
    document.getElementById('searchUser-list-ul').innerHTML = ''; // 清空之前的搜索结果
    document.getElementsByClassName('applyInfo-list-ul').innerHTML = ''; // 清空之前的搜索结果
    document.getElementById('message-list-ul').innerHTML = ''; // 清空之前的搜索结果

    var friendList = document.getElementById("friend-list-ul");
    friendList.innerHTML = ''; // 清空好友列表
    // 刷新好友列表
    populateFriendList();
}

function confirmSearchInFriends() {
    var username = document.getElementById('search-input').value.trim();
    if (username === '') {
        alert('请输入用户名！');
        return;
    }

    fetch('/user/getFriendInfoByUsername?username=' + username)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                displaySearchFriendsResults(result.data);
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function confirmSearchInUsers() {
    var username = document.getElementById('search-input').value.trim();
    if (username === '') {
        alert('请输入用户名！');
        return;
    }

    fetch('/user/getUserInfoByUsername?username=' + username)
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                displaySearchAllResults(result.data);
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function displaySearchAllResults(users) {
    var userListContainer = document.getElementById('searchUser-list-ul');
    userListContainer.innerHTML = ''; // 清空之前的搜索结果

    if (users.length === 0) {
        userListContainer.innerHTML = '<li>未找到相关用户</li>';
        return;
    }

    users.forEach(user => {
        var li = document.createElement('li');
        li.classList.add("searchUser-item");

        var infoDiv = document.createElement("div");
        infoDiv.classList.add("user-info");

        var avatarImg = document.createElement("img");
        avatarImg.classList.add("user-avatar");
        if (user.headPortrait === 0) {
            avatarImg.src = "/images/photos/defaultPhoto.png"
        } else {
            avatarImg.src = combinePath(user.headPortrait);
        }
        avatarImg.alt = user.username + " Avatar";
        infoDiv.appendChild(avatarImg);

        var nameDiv = document.createElement("div");
        nameDiv.classList.add("user-name");
        nameDiv.textContent = user.username;
        infoDiv.appendChild(nameDiv);

        li.appendChild(infoDiv);

        var statusSpan = document.createElement("span");
        statusSpan.classList.add("user-status");
        statusSpan.textContent = user.status ? "在线" : "离线";
        statusSpan.style.color = user.status ? "green" : "red";
        li.appendChild(statusSpan);

        var addFriendBtn = document.createElement("button");
        addFriendBtn.classList.add("add-friend-button");
        addFriendBtn.textContent = "添加好友";
        // 设置好友ID
        addFriendBtn.addEventListener('click', function () {
            var send = confirm("是否发送好友申请？");
            if (send) {
                sendApplyInfo(user.id);
            }
        })
        li.appendChild(addFriendBtn);

        userListContainer.appendChild(li);
    });
}

function sendApplyInfo(userId) {
    fetch('/user/sendFriendRequest/' + userId, {
        method: 'POST',
        credentials: 'same-origin', // 发送凭证
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({}) // 发送空的 JSON 数据
    })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                showMessage('clue', '好友申请已发送，等待对方确认！')
                // 给对方推送一条消息
                const message = {
                    type: 3, // 好友上线消息类型，根据实际情况填写
                    sender: localStorage.getItem("userId"), // 发送者
                    receiver: userId // 接收者
                };
                socket.send(JSON.stringify(message));
            } else if (result.code === 401) {
                showMessage('warning', result.message)
            } else {
                showMessage('warning', '发送好友申请失败，请稍后再试！')
            }
        }).catch(error => {
        console.error('发送申请失败:', error);
    });
}

function displaySearchFriendsResults(friends) {
    var userListContainer = document.getElementById('searchUser-list-ul');
    userListContainer.innerHTML = ''; // 清空之前的搜索结果

    if (friends.length === 0) {
        userListContainer.innerHTML = '<li>未找到相关用户</li>';
        return;
    }

    friends.forEach(friend => {
        var li = document.createElement('li');
        li.classList.add("searchUser-item");

        var avatarImg = document.createElement("img");
        avatarImg.classList.add("friend-avatar");
        if (friend.headPortrait === 0) {
            avatarImg.src = "/images/photos/defaultPhoto.png"
        } else {
            avatarImg.src = combinePath(friend.headPortrait);
        }
        avatarImg.alt = friend.username + " Avatar";
        li.appendChild(avatarImg);

        var nameDiv = document.createElement("div");
        nameDiv.classList.add("friend-name");
        nameDiv.textContent = friend.username;
        li.appendChild(nameDiv);

        var statusSpan = document.createElement("span");
        statusSpan.classList.add("friend-status");
        statusSpan.textContent = friend.status ? "在线" : "离线";
        statusSpan.style.color = friend.status ? "green" : "red";
        li.appendChild(statusSpan);

        userListContainer.appendChild(li);
    });
}

function displayApplyInfos() {
    // 展示已发出的申请
    fetch('/user/getFriendRequestsBySend')
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                displaySendApplyInfosResults(result.data);
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });

    // 展示已收到的申请
    fetch('/user/getFriendRequestsByReceive')
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                displayReceiveApplyInfosResults(result.data);
            }
        })
        .catch(error => {
            console.error('Error fetching data:', error);
        });
}

function displaySendApplyInfosResults(applyInfos) {
    var waitingCount = 0; // 等待处理的申请数量
    var applyInfoListContainer = document.getElementById('applyInfo-list-ul-send');
    applyInfoListContainer.innerHTML = ''; // 清空之前的搜索结果
    applyInfos.forEach(applyInfo => {
        var li = document.createElement('li');
        li.classList.add("apply-message-send-item");

        li.dataset.applyInfoId = applyInfo.id;

        var applyInfoDiv = document.createElement("div");
        applyInfoDiv.classList.add("apply-message-send-item-first");

        var nameDiv = document.createElement("div");
        nameDiv.textContent = applyInfo.receiverName;
        nameDiv.classList.add('name-div')
        applyInfoDiv.appendChild(nameDiv);

        var date = new Date(applyInfo.sendTime);

        // 格式化日期
        var formattedDate = `${date.getFullYear()}/${padZero(date.getMonth() + 1)}/${padZero(date.getDate())}`;
        var formattedTime = `${padZero(date.getHours())}:${padZero(date.getMinutes())}`;

        // 创建时间显示的 div 元素
        var timeDiv = document.createElement("div");
        timeDiv.textContent = `${formattedDate} ${formattedTime}`;
        timeDiv.classList.add('time-div')
        applyInfoDiv.appendChild(timeDiv);

        var statusDiv = document.createElement("div");
        statusDiv.classList.add('status-div')

        if (applyInfo.status === 0) {
            statusDiv.textContent = "等待处理";
            statusDiv.style.color = "#007bff";
            waitingCount++;
        } else if (applyInfo.status === 1) {
            statusDiv.textContent = "已同意";
            statusDiv.style.color = "green";
        } else if (applyInfo.status === 2) {
            statusDiv.textContent = "已拒绝";
            statusDiv.style.color = "red";
        } else {
            statusDiv.textContent = "已撤回";
            statusDiv.style.color = "grey";
        }

        applyInfoDiv.appendChild(statusDiv);

        li.appendChild(applyInfoDiv);

        if (applyInfo.status === 0) {
            var revokeBtn = document.createElement("button");
            revokeBtn.classList.add("revoke-button");
            revokeBtn.textContent = "撤回";
            revokeBtn.addEventListener('click', function () {
                fetch('/user/updateFriendRequest?requestId=' + applyInfo.id + '&status=3', {
                    method: 'PUT'
                })
                    .then(response => response.json())
                    .then(result => {
                        if (result.code === 200) {
                            // 刷新申请列表
                            displayApplyInfos();
                        }
                    })
                    .catch(error => {
                        console.error('撤回申请失败:', error);
                    });
            });
            li.appendChild(revokeBtn);
        }
        applyInfoListContainer.appendChild(li);
    })
    document.getElementById('apply-message-send-title').innerHTML = `我发送的（还有<span style="color: #007bff;"> ${waitingCount} </span>条申请等待被处理）`;
}

function displayReceiveApplyInfosResults(applyInfos) {
    var waitingCount = 0; // 等待处理的申请数量
    var applyInfoListContainer = document.getElementById('applyInfo-list-ul-receive');
    applyInfoListContainer.innerHTML = ''; // 清空之前的搜索结果
    applyInfos.forEach(applyInfo => {
        var li = document.createElement('li');
        li.classList.add("apply-message-receive-item");

        li.dataset.applyInfoId = applyInfo.id;

        var applyInfoDiv = document.createElement("div");
        applyInfoDiv.classList.add("apply-message-receive-item-first");

        var nameDiv = document.createElement("div");
        nameDiv.textContent = applyInfo.senderName;
        nameDiv.classList.add('name-div')
        applyInfoDiv.appendChild(nameDiv);

        var date = new Date(applyInfo.sendTime);

        // 格式化日期
        var formattedDate = `${date.getFullYear()}/${padZero(date.getMonth() + 1)}/${padZero(date.getDate())}`;
        var formattedTime = `${padZero(date.getHours())}:${padZero(date.getMinutes())}`;

        // 创建时间显示的 div 元素
        var timeDiv = document.createElement("div");
        timeDiv.textContent = `${formattedDate} ${formattedTime}`;
        timeDiv.classList.add('time-div')
        applyInfoDiv.appendChild(timeDiv);

        var statusDiv = document.createElement("div");
        statusDiv.classList.add('status-div')

        if (applyInfo.status === 0) {
            statusDiv.textContent = "等待处理";
            statusDiv.style.color = "#007bff";
            waitingCount++;
        } else if (applyInfo.status === 1) {
            statusDiv.textContent = "已同意";
            statusDiv.style.color = "green";
        } else if (applyInfo.status === 2) {
            statusDiv.textContent = "已拒绝";
            statusDiv.style.color = "red";
        } else {
            statusDiv.textContent = "已撤回";
            statusDiv.style.color = "grey";
        }

        applyInfoDiv.appendChild(statusDiv);

        li.appendChild(applyInfoDiv);

        if (applyInfo.status === 0) {
            var buttonDiv = document.createElement("div");
            buttonDiv.classList.add('button-div')

            var agreeBtn = document.createElement("button");
            agreeBtn.classList.add("dispose-button")
            agreeBtn.textContent = "√";
            agreeBtn.addEventListener('click', function () {
                fetch('/user/updateFriendRequest?requestId=' + applyInfo.id + '&status=1', {
                    method: 'PUT'
                })
                    .then(response => response.json())
                    .then(result => {
                        if (result.code === 200) {
                            // 刷新申请列表
                            displayApplyInfos();
                            // 给对方推送一条消息
                            const message = {
                                type: 4, // 好友申请处理结果消息类型，根据实际情况填写
                                senderName: localStorage.getItem("username"), // 发送者姓名
                                receiver: applyInfo.sender, // 接收者
                                agree: true // 同意
                            };
                            socket.send(JSON.stringify(message));
                        }
                    })
                    .catch(error => {
                        console.error('同意申请失败:', error);
                    });
            });
            buttonDiv.appendChild(agreeBtn);

            var rejectBtn = document.createElement("button");
            rejectBtn.classList.add("dispose-button")
            rejectBtn.textContent = "×";
            rejectBtn.addEventListener('click', function () {
                fetch('/user/updateFriendRequest?requestId=' + applyInfo.id + '&status=2', {
                    method: 'PUT'
                })
                    .then(response => response.json())
                    .then(result => {
                        if (result.code === 200) {
                            // 刷新申请列表
                            displayApplyInfos();
                            // 给对方推送一条消息
                            const message = {
                                type: 4, // 好友申请处理结果消息类型，根据实际情况填写
                                senderName: localStorage.getItem("username"), // 发送者姓名
                                receiver: applyInfo.sender, // 接收者
                                agree: false // 同意
                            };
                            socket.send(JSON.stringify(message));
                        }
                    })
                    .catch(error => {
                        console.error('拒绝申请失败:', error);
                    });
            });
            buttonDiv.appendChild(rejectBtn);

            li.appendChild(buttonDiv);
        }
        applyInfoListContainer.appendChild(li);
    })

    document.getElementById('apply-message-receive-title').innerHTML = `我收到的（还有<span style="color: #007bff;"> ${waitingCount} </span>条申请等待处理）`;
}

const socket = new WebSocket("ws://47.115.226.7:8080/websocket/" + localStorage.getItem("userId"));

socket.onopen = function (event) {
    console.log("WebSocket connection established.");
};

socket.onmessage = function (event) {
    const message = JSON.parse(event.data); // 解析 JSON 字符串为对象
    // 处理不同类型的消息
    switch (message.type) {
        case 0:
            // 好友上线消息
            var messageContent = "您的好友：" + message.username + "上线了";
            showMessage('clue', messageContent);
            var friendListUl = document.getElementById('friend-list-ul');
            var friendItems = friendListUl.getElementsByClassName('friend-item');

            // 遍历好友元素，查找并修改对应好友的在线状态
            for (var i = 0; i < friendItems.length; i++) {
                var friendId = friendItems[i].dataset.friendId; // 获取好友ID
                if (friendId === message.userId.toString()) { // 根据好友ID匹配
                    var statusSpan = friendItems[i].getElementsByClassName('friend-status')[0];
                    statusSpan.textContent = "在线";
                    statusSpan.style.color = "green";
                    break; // 找到好友后，结束循环
                }
            }
            // 更新在线好友数量
            var onlineCountSpan = document.getElementById("online-count");
            var oldOnlineCount = parseInt(onlineCountSpan.textContent);
            var onlineCount = oldOnlineCount + 1;
            onlineCountSpan.textContent = onlineCount;
            break;
        case 1:
            // 好友下线消息
            var friendListUl = document.getElementById('friend-list-ul');
            var friendItems = friendListUl.getElementsByClassName('friend-item');

            // 遍历好友元素，查找并修改对应好友的在线状态
            for (var i = 0; i < friendItems.length; i++) {
                var friendId = friendItems[i].dataset.friendId; // 获取好友ID
                if (friendId === message.userId.toString()) { // 根据好友ID匹配
                    var statusSpan = friendItems[i].getElementsByClassName('friend-status')[0];
                    statusSpan.textContent = "离线";
                    statusSpan.style.color = "red";
                    break; // 找到好友后，结束循环
                }
            }
            // 更新在线好友数量
            var onlineCountSpan = document.getElementById("online-count");
            var oldOnlineCount = parseInt(onlineCountSpan.textContent);
            var onlineCount = oldOnlineCount - 1;
            onlineCountSpan.textContent = onlineCount;
            break;
        case 2:
            // 聊天消息
            if (chattingWith.id === message.sender) {
                appendMessage(message)
            } else {
                var messageContent = "用户：" + message.senderName + "给您发送了一条消息";
                showMessage('clue', messageContent);
            }
            break;
        case 3:
            // 好友申请消息
            var messageContent = "用户：" + message.senderName + "向您发送了好友申请";
            showMessage('clue', messageContent);
            break;
        case 4:
            // 申请处理结果消息
            var messageContent = null;
            if (message.agree === true) {
                messageContent = "用户：" + message.senderName + "同意了您的好友请求，快去找他聊天吧！";
            } else {
                messageContent = "用户：" + message.senderName + "拒绝了您的好友请求";
            }
            showMessage('clue', messageContent);
            break;
        default:
            console.log("未知类型消息：" + message.content);
            break;
    }
};

function showMessage(type, message) {
    var messageIcon = document.getElementById('message-popup-icon');
    if (type === 'clue') {
        messageIcon.src = '/images/clue.png';
    } else if (type === 'warning') {
        messageIcon.src = '/images/warning.png';
    }
    var messagePopup = document.getElementById('message-popup');
    var messagePopupContent = document.getElementById('message-popup-content');

    // 显示消息
    messagePopupContent.textContent = message;
    messagePopup.style.display = 'flex'; // 显示消息框

    // 消息自动消失
    setTimeout(function () {
        messagePopup.style.display = 'none'; // 隐藏消息框
    }, 3000); // 3 秒后隐藏消息框
}

// 补零函数，确保月份和日期在不足两位数时补零
function padZero(num) {
    return num.toString().padStart(2, '0');
}
