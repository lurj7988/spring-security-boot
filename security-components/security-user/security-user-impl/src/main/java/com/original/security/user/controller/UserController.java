package com.original.security.user.controller;

import com.original.security.core.Response;
import com.original.security.user.api.UserApi;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.exception.EmailAlreadyExistsException;
import com.original.security.user.exception.UserAlreadyExistsException;
import com.original.security.user.exception.UserDisabledException;
import com.original.security.user.exception.UserNotFoundException;
import com.original.security.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * 用户控制器
 * 实现 UserApi 接口
 *
 * @author Original Security Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    /**
     * 业务错误码常量
     */
    private static final String ERROR_CODE_USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    private static final String ERROR_CODE_EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    private static final String ERROR_CODE_INVALID_REQUEST = "INVALID_REQUEST";
    private static final String ERROR_CODE_USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String ERROR_CODE_USER_DISABLED = "USER_DISABLED";
    private static final String ERROR_CODE_UNAUTHORIZED = "UNAUTHORIZED";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Response<UserDTO> createUser(UserCreateRequest request) {
        UserDTO user = userService.createUser(request);
        return Response.successBuilder(user).msg("success").build();
    }

    @Override
    public Response<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return Response.successBuilder(user).msg("success").build();
    }

    @Override
    public Response<UserDTO> getUser(Long userId) {
        UserDTO user = userService.getUser(userId);
        return Response.successBuilder(user).msg("success").build();
    }

    @Override
    public Response<PageDTO<UserDTO>> listUsers(int page, int size, String username, Boolean enabled) {
        PageDTO<UserDTO> users = userService.listUsers(page, size, username, enabled);
        return Response.successBuilder(users).msg("success").build();
    }

    /**
     * 处理用户名已存在异常
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public Response<Void> handleUserAlreadyExists(UserAlreadyExistsException e) {
        return Response.<Void>withBuilder(400)
                .msg("[" + ERROR_CODE_USER_ALREADY_EXISTS + "] " + e.getMessage())
                .build();
    }

    /**
     * 处理邮箱已存在异常
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public Response<Void> handleEmailAlreadyExists(EmailAlreadyExistsException e) {
        return Response.<Void>withBuilder(400)
                .msg("[" + ERROR_CODE_EMAIL_ALREADY_EXISTS + "] " + e.getMessage())
                .build();
    }

    /**
     * 处理 JSR-303 输入校验失败，返回字段级错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Response.<Void>withBuilder(400)
                .msg("[" + ERROR_CODE_INVALID_REQUEST + "] " + message)
                .build();
    }

    /**
     * 处理用户不存在异常
     */
    @ExceptionHandler(UserNotFoundException.class)
    public Response<Void> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("用户不存在: {}", e.getIdentifier());
        return Response.<Void>withBuilder(404)
                .msg("[" + ERROR_CODE_USER_NOT_FOUND + "] " + e.getMessage())
                .build();
    }

    /**
     * 处理用户已禁用异常
     */
    @ExceptionHandler(UserDisabledException.class)
    public Response<Void> handleUserDisabledException(UserDisabledException e) {
        log.warn("用户已禁用: {}", e.getUsername());
        return Response.<Void>withBuilder(403)
                .msg("[" + ERROR_CODE_USER_DISABLED + "] " + e.getMessage())
                .build();
    }

    /**
     * 处理认证异常（用户未认证）
     */
    @ExceptionHandler(IllegalStateException.class)
    public Response<Void> handleIllegalStateException(IllegalStateException e) {
        log.warn("认证异常: {}", e.getMessage());
        return Response.<Void>withBuilder(401)
                .msg("[" + ERROR_CODE_UNAUTHORIZED + "] " + e.getMessage())
                .build();
    }
}
