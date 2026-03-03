package com.original.security.user.api.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PermissionDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
