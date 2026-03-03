package com.original.security.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.original.security.user.api.dto.request.PermissionAssignRequest;
import com.original.security.user.api.dto.request.RoleCreateRequest;
import com.original.security.user.api.dto.response.PageDTO;
import com.original.security.user.api.dto.response.RoleDTO;
import com.original.security.user.event.RoleCacheEvictionListener;
import com.original.security.user.event.RolePermissionAssignedEventListener;
import com.original.security.user.service.PermissionService;
import com.original.security.user.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private PermissionService permissionService;

    /**
     * 显式 Mock 事件监听器，防止 @WebMvcTest 切片加载异步/事务组件
     */
    @MockBean
    private RolePermissionAssignedEventListener eventListener;

    @MockBean
    private RoleCacheEvictionListener cacheEvictionListener;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testCreateRole_ValidRequest_ReturnsSuccess() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("TEST_ROLE");
        request.setDescription("Test Description");

        RoleDTO responseDto = new RoleDTO();
        responseDto.setId(1L);
        responseDto.setName("TEST_ROLE");

        when(roleService.createRole(any(RoleCreateRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("TEST_ROLE"));

        verify(roleService).createRole(any(RoleCreateRequest.class));
    }

    @Test
    @WithMockUser
    void testCreateRole_DuplicateRoleName_Returns400() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName("EXISTING_ROLE");

        when(roleService.createRole(any(RoleCreateRequest.class)))
                .thenThrow(new IllegalArgumentException("Role name already exists: EXISTING_ROLE"));

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Role name already exists: EXISTING_ROLE"));
    }

    @Test
    @WithMockUser
    void testCreateRole_BlankName_Returns400() throws Exception {
        RoleCreateRequest request = new RoleCreateRequest();
        request.setName(""); // blank name

        mockMvc.perform(post("/api/roles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(roleService, never()).createRole(any());
    }

    @Test
    @WithMockUser
    void testAssignPermissions_ValidRequest_ReturnsSuccess() throws Exception {
        PermissionAssignRequest request = new PermissionAssignRequest();
        request.setPermissionIds(Collections.singletonList(1L));

        doNothing().when(roleService).assignPermissions(eq(1L), any(PermissionAssignRequest.class));

        mockMvc.perform(post("/api/roles/1/permissions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roleService).assignPermissions(eq(1L), any(PermissionAssignRequest.class));
    }

    @Test
    @WithMockUser
    void testGetRole_ExistingRole_ReturnsRoleDetails() throws Exception {
        RoleDTO responseDto = new RoleDTO();
        responseDto.setId(1L);
        responseDto.setName("TEST_ROLE");

        when(roleService.getRole(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser
    void testListRoles_ValidPagination_ReturnsPaginatedResults() throws Exception {
        PageDTO<RoleDTO> pageDTO = new PageDTO<>();
        pageDTO.setTotalElements(1);
        RoleDTO role = new RoleDTO();
        role.setName("TEST_ROLE");
        pageDTO.setContent(Collections.singletonList(role));

        when(roleService.listRoles(0, 10)).thenReturn(pageDTO);

        mockMvc.perform(get("/api/roles")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].name").value("TEST_ROLE"));
    }

    @Test
    @WithMockUser
    void testClearCache_SpecificUser_ReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/roles/cache")
                .param("username", "admin")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roleService).clearCache("admin");
        verify(permissionService).clearCache("admin");
    }

    @Test
    @WithMockUser
    void testClearCache_AllUsers_ReturnsSuccess() throws Exception {
        mockMvc.perform(delete("/api/roles/cache")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(roleService).clearAllCache();
        verify(permissionService).clearAllCache();
    }
}