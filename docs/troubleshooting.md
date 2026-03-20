# Spring Security Boot 故障排查指南

本文档提供 Spring Security Boot 框架的常见问题诊断和解决方案，帮助开发者快速定位和解决使用过程中的各种问题。

## 目录

1. [快速参考](#快速参考)
2. [认证相关故障](#认证相关故障)
3. [权限控制问题](#权限控制问题)
4. [安全配置问题](#安全配置问题)
5. [性能问题](#性能问题)
6. [集成问题](#集成问题)
7. [监控与诊断](#监控与诊断)
8. [常见问题 FAQ](#常见问题-faq)

---

## 快速参考

### 紧急排查流程

1. **查看日志** - 检查应用启动和运行时的错误日志
2. **确认配置** - 验证 `application.properties/yaml` 中的安全配置
3. **检查权限** - 确认用户是否有访问资源的权限
4. **启用调试** - 开启调试模式查看详细信息

### 常用调试命令

```bash
# 查看当前认证信息
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer ${TOKEN}"

# 测试登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'

# 查看健康状态
curl -X GET http://localhost:8080/actuator/health
```

---

## 认证相关故障

### 故障 1: 用户名或密码错误

#### 故障现象
- 登录时返回 401 错误
- 日志显示 "用户名或密码错误"
- 明确输入了正确的用户名和密码

#### 排查步骤

1. **确认用户名和密码**
   - 检查大小写敏感度（默认敏感）
   - 确认数据库中用户存在且启用
   - 验证密码是否被正确编码

2. **查看日志详情**
   ```bash
   # 查看认证失败日志
   grep "用户名或密码错误" application.log
   ```

3. **检查密码编码**
   ```java
   // 使用 BCryptPasswordEncoder 验证密码
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
   boolean matches = encoder.matches(rawPassword, encodedPassword);
   ```

#### 解决方案

**方案 1: 重置密码**
```sql
-- MySQL 重置密码示例
UPDATE sys_users
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE username = 'admin';
```

**方案 2: 检查配置**
```properties
# application.properties
# 确保使用 BCryptPasswordEncoder
security.password.encoder.type=bcrypt
```

**方案 3: 启用详细日志**
```properties
logging.level.org.springframework.security=DEBUG
```

---

### 故障 2: JWT Token 过期或无效

#### 故障现象
- 返回 401 "Token 已过期" 错误
- Token 格式正确但无法验证
- Token 自动刷新失败

#### 排查步骤

1. **检查 Token 有效期**
   ```java
   // 验证 Token 的过期时间（使用 JJWT 0.11.x+ API）
   Jws<Claims> claims = Jwts.parserBuilder()
       .setSigningKey(secretKey)
       .build()
       .parseClaimsJws(token);

   long expirationTime = claims.getBody().getExpiration().getTime();
   long currentTime = System.currentTimeMillis();

   if (currentTime > expirationTime) {
       // Token 已过期
   }
   ```

2. **检查 Token 签名**
   ```properties
   # 确保签名密钥配置正确
   # 密钥必须是 Base64 编码的至少 256 位（32 字节）随机值
   # 生成方式: openssl rand -base64 32
   security.jwt.secret=${JWT_SECRET:请使用安全的随机密钥}
   ```
   > ⚠️ **安全警告**: 生产环境必须使用环境变量或密钥管理服务配置 JWT 密钥！

3. **验证 Token 黑名单**
   > ⚠️ **注意**: Token 黑名单功能需要用户自行实现，框架默认不包含此功能。

   ```java
   // 检查 Token 是否在黑名单中（需自行实现 tokenBlacklistService）
   if (tokenBlacklistService.isBlacklisted(token)) {
       throw new BadCredentialsException("Token 已被吊销");
   }
   ```

#### 解决方案

**方案 1: 刷新 Token**
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}'
```

**方案 2: 调整 Token 有效期**
```properties
# 延长 Token 有效期
security.jwt.expiration=86400000  # 24小时
security.jwt.refresh-expiration=604800000  # 7天
```

**方案 3: 检查 Token 配置**
> ⚠️ **注意**: 本框架使用自定义的 `JwtUtils` 组件处理 JWT，而非 Spring Security OAuth 的 JwtTokenStore。

```java
// 确保正确配置 JWT 密钥
@Configuration
public class JwtConfig {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecret);
    }
}
```

---

### 故障 3: Session 认证失败

#### 故障现象
- 使用 Session 认证时提示未登录
- Session 超时失效
- 多设备登录冲突

#### 排查步骤

1. **检查 Session 配置**
   ```properties
   # Session 配置
   server.servlet.session.timeout=30m
   server.servlet.session.cookie.max-age=1800
   ```

2. **查看 Session 状态**
   ```java
   // 查看当前 Session 信息
   @GetMapping("/api/session/info")
   public ResponseEntity<Map<String, Object>> sessionInfo(
       HttpSession session) {

       Map<String, Object> info = new HashMap<>();
       info.put("sessionId", session.getId());
       info.put("creationTime", session.getCreationTime());
       info.put("lastAccessedTime", session.getLastAccessedTime());
       info.put("maxInactiveInterval", session.getMaxInactiveInterval());

       return ResponseEntity.ok(info);
   }
   ```

3. **检查 Session 集群配置**
   > ⚠️ **注意**: Redis Session 集群功能需要额外添加 `spring-session-data-redis` 依赖并配置 Redis。

   ```java
   @Configuration
   @EnableRedisHttpSession
   public class SessionConfig {

       @Bean
       public LettuceConnectionFactory connectionFactory() {
           // Redis 连接配置
           return new LettuceConnectionFactory(
               new RedisStandaloneConfiguration("localhost", 6379));
       }
   }
   ```

#### 解决方案

**方案 1: 修复 Session 配置**
```properties
# application.properties
# 启用 Session 管理
spring.session.store-type=redis
spring.session.redis.namespace=spring-security-boot
spring.session.redis.cleanup-cron=0 * * * * *
```

**方案 2: 检查 Redis 连接**
```bash
# 测试 Redis 连接
redis-cli ping
# PONG

# 查看 Session 数据（生产环境请使用 SCAN 替代 KEYS）
# ⚠️ 警告: KEYS 命令在大数据量下会阻塞 Redis，生产环境请慎用
redis-cli --scan --pattern "spring*:session:*"
```

**方案 3: 启用 Remember Me**
```java
@Configuration
public class SecurityConfig {

    @Value("${security.remember-me.key}")
    private String rememberMeKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .rememberMe()
                .key(rememberMeKey) // 从配置文件读取
                .tokenValiditySeconds(2592000) // 30天
                .and()
            // 其他配置...
        ;

        return http.build();
    }
}
```
> ⚠️ **注意**: Remember Me 功能需要在配置文件中设置 `security.remember-me.key` 属性。

---

### 故障 4: 第三方 OAuth2 认证失败

#### 故障现象
- OAuth2 登录页面无法访问
- 授权码获取失败
- Token 获取失败

#### 排查步骤

1. **检查 OAuth2 配置**
   ```yaml
   # application.yaml
   security:
     oauth2:
       client:
         registration:
           google:
             clientId: your-client-id
             clientSecret: your-client-secret
             redirectUri: "{baseUrl}/login/oauth2/code/{registrationId}"
             scope:
               - profile
               - email
   ```

2. **查看回调地址**
   - 确认回调 URL 正确配置
   - 检查回调 URL 是否被白名单

3. **检查依赖**
   ```xml
   <!-- 确保包含 OAuth2 依赖 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-oauth2-client</artifactId>
   </dependency>
   ```

#### 解决方案

<!-- 确保包含 OAuth2 依赖 -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-oauth2-client</artifactId>
   </dependency>
   ```
   > ⚠️ **注意**: OAuth2 客户端功能需要额外配置，本框架默认不包含此功能。

   #### 解决方案

   **方案 1: 修正 OAuth2 配置**
```properties
# Google OAuth2 配置示例
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email

# 修正回调地址
server.servlet.context-path=/app
```

**方案 2: 添加 CORS 配置**
```java
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/oauth2/**")
                    .allowedOrigins("https://your-domain.com")
                    .allowedMethods("GET", "POST")
                    .allowedHeaders("*");
            }
        };
    }
}
```

**方案 3: 检查提供商配置**
```java
@Bean
public ClientRegistrationRepository clientRegistrationRepository() {
    return new InMemoryClientRegistrationRepository(
        GoogleClientRegistration.builder()
            .clientId("your-client-id")
            .clientSecret("your-client-secret")
            .scope("profile", "email")
            .redirectUriTemplate("{baseUrl}/login/oauth2/code/{registrationId}")
            .build()
    );
}
```

---

## 权限控制问题

### 故障 5: @PreAuthorize 注解不生效

#### 故障现象
- 有权限的用户无法访问受保护资源
- 即使有相应角色仍返回 403
- 注解被忽略不执行

#### 排查步骤

1. **检查注解导入**
   ```java
   import org.springframework.security.access.prepost.PreAuthorize;

   @RestController
   public class UserController {

       @PreAuthorize("hasRole('ADMIN')") // 确保导入正确
       @GetMapping("/admin/users")
       public List<User> listUsers() {
           return userService.getAllUsers();
       }
   }
   ```

2. **确认启用方法安全**
   ```java
   @SpringBootApplication
   @EnableMethodSecurity // Spring Security 5.7.x+ 推荐方式
   public class Application {

   }
   ```

3. **检查表达式语法**
   ```java
   // 常见表达式
   @PreAuthorize("hasRole('ADMIN')")           // 检查角色
   @PreAuthorize("hasAuthority('READ_USER')") // 检查权限
   @PreAuthorize("isAuthenticated()")         // 检查已认证
   @PreAuthorize("#id == authentication.principal.id") // 使用参数
   ```

#### 解决方案

**方案 1: 修正方法安全配置**
```java
   @Configuration
   @EnableMethodSecurity // Spring Security 5.7.x+ 推荐方式
   public class MethodSecurityConfig {

       // 确保配置类被扫描到
   }
```

**方案 2: 检查 AOP 依赖**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

**方案 3: 使用 SpEL 表达式**
```java
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
@GetMapping("/users/{userId}")
public User getUser(@PathVariable Long userId) {
    return userService.getUserById(userId);
}
```

---

### 故障 6: 动态权限加载失败

#### 故障现象
- 权限缓存不更新
- 新分配的权限不生效
- 权限数据源配置错误

#### 排查步骤

1. **检查权限数据源**
   ```java
   @Service
   public class CustomPermissionService implements PermissionService {

       @Override
       public List<String> getUserPermissions(String username) {
           // 检查数据源连接
           return permissionRepository.findByUsername(username)
               .stream()
               .map(Permission::getName)
               .collect(Collectors.toList());
       }
   }
   ```

2. **验证缓存配置**
   ```java
   @Configuration
   public class CacheConfig {

       @Bean
       public CacheManager cacheManager() {
           CaffeineCacheManager cacheManager = new CaffeineCacheManager();
           cacheManager.setCaffeine(Caffeine.newBuilder()
               .expireAfterWrite(30, TimeUnit.MINUTES));
           return cacheManager;
       }
   }
   ```

3. **检查权限刷新机制**
   ```java
   @EventListener
   public void handleUserPermissionChange(UserPermissionEvent event) {
       // 清除用户权限缓存
       permissionCache.evict(event.getUsername());
   }
   ```

#### 解决方案

**方案 1: 实现权限缓存管理**
```java
@Service
public class PermissionCacheService {

    @CacheEvict(value = "permissions", key = "#username")
    public void clearUserPermissions(String username) {
        // 清除指定用户权限缓存
    }

    @CacheEvict(value = "permissions", allEntries = true)
    public void clearAllPermissions() {
        // 清除所有权限缓存
    }
}
```

**方案 2: 使用 Spring Security 的缓存支持**
```java
@Configuration
public class SecurityCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        cacheManager.setCacheManagers(Arrays.asList(
            caffeineCacheManager(),
            redisCacheManager()
        ));
        return cacheManager;
    }
}
```

**方案 3: 定时刷新权限**
```java
@Scheduled(fixedRate = 300000) // 每5分钟
public void refreshPermissions() {
    permissionService.refreshAllPermissions();
}
```

---

### 故障 7: 角色权限配置错误

#### 故障现象
- 用户权限不足或过多
- 角色继承不生效
- 权限字符串格式错误

#### 排查步骤

1. **检查角色定义**
   ```java
   @Entity
   public class Role {

       @Id
       private Long id;

       @ManyToMany(fetch = FetchType.EAGER)
       private Set<Permission> permissions = new HashSet<>();

       @ManyToMany(mappedBy = "roles")
       private Set<User> users = new HashSet<>();
   }
   ```

2. **验证权限字符串**
   ```java
   // 权限应该使用规范的命名
   "USER_CREATE"  // 推荐
   "user:create"   // 可选
   "userCreate"   // 不推荐
   ```

3. **检查角色继承**
   ```java
   @Service
   public class RoleHierarchyServiceImpl implements RoleHierarchy {

       @Override
       public Collection<GrantedAuthority> getReachableGrantedAuthorities(
           Collection<String> authorities) {

           // 实现角色继承逻辑
           // ADMIN > MANAGER > USER
       }
   }
   ```

#### 解决方案

**方案 1: 规范化权限定义**
```java
public enum Permission {
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_DELETE("user:delete"),
    ROLE_ADMIN("role:admin"),
    ROLE_MANAGER("role:manager");

    private String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
```

**方案 2: 配置角色层次**
```java
@Bean
public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
    return roleHierarchy;
}
```

**方案 3: 使用权限枚举**
```java
@PreAuthorize("hasAuthority('USER_READ')")
@GetMapping("/users")
public List<User> listUsers() {
    return userService.getAllUsers();
}
```

---

## 安全配置问题

### 故障 8: CORS 配置错误

#### 故障现象
- 前端请求被浏览器阻止
- 预检请求（OPTIONS）失败
- 跨域响应头缺失

#### 排查步骤

1. **检查浏览器控制台**
   - 查看是否有 CORS 错误
   - 确认请求方法和路径

2. **验证后端配置**
   ```java
   @Configuration
   public class CorsConfig {

       @Bean
       public CorsConfigurationSource corsConfigurationSource() {
           CorsConfiguration configuration = new CorsConfiguration();
           configuration.setAllowedOrigins(Arrays.asList("https://your-domain.com"));
           configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
           configuration.setAllowedHeaders(Arrays.asList("*"));
           configuration.setAllowCredentials(true);

           UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
           source.registerCorsConfiguration("/**", configuration);
           return source;
       }
   }
   ```

3. **检查 Spring Security 配置**
   ```java
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
           .cors(cors -> cors.configurationSource(corsConfigurationSource()))
           .csrf(csrf -> csrf.disable())
           // 其他配置...
       ;
       return http.build();
   }
   ```

#### 解决方案

**方案 1: 正确配置 CORS**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:3000", "https://your-domain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**方案 2: 开发环境配置**
> ⚠️ **警告**: `allowed-origins=*` 与 `allow-credentials=true` 组合在浏览器中不被支持！
> 请使用具体的域名列表替代通配符。

```properties
# 开发环境配置（仅开发环境使用）
spring.mvc.cors.allowed-origins=http://localhost:3000,http://localhost:8080
spring.mvc.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.mvc.cors.allowed-headers=*
spring.mvc.cors.allow-credentials=true
```

**方案 3: 前端配置（Vue示例）**
```javascript
// axios 配置
axios.defaults.withCredentials = true;
axios.defaults.baseURL = 'http://localhost:8080';

// 处理预检请求
axios.interceptors.request.use(config => {
    if (config.method === 'post' || config.method === 'put') {
        config.headers = {
            'Content-Type': 'application/json',
            ...config.headers
        };
    }
    return config;
});
```

---

### 故障 9: CSRF 保护冲突

#### 故障现象
- POST 请求被拒绝
- CSRF Token 不匹配
- 前端表单提交失败

#### 排查步骤

1. **检查 CSRF 配置**
   ```java
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
           .csrf(csrf -> csrf
               .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
               .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
           )
           // 其他配置...
       ;
       return http.build();
   }
   ```

2. **验证 Token 获取**
   ```html
   <!-- 在 HTML 中获取 CSRF Token -->
   <meta name="_csrf" content="${_csrf.token}"/>
   <meta name="_csrf_header" content="${_csrf.headerName}"/>

   <!-- 在 AJAX 请求中设置 Token -->
   <script>
   var token = $("meta[name='_csrf']").attr("content");
   var header = $("meta[name='_csrf_header']").attr("content");

   $(document).ajaxSend(function(e, xhr, options) {
       xhr.setRequestHeader(header, token);
   });
   </script>
   ```

3. **检查特殊端点**
   ```java
   @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
       http
           .csrf(csrf -> csrf
               .ignoringRequestMatchers("/api/public/**", "/actuator/**")
           )
           // 其他配置...
       ;
       return http.build();
   }
   ```

#### 解决方案

**方案 1: 配置 CSRF 过滤**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .sessionAuthenticationStrategy(new PreventLoginAuthenticationStrategy())
        )
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login", "/error", "/public/**").permitAll()
            .anyRequest().authenticated()
        )
    ;
    return http.build();
}
```

**方案 2: 前端获取 CSRF Token**
```javascript
// 在登录后获取 CSRF Token
function getCsrfToken() {
    return axios.get('/csrf-token')
        .then(response => response.data)
        .catch(error => console.error('获取 CSRF Token 失败', error));
}

// 提交表单时包含 CSRF Token
async function submitForm(formData) {
    const csrfToken = await getCsrfToken();

    return axios.post('/api/submit', formData, {
        headers: {
            'X-XSRF-TOKEN': csrfToken
        }
    });
}
```

**方案 3: 禁用 CSRF（不推荐）**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        // 其他配置...
    ;
    return http.build();
}
```

---

### 故障 10: 安全头配置问题

#### 故障现象
- 安全扫描报告发现风险
- HSTS、CSP 等头信息缺失
- 敏感信息泄露

#### 排查步骤

1. **检查安全头配置**
   ```java
   @Configuration
   public class SecurityHeadersConfig {

       @Bean
       public HeaderWriterFilter headerWriterFilter() {
           HeaderWriterFilter filter = new HeaderWriterFilter(
               Arrays.asList(
                   new StrictTransportSecurityWriter("max-age=31536000; includeSubDomains"),
                   new XContentTypeOptionsWriter(),
                   new XXssProtectionWriter(),
                   new XFrameOptionsWriter("DENY"),
                   new CacheControlHeadersWriter(),
                   new HstsHeaderWriter(),
                   new ContentSecurityPolicyWriter("default-src 'self'")
               )
           );
           return filter;
       }
   }
   ```

2. **查看响应头**
   ```bash
   curl -I http://localhost:8080/api/users/me
   # 查看响应头是否包含安全头
   ```

3. **使用安全扫描工具**
   ```bash
   # 使用 OWASP ZAP 扫描
   zap-cli -t http://localhost:8080 scan --scanners all
   ```

#### 解决方案

**方案 1: 配置常见安全头**
```java
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'")
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .frameOptions(frame -> frame.sameOrigin())
                .contentTypeOptions()
            )
            // 其他配置...
        ;
        return http.build();
    }
}
```

**方案 2: 使用安全响应头过滤器**
```java
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {

        // 设置安全头
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "default-src 'self'");

        filterChain.doFilter(request, response);
    }
}
```

**方案 3: 使用 Spring Security 的内置支持**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'")
            )
            .and()
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .preload(true)
                .maxAgeInSeconds(31536000)
            )
        )
        // 其他配置...
    ;
    return http.build();
}
```

---

## 性能问题

### 故障 11: 认证响应慢

#### 故障现象
- 登录接口响应时间超过 2 秒
- 用户信息查询缓慢
- 权限检查耗时过长

#### 排查步骤

1. **性能分析**
   ```java
   @Aspect
   @Component
   public class PerformanceAspect {

       @Around("execution(* com.original.security..*.*(..))")
       public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
           long start = System.currentTimeMillis();

           try {
               Object result = joinPoint.proceed();
               long duration = System.currentTimeMillis() - start;

               if (duration > 1000) { // 超过1秒记录日志
                   log.warn("Method {} took {} ms",
                           joinPoint.getSignature().toShortString(),
                           duration);
               }

               return result;
           } catch (Throwable e) {
               throw e;
           }
       }
   }
   ```

2. **检查数据库查询**
   ```sql
   -- 慢查询分析
   EXPLAIN SELECT * FROM sys_users WHERE username = 'admin';
   EXPLAIN SELECT u.*, r.name FROM sys_users u
   LEFT JOIN sys_user_roles ur ON u.id = ur.user_id
   LEFT JOIN sys_roles r ON ur.role_id = r.id
   WHERE u.username = 'admin';
   ```

3. **监控缓存命中率**
   > ⚠️ **注意**: 缓存功能需要额外配置 `@EnableCaching` 并添加相关依赖。

   ```java
   @Bean
   public CacheManager caffeineCacheManager() {
       CaffeineCacheManager cacheManager = new CaffeineCacheManager();
       cacheManager.setCaffeine(Caffeine.newBuilder()
           .recordStats() // 启用统计
           .expireAfterWrite(30, TimeUnit.MINUTES));

       return cacheManager;
   }

   // 获取缓存统计需要使用 Caffeine 原生 API
   public void logCacheStats(CaffeineCacheManager cacheManager) {
       com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
           (com.github.benmanes.caffeine.cache.Cache<Object, Object>)
           cacheManager.getCache("users").getNativeCache();
       CacheStats stats = nativeCache.stats();
       log.info("Cache hit rate: {}", stats.hitRate());
   }
   ```

#### 解决方案

**方案 1: 添加缓存层**
> ⚠️ **注意**: 缓存功能需要额外配置 `@EnableCaching` 并添加 `spring-boot-starter-cache` 依赖。

```java
@Service
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

    @Cacheable(key = "#username")
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @CacheEvict(key = "#user.username")
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
```

**方案 2: 优化数据库查询**
```java
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Override
    public Optional<User> findByUsername(String username) {
        // 使用 JPQL 优化
        return entityManager.createQuery(
            "SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username",
            User.class)
            .setParameter("username", username)
            .getResultStream()
            .findFirst();
    }
}
```

**方案 3: 异步认证**
```java
@Service
public class AsyncAuthService {

    @Async("authTaskExecutor")
    public CompletableFuture<Authentication> authenticate(AuthenticationRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // 执行异步认证
            return authProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );
        });
    }
}
```

---

### 故障 12: 权限检查性能问题

#### 故障现象
- 复杂权限验证耗时
- 多层权限检查导致延迟
- 权限缓存失效频繁

#### 排查步骤

1. **分析权限检查热点**
   ```java
   @Around("@annotation(preAuthorize)")
   public Object preAuthorizeAdvice(ProceedingJoinPoint joinPoint, PreAuthorize preAuthorize) throws Throwable {
       long start = System.currentTimeMillis();

       try {
           Object result = joinPoint.proceed();
           long duration = System.currentTimeMillis() - start;

           if (duration > 500) { // 超过500ms记录
               log.warn("Permission check took {} ms: {}",
                       duration,
                       preAuthorize.value());
           }

           return result;
       } catch (Throwable e) {
           throw e;
       }
   }
   ```

2. **检查权限表达式**
   ```java
   // 避免复杂的权限表达式
   @PreAuthorize("@customPermissionEvaluator.hasPermission(authentication, #id, 'EDIT')")
   public void editResource(Long id) {
       // 复杂的权限评估
   }
   ```

3. **监控权限缓存**
   ```java
   @Component
   public class PermissionMonitor {

       @Scheduled(fixedRate = 60000)
       public void monitorPermissionCache() {
           Cache cache = cacheManager.getCache("permissions");
           CacheStats stats = cache.stats();

           log.info("Permission Cache - Hit Rate: {}, Miss Count: {}",
                   stats.hitRate(), stats.missCount());
       }
   }
   ```

#### 解决方案

**方案 1: 使用缓存**
```java
@Service
public class PermissionService {

    @Cacheable(value = "permissions", key = "#username + ':' + #permission")
    public boolean hasPermission(String username, String permission) {
        return permissionRepository.existsByUsernameAndPermission(username, permission);
    }
}
```

**方案 2: 预加载权限**
```java
@Component
public class PermissionPreloader {

    @PostConstruct
    public void preloadPermissions() {
        // 预加载常用权限
        List<String> permissions = Arrays.asList(
            "user:read", "user:write", "user:delete",
            "role:admin", "role:manager"
        );

        permissions.forEach(permission -> {
            // 缓存权限
        });
    }
}
```

**方案 3: 优化权限数据结构**
```java
@Service
public class PermissionServiceImpl implements PermissionService {

    // 使用内存缓存优化
    private final Map<String, Set<String>> userPermissionCache = new ConcurrentHashMap<>();

    @Override
    public boolean hasPermission(String username, String permission) {
        return userPermissionCache.computeIfAbsent(username,
            k -> loadUserPermissions(k))
            .contains(permission);
    }
}
```

---

## 集成问题

### 故障 13: Spring Cloud 集成问题

#### 故障现象
- 服务间调用认证失败
- Feign 客户端无法传递 Token
- 负载均衡问题

#### 排查步骤

1. **检查 Feign 配置**
   ```java
   @FeignClient(name = "user-service", url = "http://user-service")
   public interface UserServiceClient {

       @RequestHeader("Authorization")
       @GetMapping("/api/users/{id}")
       User getUser(@PathVariable("id") Long id);
   }
   ```

2. **验证 Token 传递**
   ```java
   @Component
   public class FeignRequestInterceptor implements RequestInterceptor {

       @Override
       public void apply(RequestTemplate template) {
           // 从 SecurityContext 获取 Token（添加空值检查）
           Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

           if (authentication != null && authentication.getCredentials() != null) {
               String token = authentication.getCredentials().toString();
               template.header("Authorization", "Bearer " + token);
           }
       }
   }
   ```
   > ⚠️ **注意**: 在异步环境中，`SecurityContextHolder` 默认使用 `MODE_THREADLOCAL`，会丢失上下文。
   > 需要配置 `DelegatingSecurityContextRunnable` 或使用 `MODE_INHERITABLETHREADLOCAL`。

3. **检查服务发现**
   ```yaml
   # application.yaml
   spring:
     cloud:
       nacos:
         discovery:
           server-addr: localhost:8848
           namespace: dev
           group: SECURITY_GROUP
   ```

#### 解决方案

**方案 1: 配置 Feign 客户端**
```java
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // 从 SecurityContext 获取 Token
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() != null) {
                String token = authentication.getCredentials().toString();
                template.header("Authorization", "Bearer " + token);
            }
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
```

**方案 2: 使用 OAuth2 客户端凭证模式**
```java
// 需要添加 spring-boot-starter-oauth2-client 依赖
@Bean
public RequestInterceptor oauth2FeignRequestInterceptor(
        OAuth2AuthorizedClientManager authorizedClientManager) {

    return template -> {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId("client-registration-id")
            .principal("feign-client")
            .build();

        OAuth2AuthorizedClient authorizedClient =
            authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            template.header("Authorization",
                "Bearer " + authorizedClient.getAccessToken().getTokenValue());
        }
    };
}
```

**方案 3: 配置服务调用**
```java
@FeignClient(name = "user-service",
             configuration = FeignConfig.class,
             fallback = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    User getUser(@RequestHeader("Authorization") String token,
                 @PathVariable Long id);
}
```

---

### 故障 14: 部署环境问题

#### 故障现象
- 生产环境认证失败
- 容器化部署问题
- 环境变量配置错误

#### 排查步骤

1. **检查环境变量**
   ```bash
   # 查看容器环境
   docker exec -it container-name env

   # 检查配置文件
   docker exec -it container-name cat /app/config/application.properties
   ```

2. **验证配置**
   ```java
   @ConfigurationProperties(prefix = "security")
   @Data
   public class SecurityProperties {

       private String jwtSecret;
       private Long jwtExpiration;
       private String passwordEncoderType;

       @PostConstruct
       public void validate() {
           if (jwtSecret == null || jwtSecret.isEmpty()) {
               throw new IllegalStateException("JWT secret must be configured");
           }
       }
   }
   ```

3. **检查文件权限**
   ```bash
   # 检查配置文件权限
   ls -la /app/config/
   chmod 600 /app/config/application.properties
   ```

#### 解决方案

**方案 1: 使用环境变量**
```properties
# 使用环境变量
security.jwt.secret=${JWT_SECRET:default-secret}
security.jwt.expiration=${JWT_EXPIRATION:86400000}
```

**方案 2: 配置 Docker**
> ⚠️ **安全警告**: 不应以 root 用户运行容器！应创建非特权用户。

```dockerfile
FROM openjdk:8-jre-alpine

# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

COPY target/app.jar /app/app.jar
COPY config/application.properties /app/config/

# 设置正确的权限并切换用户
RUN chown -R appuser:appgroup /app && \
    chmod 600 /app/config/application.properties

USER appuser
EXPOSE 8080
CMD java $JAVA_OPTS -jar /app/app.jar
```

**方案 3: 使用配置服务器**
```yaml
# bootstrap.yaml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      name: security-config
      profile: ${SPRING_PROFILES_ACTIVE:default}
```

---

## 监控与诊断

### 故障 15: 健康检查失败

#### 故障现象
- /actuator/health 返回 503
- 数据库连接检查失败
- 权限服务不可用

#### 排查步骤

1. **检查健康配置**
   ```java
   @Configuration
   public class HealthConfig {

       @Bean
       public HealthIndicator customHealthIndicator() {
           return () -> {
               try {
                   // 检查数据库连接
                   jdbcTemplate.execute("SELECT 1");
                   return Health.up()
                       .withDetail("database", "Available")
                       .build();
               } catch (Exception e) {
                   return Health.down()
                       .withDetail("error", e.getMessage())
                       .build();
               }
           };
       }
   }
   ```

2. **查看健康端点**
   ```bash
   curl -X GET http://localhost:8080/actuator/health
   curl -X GET http://localhost:8080/actuator/health/db
   ```

3. **自定义健康检查**
   ```java
   @Component
   public class SecurityHealthIndicator implements HealthIndicator {

       @Override
       public Health health() {
           boolean securityOk = checkSecurityServices();

           if (securityOk) {
               return Health.up()
                   .withDetail("message", "Security services are healthy")
                   .build();
           } else {
               return Health.down()
                   .withDetail("error", "Security services are not available")
                   .build();
           }
       }

       private boolean checkSecurityServices() {
           // 检查认证服务
           return true;
       }
   }
   ```

#### 解决方案

**方案 1: 配置健康检查**
> ⚠️ **安全警告**: 生产环境不应暴露所有 actuator 端点！请根据需要选择暴露的端点。

```properties
# application.properties
# 生产环境建议只暴露必要端点
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when_authorized

# 数据库健康检查
management.health.db.enabled=true
management.health.defaults.enabled=true
```

**方案 2: 自定义健康指标**
```java
@RestController
public class HealthController {

    @GetMapping("/health/security")
    public ResponseEntity<Map<String, Object>> securityHealth() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "UP");
        health.put("version", "1.0.0");
        health.put("timestamp", Instant.now());

        // 添加安全相关的健康信息
        health.put("authentication", checkAuthentication());
        health.put("permissions", checkPermissions());

        return ResponseEntity.ok(health);
    }

    private Map<String, Object> checkAuthentication() {
        // 检查认证服务状态
        return Map.of("status", "healthy", "users", "100");
    }
}
```

**方案 3: 健康检查聚合**
```java
@Component
public class CompositeHealthIndicator implements HealthIndicator {

    private final List<HealthIndicator> indicators = new ArrayList<>();

    @Autowired
    public CompositeHealthIndicator(List<HealthIndicator> indicators) {
        this.indicators.addAll(indicators);
    }

    @Override
    public Health health() {
        Map<String, Health> results = new HashMap<>();

        indicators.forEach(indicator -> {
            Health health = indicator.health();
            results.put(health.getClass().getSimpleName(), health);
        });

        boolean allHealthy = results.values().stream()
            .allMatch(h -> h.getStatus() == Status.UP);

        if (allHealthy) {
            return Health.up()
                .withDetails(results)
                .build();
        } else {
            return Health.down()
                .withDetails(results)
                .build();
        }
    }
}
```

---

### 故障 16: 日志分析

#### 故障现象
- 日志信息不完整
- 安全事件记录不足
- 日志级别配置错误

#### 排查步骤

1. **配置日志级别**
   ```properties
   # 日志配置
   logging.level.org.springframework.security=DEBUG
   logging.level.org.springframework.web=DEBUG
   logging.level.com.original.security=TRACE

   # 日志文件
   logging.file.name=logs/application.log
   logging.file.max-size=100MB
   logging.file.max-history=30
   ```

2. **自定义安全日志**
   ```java
   @Component
   public class SecurityEventListener {

       @EventListener
       public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
           log.info("User {} logged in successfully",
                   event.getAuthentication().getName());

           // 记录登录事件
           securityEventService.logEvent(
               "LOGIN",
               "User login",
               event.getAuthentication()
           );
       }

       @EventListener
       public void handleAuthenticationFailure(AuthenticationFailureEvent event) {
           log.warn("Login failed for user: {} - Reason: {}",
                   event.getAuthentication().getName(),
                   event.getException().getMessage());
       }
   }
   ```

3. **日志分析工具**
   ```bash
   # 使用 grep 分析认证相关日志
   grep "authentication" logs/application.log | tail -n 100

   # 分析慢查询
   grep "took.*ms" logs/application.log | sort -nr | head -n 20

   # 统计错误
   grep "ERROR\|Exception" logs/application.log | wc -l
   ```

#### 解决方案

**方案 1: 配置结构化日志**

首先添加依赖：
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.3</version>
</dependency>
```

然后配置 logback-spring.xml：
```xml
<configuration>
    <appender name="JSON" class="ch.qos.logback.core.FileAppender">
        <file>logs/security-events.json</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>

    <logger name="com.original.security" level="INFO" additivity="false">
        <appender-ref ref="JSON"/>
    </logger>
</configuration>
```

**方案 2: 安全事件日志**
```java
@Component
public class SecurityEventLogger {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventLogger.class);

    public void logSecurityEvent(String type, String message,
                                Authentication authentication,
                                Map<String, Object> details) {

        Map<String, Object> event = new HashMap<>();
        event.put("timestamp", Instant.now());
        event.put("type", type);
        event.put("message", message);
        event.put("username", authentication != null ? authentication.getName() : "anonymous");
        event.put("details", details);

        log.info("Security Event: {}", toJson(event));
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return map.toString();
        }
    }
}
```

**方案 3: 日志监控**
```java
@Component
public class LogMonitor {

    private static final Logger log = LoggerFactory.getLogger(LogMonitor.class);

    @Scheduled(fixedRate = 300000) // 每5分钟
    public void monitorLogs() {
        // 统计错误日志
        long errorCount = countErrorLogs();

        if (errorCount > 10) {
            log.warn("High error count detected: {}", errorCount);
            alertService.sendAlert("High error count: " + errorCount);
        }

        // 检查认证失败率
        double authFailureRate = calculateAuthenticationFailureRate();

        if (authFailureRate > 0.3) {
            log.warn("High authentication failure rate: {}", authFailureRate);
        }
    }

    private long countErrorLogs() {
        // 实现错误日志统计
        return 0;
    }
}
```

---

## 常见问题 FAQ

### Q1: 如何处理 401 和 403 的区别？

**401 Unauthorized**：
- 表示未认证（没有提供 Token 或 Token 无效）
- 客户端可以重新提供认证信息
- HTTP 状态码：401

**403 Forbidden**：
- 表示已认证但权限不足
- 即使重新认证也无法访问
- HTTP 状态码：403

```java
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Response<String>> handleAuthenticationException(
            AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Response.errorBuilder("用户未认证").build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response<String>> handleAccessDeniedException(
            AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(Response.errorBuilder("权限不足").build());
    }
}
```

### Q2: 如何实现多租户权限控制？

```java
@Configuration
public class MultiTenantSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/{tenantId}/**").access(new TenantExpression())
            )
        ;
        return http.build();
    }
}

class TenantExpression implements AuthorizationManager<RequestAuthorizationContext> {

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {

        String tenantId = context.getVariables().get("tenantId");
        Authentication auth = authentication.get();

        // 验证用户是否有访问该租户的权限
        if (auth instanceof TenantUserDetails) {
            TenantUserDetails user = (TenantUserDetails) auth;
            return new AuthorizationDecision(
                user.getTenants().contains(tenantId)
            );
        }

        return new AuthorizationDecision(false);
    }
}
```

### Q3: 如何实现单点登录（SSO）？

> ⚠️ **注意**: SSO/OAuth2 功能需要额外配置，本框架默认不包含此功能。

```java
@Configuration
public class SsoConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
            ClientRegistration.withRegistrationId("google")
                .clientId("${GOOGLE_CLIENT_ID}")
                .clientSecret("${GOOGLE_CLIENT_SECRET}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://www.googleapis.com/oauth2/v4/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .clientName("Google")
                .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/")
                .failureUrl("/login?error=true")
            );
        return http.build();
    }
}
```

### Q4: 如何处理密码重置功能的安全问题？

```java
@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    @Value("${security.reset-token.expiration}")
    private long tokenExpiration;

    public String createResetToken(String username) {
        // 使用加密安全的随机数生成器生成 Token
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // 存储 Token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUsername(username);
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(resetToken);

        // 发送邮件
        emailService.sendResetEmail(username, token);

        return token;
    }

    public boolean validateResetToken(String username, String token) {
        PasswordResetToken resetToken = tokenRepository
            .findByUsernameAndToken(username, token)
            .orElseThrow(() -> new RuntimeException("Invalid token"));

        // 检查 Token 是否过期
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            return false;
        }

        return true;
    }
}
```

### Q5: 如何实现 IP 白名单功能？

```java
@Configuration
public class IpWhitelistConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().access(new IpWhitelistAuthorizationManager())
            );
        return http.build();
    }
}

class IpWhitelistAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    // 使用完整的 IP 地址或 CIDR 表示法
    private final Set<String> whitelist = Set.of("192.168.1.0/24", "10.0.0.0/8");

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authentication,
            RequestAuthorizationContext context) {

        String remoteAddr = context.getRequest().getRemoteAddr();

        try {
            InetAddress remoteInetAddress = InetAddress.getByName(remoteAddr);

            // 检查是否在白名单中（使用 CIDR 匹配）
            boolean isAllowed = whitelist.stream().anyMatch(cidr ->
                isInCidrRange(remoteInetAddress, cidr));

            return new AuthorizationDecision(isAllowed);
        } catch (Exception e) {
            return new AuthorizationDecision(false);
        }
    }

    private boolean isInCidrRange(InetAddress address, String cidr) {
        // 使用 Apache Commons Net 或自行实现 CIDR 匹配
        // 简化示例：精确匹配
        return cidr.equals(address.getHostAddress());
    }
}
```

### Q6: 如何处理跨域认证问题？

```java
@Configuration
public class CorsSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://your-frontend.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

### Q7: 如何优化性能监控？

```java
@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "security-boot",
            "version", "1.0.0"
        );
    }

    @Bean
    public TimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new TimedAspect(meterRegistry, "security.timing");
    }
}

@Component
public class SecurityMetrics {

    private final MeterRegistry meterRegistry;

    public SecurityMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordAuthenticationEvent(String username, boolean success) {
        Timer.Sample.start(meterRegistry).stop(
            Timer.builder("security.authentication.timer")
                .tag("username", username)
                .tag("success", String.valueOf(success))
                .register(meterRegistry)
        );
    }

    public void incrementAuthFailures(String reason) {
        meterRegistry.counter("security.authentication.failures")
            .tag("reason", reason)
            .increment();
    }
}
```

---

## 快速索引

### 按错误类型查找

| 错误信息 | 相关故障 | 链接 |
|---------|---------|------|
| 401 Unauthorized | 认证相关 | [故障 1-4](#认证相关故障) |
| 403 Forbidden | 权限控制 | [故障 5-7](#权限控制问题) |
| Token expired | JWT 问题 | [故障 2](#故障-2-jwt-token-过期或无效) |
| CORS error | 跨域配置 | [故障 8](#故障-8-cors-配置错误) |
| CSRF token mismatch | CSRF 保护 | [故障 9](#故障-9-csrf-保护冲突) |
| Access Denied | 权限不足 | [故障 5](#故障-5-preauthorize-注解不生效) |
| Authentication failed | 认证失败 | [故障 1](#故障-1-用户名或密码错误) |
| Session timeout | 会话过期 | [故障 3](#故障-3-session-认证失败) |
| Service unavailable | 健康检查 | [故障 15](#故障-15-健康检查失败) |

### 按组件查找

| 组件 | 相关故障 |
|------|---------|
| JWT | 故障 2, Q1 |
| Session | 故障 3 |
| OAuth2 | 故障 4, Q3 |
| @PreAuthorize | 故障 5 |
| 权限缓存 | 故障 6, 12 |
| CORS | 故障 8, Q6 |
| CSRF | 故障 9 |
| 安全头 | 故障 10 |
| Feign | 故障 13 |
| Docker | 故障 14 |
| Actuator | 故障 15 |

---

## 结语

本文档提供了 Spring Security Boot 框架的常见问题排查指南。如果遇到未列出的问题，建议：

1. 查看 Spring Boot 和 Spring Security 的官方文档
2. 启用 DEBUG 级别日志获取更多信息
3. 使用开发者工具进行调试
4. 在社区或 GitHub Issues 中搜索类似问题

**文档更新日期**: 2026-03-20
**文档版本**: 1.1.0 (代码审查修复版)