package com.original.security.user.service.impl;

import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.config.UserProperties;
import com.original.security.user.entity.Role;
import com.original.security.user.entity.User;
import com.original.security.user.event.UserCreatedEvent;
import com.original.security.user.exception.EmailAlreadyExistsException;
import com.original.security.user.exception.UserAlreadyExistsException;
import com.original.security.user.repository.RoleRepository;
import com.original.security.user.repository.UserRepository;
import com.original.security.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
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

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserProperties userProperties;

    public UserServiceImpl(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          ApplicationEventPublisher eventPublisher,
                          UserProperties userProperties) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.userProperties = userProperties;
    }

    @Override
    @Transactional
    public UserDTO createUser(UserCreateRequest request) {
        log.info("创建用户请求: username={}, email={}", request.getUsername(), request.getEmail());

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("用户名已存在: username={}", request.getUsername());
            throw new UserAlreadyExistsException(request.getUsername());
        }

        // 检查邮箱是否已存在
        String email = request.getEmail();
        // 注意：@NotBlank 注解已处理 null 和空字符串，这里主要是防御性检查
        // 如果邮箱被使用过（已被其他人或之前创建），则拒绝
        if (email != null && !email.trim().isEmpty() && userRepository.existsByEmail(email)) {
            log.warn("邮箱已存在: email={}", email);
            throw new EmailAlreadyExistsException(email);
        }

        // 创建用户实体
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getEmail()
        );

        // 分配默认角色（通过配置支持）
        // 注意：在高并发场景下，多个用户可能同时创建并都获得首用户角色
        // 这是一个已知的限制，建议在后续 Story 中使用分布式锁或数据库触发器解决
        String defaultRoleName = "USER";
        String firstUserRoleName = "ADMIN";

        if (userRepository.count() == FIRST_USER_THRESHOLD) {
            // 注意：在高并发场景下，多个用户可能同时创建并都获得首用户角色
            // 这是一个已知的限制，建议在后续 Story 中使用分布式锁或数据库触发器解决
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

        // 保存用户
        User savedUser = userRepository.save(user);

        // 发布审计事件
        eventPublisher.publishEvent(
                new UserCreatedEvent(this, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail())
        );

        log.info("用户创建成功: userId={}, username={}", savedUser.getId(), savedUser.getUsername());

        return toDTO(savedUser);
    }

    @Override
    public UserDTO getCurrentUser() {
        // TODO: 从 Spring Security 上下文获取当前用户
        // 在 Story 5.3 中实现
        throw new UnsupportedOperationException("getCurrentUser() 将在 Story 5.3 中实现");
    }

    @Override
    public UserDTO getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在: userId={}", userId);
                    return new IllegalArgumentException("用户不存在");
                });

        return toDTO(user);
    }

    @Override
    public PageDTO<UserDTO> listUsers(int page, int size) {
        // 参数验证
        if (page < 0) {
            page = 0;
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            size = DEFAULT_PAGE_SIZE;
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);

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
}
