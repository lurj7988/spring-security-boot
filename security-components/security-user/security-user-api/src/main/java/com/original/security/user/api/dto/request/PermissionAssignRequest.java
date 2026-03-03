package com.original.security.user.api.dto.request;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class PermissionAssignRequest {
    @NotEmpty(message = "Permissions list cannot be empty")
    private List<Long> permissionIds;
}
