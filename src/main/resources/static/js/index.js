document.getElementById('switchToRegister').addEventListener('click', function() {
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('registerContainer').style.display = 'block';
});

document.getElementById('switchToLogin').addEventListener('click', function() {
    document.getElementById('registerContainer').style.display = 'none';
    document.getElementById('loginContainer').style.display = 'block';
});

// 检查用户名是否已存在
document.getElementById('username').addEventListener('blur', function() {
    var username = this.value;
    if (username.trim() !== '') {
        fetch('/user/checkUsername?username=' + username) // 将用户名编码后拼接到 URL 中
            .then(response => response.json())
            .then(result => {
                if (result.code === 401) {
                    displayError('usernameTooltip', result.message);
                } else {
                    hideError('usernameTooltip');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                displayError('usernameTooltip', '网络错误，请稍后再试');
            });
    } else {
        hideError('usernameTooltip')
    }
});

// 登录表单提交
document.getElementById('loginForm').addEventListener('submit', function(event) {
    event.preventDefault(); // 阻止表单提交默认行为

    var username = document.getElementById('username').value;
    var password = document.getElementById('password').value;

    var formData = {
        username: username,
        password: password
    }

    fetch('/user/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                localStorage.setItem('userId', result.data.id);
                localStorage.setItem('username', username);
                window.location.href = '/chat';
            } else {
                displayError('passwordTooltip', result.message);
            }
        })
        .catch(error => {
            console.error('发生错误:', error);
            displayError('网络错误，请稍后再试。');
        });
});

// 注册表单提交
document.getElementById('registrationForm').addEventListener('submit', function(event) {
    event.preventDefault(); // 阻止表单提交默认行为

    var username = document.getElementById('registerUsername');
    if (username.value.trim() !== '') {
        if (username.value.length > 10) {
            displayError('registerUsernameTooltip', '用户名长度不能超过10个字符');
            username.value = '';
            return;
        } else {
            hideError('registerUsernameTooltip');
        }
    }
    var password = document.getElementById('registerPassword');
    var confirmPassword = document.getElementById('confirmPassword');

    if (password.value !== confirmPassword.value) {
        displayError('confirmPasswordTooltip', '两次输入的密码不一致');
        confirmPassword.value = '';
        return;
    }

    var formData = {
        username: username.value,
        password: password.value
    }

    fetch('/user/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => response.json())
        .then(result => {
            if (result.code === 200) {
                hideError('registerUsernameTooltip');
                hideError('registerPasswordTooltip');
                hideError('confirmPasswordTooltip');
                alert('注册成功，请登录');
                document.getElementById('registerContainer').style.display = 'none';
                document.getElementById('loginContainer').style.display = 'block';
                username.value = '';
                password.value = '';
                confirmPassword.value = '';
            } else {
                displayError('passwordError', result.message);
            }
        })
        .catch(function(error) {
            console.error('发生错误:', error);
            displayError('网络错误，请稍后再试。');
        });
});

function displayError(elementId, message) {
    const errorDiv = document.getElementById(elementId);
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}

// 提示信息隐藏函数
function hideError(elementId) {
    const errorDiv = document.getElementById(elementId);
    errorDiv.style.display = 'none';
}
