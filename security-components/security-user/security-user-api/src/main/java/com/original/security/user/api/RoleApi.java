package com.original.security.user.api;

import com.original.security.core.Response;
import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.RoleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@FeignClient(name = "security-user-role-api", url = "${security.user.api.url:}")
@RequestMapping("/api/roles")
public interface RoleApi {

    @PostMapping
    Response<RoleDTO> createRole(@Valid @RequestBody RoleCreateRequest request);

    @PostMapping("/{roleId}/permissions")
    Response<Void> assignPermissions(@PathVariable("roleId") Long roleId, 
                                     @Valid @RequestBody PermissionAssignRequest request);

    @GetMapping("/{roleId}")
    Response<RoleDTO> getRole(@PathVariable("roleId") Long roleId);

    @GetMapping
    Response<PageDTO<RoleDTO>> listRoles(@RequestParam(value = "page", defaultValue = "0") int page,
                                         @RequestParam(value = "size", defaultValue = "10") int size);

    /**
     * 手动清除权限和角色缓存 (AC 2)
     */
    @DeleteMapping("/cache")
    Response<Void> clearCache(@RequestParam(value = "username", required = false) String username);
}
