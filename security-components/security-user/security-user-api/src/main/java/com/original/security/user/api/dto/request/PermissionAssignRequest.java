package com.original.security.user.api.dto.request;

import javax.validation.constraints.NotEmpty;
import java.util.List;


public class PermissionAssignRequest {
    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    @NotEmpty(message = "Permissions list cannot be empty")
    private List<Long> permissionIds;
}
