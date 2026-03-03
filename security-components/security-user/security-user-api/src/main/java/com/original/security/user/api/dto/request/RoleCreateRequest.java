package com.original.security.user.api.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RoleCreateRequest {
    @NotBlank(message = "Role name cannot be blank")
    private String name;
    
    private String description;
}
