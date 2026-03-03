package com.original.security.user.controller;

import com.original.security.core.Response;
import com.original.security.user.api.RoleApi;
import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.RoleDTO;
import com.original.security.user.service.RoleService;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController implements RoleApi {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public Response<RoleDTO> createRole(RoleCreateRequest request) {
        RoleDTO role = roleService.createRole(request);
        return Response.successBuilder(role).build();
    }

    @Override
    public Response<Void> assignPermissions(Long roleId, PermissionAssignRequest request) {
        roleService.assignPermissions(roleId, request);
        return Response.<Void>successBuilder(null).build();
    }

    @Override
    public Response<RoleDTO> getRole(Long roleId) {
        RoleDTO role = roleService.getRole(roleId);
        return Response.successBuilder(role).build();
    }

    @Override
    public Response<PageDTO<RoleDTO>> listRoles(int page, int size) {
        PageDTO<RoleDTO> roles = roleService.listRoles(page, size);
        return Response.successBuilder(roles).build();
    }

    /**
     * 处理业务规则冲突（如角色名重复）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Response<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        return Response.<Void>withBuilder(400).msg(e.getMessage()).build();
    }

    /**
     * 处理 JSR-303 输入校验失败，返回字段级错误信息（HIGH-3 修复）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Response.<Void>withBuilder(400).msg(message).build();
    }
}
