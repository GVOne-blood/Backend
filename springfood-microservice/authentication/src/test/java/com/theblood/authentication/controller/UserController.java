package com.theblood.authentication.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theblood.authentication.dto.request.UserRequest;
import com.theblood.authentication.dto.response.UserDetail;
import com.theblood.authentication.model.User;
import com.theblood.authentication.service.AuthService;
import com.theblood.authentication.service.UserService;
import com.theblood.springfood.common.dto.request.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("UserController Unit Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private UserDetail userDetail;
    private CustomUserPrincipal mockPrincipal;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        // Setup mock user principal
        mockPrincipal = new CustomUserPrincipal();
        mockPrincipal.setUserId(testUserId);
        mockPrincipal.setUsername("testuser");

        // Setup mock user detail
        userDetail = new UserDetail();
        userDetail.setId(String.valueOf(testUserId));
        userDetail.setUsername("testuser");
        userDetail.setEmail("test@example.com");
    }

    @Test
    @DisplayName("GET /user/ - Should return list of users with pagination")
    void listUsers_ShouldReturnPagedUsers() throws Exception {
        // Arrange
        UserDetail user1 = new UserDetail();
        user1.setId(String.valueOf(UUID.randomUUID()));
        user1.setUsername("user1");

        UserDetail user2 = new UserDetail();
        user2.setId(String.valueOf(UUID.randomUUID()));
        user2.setUsername("user2");

        Page<UserDetail> userPage = new PageImpl<>(
                Arrays.asList(user1, user2),
                PageRequest.of(0, 5),
                2
        );

        when(userService.getListUsers(any(Pageable.class))).thenReturn(userPage);

        // Act & Assert
        mockMvc.perform(get("/user/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get list users successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].username").value("user1"))
                .andExpect(jsonPath("$.data.content[1].username").value("user2"));

        // Verify service was called with correct pageable
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService, times(1)).getListUsers(pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageSize()).isEqualTo(5);
        assertThat(capturedPageable.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("GET /user/ - Should handle custom pagination parameters")
    void listUsers_WithCustomPagination_ShouldReturnCorrectPage() throws Exception {
        // Arrange
        Page<UserDetail> emptyPage = new PageImpl<>(
                Arrays.asList(),
                PageRequest.of(2, 10),
                0
        );

        when(userService.getListUsers(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/user/")
                        .param("page", "2")
                        .param("size", "10")
                        .param("sort", "username,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)))
                .andExpect(jsonPath("$.data.number").value(2))
                .andExpect(jsonPath("$.data.size").value(10));

        verify(userService, times(1)).getListUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    @DisplayName("GET /user/profile - Should return user profile for authenticated user")
    void getUserById_WithValidAuth_ShouldReturnUserProfile() throws Exception {
        // Arrange
        when(userService.getUserDetail(testUserId)).thenReturn(userDetail);

        // Act & Assert
        mockMvc.perform(get("/user/profile")
                        .with(authentication(createAuthentication(mockPrincipal)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Get user detail successfully"))
                .andExpect(jsonPath("$.data.id").value(testUserId.toString()))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService, times(1)).getUserDetail(testUserId);
    }

    @Test
    @DisplayName("GET /user/profile - Should return 401 for unauthenticated user")
    void getUserById_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserDetail(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "GUEST")
    @DisplayName("GET /user/profile - Should return 403 for user without CUSTOMER role")
    void getUserById_WithoutCustomerRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserDetail(any(UUID.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    @DisplayName("PUT /user/profile - Should update user profile successfully")
    void updateUser_WithValidData_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@example.com");

        UserDetail updatedUser = new UserDetail();
        updatedUser.setId(String.valueOf(testUserId));
        updatedUser.setUsername("updateduser");
        updatedUser.setEmail("updated@example.com");

        when(userService.updateUser(eq(testUserId), any(UserRequest.class)))
                .thenReturn(updatedUser);

        // Act & Assert
        mockMvc.perform(put("/user/profile")
                        .with(authentication(createAuthentication(mockPrincipal)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.username").value("updateduser"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));

        // Verify with ArgumentCaptor
        ArgumentCaptor<UserRequest> requestCaptor = ArgumentCaptor.forClass(UserRequest.class);
        verify(userService, times(1)).updateUser(eq(testUserId), requestCaptor.capture());

        UserRequest capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getUsername()).isEqualTo("updateduser");
        assertThat(capturedRequest.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    @DisplayName("DELETE /user/profile/{id} - Should delete other user successfully")
    void deleteUserById_DifferentUser_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        UUID targetUserId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setUsername("otheruser");

        when(userService.findById(targetUserId)).thenReturn(targetUser);
        doNothing().when(userService).deleteUser(targetUserId);

        // Act & Assert
        mockMvc.perform(delete("/user/profile/{id}", targetUserId)
                        .with(authentication(createAuthentication(mockPrincipal)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));

        verify(userService, times(1)).findById(targetUserId);
        verify(userService, times(1)).deleteUser(targetUserId);
        verify(authService, never()).logout(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "CUSTOMER")
    @DisplayName("DELETE /user/profile/{id} - Should delete self and logout")
    void deleteUserById_SelfDelete_ShouldDeleteAndLogout() throws Exception {
        // Arrange
        User selfUser = new User();
        selfUser.setId(testUserId);
        selfUser.setUsername("testuser");

        when(userService.findById(testUserId)).thenReturn(selfUser);
        doNothing().when(userService).deleteUser(testUserId);
        doNothing().when(authService).logout(any(), any());

        // Act & Assert
        mockMvc.perform(delete("/user/profile/{id}", testUserId)
                        .with(authentication(createAuthentication(mockPrincipal)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204))
                .andExpect(jsonPath("$.message").value("Your account has been deleted"));

        verify(userService, times(1)).findById(testUserId);
        verify(userService, times(1)).deleteUser(testUserId);
        verify(authService, times(1)).logout(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("DELETE /user/profile/{id} - Admin should be able to delete users")
    void deleteUserById_AsAdmin_ShouldDeleteSuccessfully() throws Exception {
        // Arrange
        UUID targetUserId = UUID.randomUUID();
        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setUsername("someuser");

        CustomUserPrincipal adminPrincipal = new CustomUserPrincipal();
        adminPrincipal.setUserId(UUID.randomUUID());
        adminPrincipal.setUsername("admin");

        when(userService.findById(targetUserId)).thenReturn(targetUser);
        doNothing().when(userService).deleteUser(targetUserId);

        // Act & Assert
        mockMvc.perform(delete("/user/profile/{id}", targetUserId)
                        .with(authentication(createAuthentication(adminPrincipal)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(204));

        verify(userService, times(1)).deleteUser(targetUserId);
    }

    @Test
    @DisplayName("GET /user/search - Should search users with criteria")
    void searchUsers_WithCriteria_ShouldReturnResults() throws Exception {
        // Arrange
        Map<String, String> criteria = new HashMap<>();
        criteria.put("username", "test");
        criteria.put("email", "test@");

        UserDetail foundUser = new UserDetail();
        foundUser.setId(String.valueOf(testUserId));
        foundUser.setUsername("testuser");
        foundUser.setEmail("test@example.com");

        Page<UserDetail> searchResults = new PageImpl<>(
                Arrays.asList(foundUser),
                PageRequest.of(0, 5),
                1
        );

        when(userService.search(any(Pageable.class), eq(criteria)))
                .thenReturn(searchResults);

        // Act & Assert
        mockMvc.perform(get("/user/search")
                        .param("username", "test")
                        .param("email", "test@")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Search users successfully"))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].username").value("testuser"));

        verify(userService, times(1)).search(any(Pageable.class), eq(criteria));
    }

    @Test
    @DisplayName("GET /user/search - Should return 400 for invalid search criteria")
    void searchUsers_WithInvalidCriteria_ShouldReturnBadRequest() throws Exception {
        // Arrange
        when(userService.search(any(Pageable.class), any()))
                .thenThrow(new IllegalArgumentException("Invalid search criteria"));

        // Act & Assert
        mockMvc.perform(get("/user/search")
                        .param("invalid", "param")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Search failed")));
    }

    // Helper method to create authentication with custom principal
    private org.springframework.security.core.Authentication createAuthentication(CustomUserPrincipal principal) {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal,
                null,
                Arrays.asList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }
}
