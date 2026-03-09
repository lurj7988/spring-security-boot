package com.original.security.user.api.dto.request;

import javax.validation.constraints.NotBlank;

public class RoleCreateRequest {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotBlank(message = "Role name cannot be blank")
    private String name;
    
    private String description;
}
