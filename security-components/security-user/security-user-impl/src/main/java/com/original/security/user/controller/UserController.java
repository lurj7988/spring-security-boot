package com.original.security.user.controller;

import com.original.security.core.Response;
import com.original.security.user.api.UserApi;
import com.original.security.user.api.dto.request.UserCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.UserDTO;
import com.original.security.user.exception.EmailAlreadyExistsException;
import com.original.security.user.exception.UserAlreadyExistsException;
import com.original.security.user.service.UserService;
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

    /**
     * 业务错误码常量
     */
    private static final String ERROR_CODE_USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    private static final String ERROR_CODE_EMAIL_ALREADY_EXISTS = "EMAIL_ALREADY_EXISTS";
    private static final String ERROR_CODE_INVALID_REQUEST = "INVALID_REQUEST";

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
    public Response<PageDTO<UserDTO>> listUsers(int page, int size) {
        PageDTO<UserDTO> users = userService.listUsers(page, size);
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
     * 处理其他业务异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        // 根据异常类型确定 HTTP 状态码
        String message = e.getMessage();
        int code = 400; // 默认 400 Bad Request
        if (message != null) {
            // 资源不存在场景
            if (message.contains("用户不存在") || message.contains("userId") || message.contains("ID")) {
                code = 404;
            }
            // 参数验证失败场景
            else if (message.contains("无效") || message.contains("不符合") || message.contains("不合法")) {
                code = 400;
            }
        }
        return Response.<Void>withBuilder(code).msg(message).build();
    }
}
