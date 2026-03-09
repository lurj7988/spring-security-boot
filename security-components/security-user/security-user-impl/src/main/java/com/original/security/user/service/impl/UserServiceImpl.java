package com.original.security.user.service.impl;

import com.original.security.user.api.dto.request.PasswordChangeRequest;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.config.UserProperties;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.event.UserCreatedEvent;
import com.original.security.user.exception.EmailAlreadyExistsException;
import com.original.security.user.exception.InvalidPasswordException;
import com.original.security.user.exception.PasswordPolicyViolationException;
import com.original.security.user.exception.UserAlreadyExistsException;
import com.original.security.user.exception.UserDisabledException;
import com.original.security.user.exception.UserNotFoundException;
import com.original.security.user.notification.NotificationService;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    /**
     * 第一个用户时的用户计数阈值（用于分配特殊角色）
     */
    private static final long FIRST_USER_THRESHOLD = 0L;

    /**
     * 默认分页大小限制
     */
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 密码复杂度正则表达式：至少一个数字，一个字母，一个特殊字符，长度在8到50之间
     */
    private static final Pattern PASSWORD_COMPLEXITY_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!]).{8,50}$");

    /**
     * Spring Security 默认匿名用户标识
     */
    private static final String ANONYMOUS_USER = "anonymousUser";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserProperties userProperties;
    private final SessionRegistry sessionRegistry;
    private final NotificationService notificationService;

    public UserServiceImpl(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          ApplicationEventPublisher eventPublisher,
                          UserProperties userProperties,
                          SessionRegistry sessionRegistry,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.userProperties = userProperties;
        this.sessionRegistry = sessionRegistry;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        log.info("创建用户请求: username={}, email={}", request.getUsername(), request.getEmail());

        // 验证密码长度
        int maxLength = userProperties.getPassword().getMaxLength();
        if (request.getPassword() != null && request.getPassword().length() > maxLength) {
            log.warn("密码长度超过限制: username={}, password_length={}", request.getUsername(), request.getPassword().length());
            throw new PasswordPolicyViolationException("密码长度不能超过" + maxLength + "个字符");
        }

        // 创建用户实体
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail()
        );

        // 分配默认角色（通过配置支持）
        String defaultRoleName = userProperties.getDefaultRole().getName();
        String firstUserRoleName = userProperties.getDefaultRole().getFirstUserRole();

        if (userRepository.count() == FIRST_USER_THRESHOLD) {
            // 第一个用户获得配置的首用户角色（默认 ADMIN）
            Role firstUserRole = roleRepository.findByName(firstUserRoleName)
                    .orElseGet(() -> createDefaultRole(firstUserRoleName, "首用户角色"));
            user.addRole(firstUserRole);

            // 同时创建普通用户角色（供后续用户使用）
            roleRepository.findByName(defaultRoleName)
                    .orElseGet(() -> createDefaultRole(defaultRoleName, "普通用户角色"));
        } else {
            // 后续用户获得配置的默认角色
            Role userRole = roleRepository.findByName(defaultRoleName)
                    .orElseGet(() -> createDefaultRole(defaultRoleName, "普通用户角色"));
            user.addRole(userRole);
        }

        try {
            // 保存用户 - 依赖数据库的唯一约束来防止重复
            User savedUser = userRepository.save(user);

            // 发布审计事件
            eventPublisher.publishEvent(
                    new UserCreatedEvent(this, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail())
            );

            log.info("用户创建成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());

            return toDTO(savedUser);
        } catch (DataIntegrityViolationException e) {
            // 检查是用户名还是邮箱重复
            if (e.getMessage().contains("username") || e.getMessage().contains("users.UK_username")) {
                log.warn("用户名已存在: username={}", request.getUsername());
                throw new UserAlreadyExistsException(request.getUsername());
            } else if (e.getMessage().contains("email") || e.getMessage().contains("users.UK_email")) {
                log.warn("邮箱已存在: email={}", request.getEmail());
                throw new EmailAlreadyExistsException(request.getEmail());
            } else {
                // 其他数据完整性违规
                log.error("用户创建失败，数据完整性违规: username={}, error={}", request.getUsername(), e.getMessage());
                throw new IllegalStateException("用户创建失败：数据完整性错误");
            }
        }
    }

    @Override
    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 验证认证状态
        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            log.warn("未授权的访问尝试: 获取当前用户");
            throw new IllegalStateException("用户未认证");
        }

        String username = authentication.getName();

        // 验证用户名有效性
        if (username == null || username.trim().isEmpty()) {
            log.warn("认证上下文中用户名为空");
            throw new IllegalStateException("用户未认证");
        }

        // 查找用户
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("认证用户不存在于数据库: username={}", username);
                    return new UserNotFoundException(username);
                });

        // 验证用户是否被禁用
        if (!user.isEnabled()) {
            log.warn("尝试访问已禁用的用户: username={}", username);
            throw new UserDisabledException(username);
        }

        log.info("获取当前用户成功: username={}, userId={}", username, user.getId());
        return toDTO(user);
    }

    @Override
    public UserDTO getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在: userId={}", userId);
                    return new UserNotFoundException(userId);
                });

        return toDTO(user);
    }

    @Override
    public PageDTO<UserDTO> listUsers(int page, int size, String usernameKeyword, Boolean enabled) {
        // 参数验证
        if (page < 0) {
            page = 0;
        }
        if (page > 10000) {
            log.warn("分页参数过大，已限制: requestedPage={}, actualPage=0", page);
            page = 0;
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            size = DEFAULT_PAGE_SIZE;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        // 使用统一的查询方法，支持所有筛选条件
        String trimmedKeyword = (usernameKeyword != null) ? usernameKeyword.trim() : null;
        if (trimmedKeyword != null && trimmedKeyword.isEmpty()) {
            trimmedKeyword = null;
        }

        userPage = userRepository.findByUsernameContainingAndEnabled(trimmedKeyword, enabled, pageable);

        return new PageDTO<>(
                userPage.getContent().stream()
                        .map(this::toDTO)
                        .collect(Collectors.toList()),
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getSize(),
                userPage.getNumber()
        );
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            throw new IllegalStateException("用户未认证");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("修改密码失败，旧密码不匹配: username={}", username);
            throw new InvalidPasswordException("旧密码不正确");
        }

        // 验证新密码长度
        int maxLength = userProperties.getPassword().getMaxLength();
        if (request.getNewPassword() != null && request.getNewPassword().length() > maxLength) {
            log.warn("新密码长度超过限制: username={}, password_length={}", username, request.getNewPassword().length());
            throw new PasswordPolicyViolationException("密码长度不能超过" + maxLength + "个字符");
        }

        // 验证新密码复杂度
        if (!PASSWORD_COMPLEXITY_PATTERN.matcher(request.getNewPassword()).matches()) {
            log.warn("修改密码失败，新密码复杂度不足: username={}", username);
            throw new PasswordPolicyViolationException("密码复杂度不足：必须包含至少一个数字、一个字母和一个特殊字符，且长度应在8到50之间");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // 使当前用户的所有会话失效
        expireUserSessions(user);

        // 发送密码更改通知
        try {
            notificationService.sendPasswordChangedNotification(user);
        } catch (Exception e) {
            log.error("发送密码更改通知失败: userId={}, error={}", user.getId(), e.getMessage(), e);
            // 不中断主要业务流程，仅记录错误
        }

        log.info("用户密码修改成功: username={}", username);
    }

    @Override
    @Transactional
    public String resetPassword(Long userId) {
        // 查询目标用户（只查询一次）
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 确保不能重置自己的密码（必须由管理员操作）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            if (user.getUsername().equals(currentUsername)) {
                log.warn("用户试图重置自己的密码，应使用 changePassword 方法: username={}", currentUsername);
                throw new IllegalStateException("不允许通过此接口重置自己的密码，请使用 changePassword 接口");
            }
        }

        String newPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // 使被重置密码的用户的所有会话失效
        expireUserSessions(user);

        // 发送密码重置通知
        try {
            notificationService.sendPasswordResetNotification(user, newPassword);
        } catch (Exception e) {
            log.error("发送密码重置通知失败: userId={}, error={}", userId, e.getMessage(), e);
            // 不中断主要业务流程，仅记录错误
        }

        log.info("管理员重置用户密码成功: userId={}, resetBy={}", userId,
                 authentication != null ? authentication.getName() : "unknown");

        return newPassword;
    }

    /**
     * 生成符合复杂度的随机密码
     */
    private String generateRandomPassword() {
        StringBuilder charPool = new StringBuilder();
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";

        if (userProperties.getPassword().isIncludeLowercase()) {
            charPool.append(lowercase);
        }
        if (userProperties.getPassword().isIncludeUppercase()) {
            charPool.append(uppercase);
        }
        if (userProperties.getPassword().isIncludeNumbers()) {
            charPool.append(numbers);
        }
        if (userProperties.getPassword().isIncludeSpecialChars()) {
            charPool.append(userProperties.getPassword().getSpecialCharacters());
        }

        if (charPool.length() == 0) {
            throw new IllegalStateException("密码字符池不能为空，请检查密码配置属性");
        }

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // 确保至少包含每种类型的至少一个字符
        if (userProperties.getPassword().isIncludeLowercase()) {
            password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        }
        if (userProperties.getPassword().isIncludeUppercase()) {
            password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        }
        if (userProperties.getPassword().isIncludeNumbers()) {
            password.append(numbers.charAt(random.nextInt(numbers.length())));
        }
        if (userProperties.getPassword().isIncludeSpecialChars()) {
            password.append(userProperties.getPassword().getSpecialCharacters()
                    .charAt(random.nextInt(userProperties.getPassword().getSpecialCharacters().length())));
        }

        // 填充剩余长度
        int remainingLength = userProperties.getPassword().getLength() - password.length();
        for(int i = 0; i < remainingLength; i++) {
            password.append(charPool.charAt(random.nextInt(charPool.length())));
        }

        // 随机打乱密码字符顺序
        for(int i = 0; i < password.length(); i++) {
            int randomPos = random.nextInt(password.length());
            char temp = password.charAt(i);
            password.setCharAt(i, password.charAt(randomPos));
            password.setCharAt(randomPos, temp);
        }

        return password.toString();
    }

    /**
     * 创建默认角色
     */
    private Role createDefaultRole(String name, String description) {
        Role role = new Role(name, description);
        Role savedRole = roleRepository.save(role);
        log.info("创建默认角色: name={}", name);
        return savedRole;
    }

    /**
     * 转换 User 实体为 UserDTO
     */
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            dto.setRoles(roleNames);
        }

        return dto;
    }

    /**
     * 使指定用户的所有活动会话失效
     */
    private void expireUserSessions(User user) {
        if (sessionRegistry != null) {
            // 获取与用户关联的所有会话信息
            java.util.List<org.springframework.security.core.session.SessionInformation> sessions =
                sessionRegistry.getAllSessions(user, false);

            // 遍历并使每个会话过期
            for (org.springframework.security.core.session.SessionInformation session : sessions) {
                session.expireNow();
            }

            log.info("已使用户的所有会话失效: userId={}, username={}", user.getId(), user.getUsername());
        } else {
            log.warn("SessionRegistry 未初始化，无法使会话失效");
        }
    }
}
