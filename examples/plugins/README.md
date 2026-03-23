# Spring Security Boot 示例插件

本目录包含 Spring Security Boot 框架的示例插件实现，用于演示如何开发自定义认证插件。

## 目录结构

```
examples/plugins/
├── README.md                    # 本文件
└── sms-auth-plugin/            # 短信验证码认证插件示例
    ├── src/main/java/
    │   └── com/example/security/plugin/sms/
    │       ├── SmsAuthenticationPlugin.java      # 插件主类
    │       ├── SmsAuthenticationProvider.java    # 认证提供者
    │       ├── SmsAuthenticationToken.java       # 认证令牌
    │       ├── SmsVerifyCodeService.java         # 验证码服务接口
    │       └── config/
    │           ├── SmsAuthenticationConfig.java  # 自动配置
    │           └── SmsProperties.java            # 配置属性
    └── src/test/java/
        └── com/example/security/plugin/sms/
            ├── SmsAuthenticationPluginTest.java
            └── SmsAuthenticationProviderTest.java
```

## 短信认证插件 (sms-auth-plugin)

### 功能说明

该插件演示了如何实现一个完整的短信验证码认证插件，包括：

- 自定义 `AuthenticationToken` 实现
- 自定义 `AuthenticationProvider` 实现
- 插件主类实现 `AuthenticationPlugin` 接口
- Spring Boot 自动配置
- 单元测试示例

### 如何使用

1. **复制代码到项目**

将 `sms-auth-plugin` 目录下的代码复制到你的项目中，调整包名。

2. **实现验证码服务**

```java
@Service
public class MySmsVerifyCodeService implements SmsVerifyCodeService {

    private final RedisTemplate<String, String> redisTemplate;

    // 构造器注入...

    @Override
    public boolean sendVerifyCode(String phoneNumber) {
        String code = generateRandomCode();
        // 调用短信服务商 API 发送验证码
        boolean sent = smsClient.send(phoneNumber, code);
        if (sent) {
            // 存储验证码到 Redis
            redisTemplate.opsForValue().set(
                "sms:code:" + phoneNumber, code,
                getExpireSeconds(), TimeUnit.SECONDS);
        }
        return sent;
    }

    @Override
    public boolean verifyCode(String phoneNumber, String verifyCode) {
        String key = "sms:code:" + phoneNumber;
        String storedCode = redisTemplate.opsForValue().get(key);
        if (verifyCode.equals(storedCode)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }
}
```

3. **实现用户详情服务**

确保你的 `UserDetailsService` 支持通过手机号查询用户：

```java
@Service
public class PhoneUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 构造器注入...

    @Override
    public UserDetails loadUserByUsername(String phone) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        return convertToUserDetails(user);
    }
}
```

4. **配置属性**

```properties
# application.properties
security.sms.enabled=true
security.sms.expire-seconds=300
security.sms.max-attempts=5
```

### 认证端点示例

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/sms")
    public Response<?> smsLogin(@RequestParam String phone,
                                @RequestParam String code) {
        SmsAuthenticationToken token = new SmsAuthenticationToken(phone, code);
        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);
        return Response.successBuilder("登录成功").build();
    }
}
```

## 开发自定义插件

完整的插件开发指南请参考：[插件开发指南](../../docs/plugin-development.md)

### 核心步骤

1. 创建自定义 `AuthenticationToken`
2. 实现 `AuthenticationProvider` 接口
3. 实现 `AuthenticationPlugin` 接口
4. 编写单元测试
5. 添加自动配置（可选）

### 注意事项

- 必须使用构造器依赖注入
- 公共 API 必须有 JavaDoc
- 日志中敏感数据需要脱敏
- 测试覆盖率要求 ≥ 90%

## 相关文档

- [快速开始](../../docs/quick-start.md)
- [配置参考](../../docs/configuration.md)
- [插件开发指南](../../docs/plugin-development.md)
