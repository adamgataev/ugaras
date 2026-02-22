package com.amac.ugaras.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.amac.ugaras.exceptions.GlobalExceptionHandler;
import com.amac.ugaras.exceptions.ResourceNotFoundException;
import com.amac.ugaras.models.dtos.user.UserRequestDto;
import com.amac.ugaras.models.dtos.user.UserResponseDto;
import com.amac.ugaras.models.dtos.user.UserUpdateRequestDto;
import com.amac.ugaras.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({ GlobalExceptionHandler.class, UserControllerTest.ObjectMapperConfig.class })
class UserControllerTest {

    @TestConfiguration
    static class ObjectMapperConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users returns 201 and Location header")
    void createSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        UserRequestDto request = new UserRequestDto("+31612345678", "user@test.com", "password123");
        UserResponseDto response = new UserResponseDto(id, "+31612345678", "user@test.com", true, Instant.now());

        when(userService.create(any(UserRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.phoneNumber").value("+31612345678"))
                .andExpect(jsonPath("$.email").value("user@test.com"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/users returns 400 when validation fails")
    void createValidationFailure() throws Exception {
        UserRequestDto invalid = new UserRequestDto("", null, "short");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} returns 200 with user")
    void getByIdSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponseDto dto = new UserResponseDto(id, "+316", "a@b.com", true, Instant.now());
        when(userService.getById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.phoneNumber").value("+316"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} returns 404 when not found")
    void getByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getById(id)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("GET /api/v1/users/by-phone/{phone} returns 200")
    void getByPhoneSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        String phone = "0612345678";
        UserResponseDto dto = new UserResponseDto(id, phone, "x@y.com", true, Instant.now());
        when(userService.getByPhoneNumber(phone)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/users/by-phone/{phone}", phone))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value(phone));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} returns 200 with updated user")
    void updateSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        UserUpdateRequestDto request = new UserUpdateRequestDto("+316999", "new@mail.com", null, null);
        UserResponseDto dto = new UserResponseDto(id, "+316999", "new@mail.com", true, Instant.now());
        when(userService.update(eq(id), any(UserUpdateRequestDto.class))).thenReturn(dto);

        mockMvc.perform(put("/api/v1/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phoneNumber").value("+316999"))
                .andExpect(jsonPath("$.email").value("new@mail.com"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} returns 204")
    void deleteSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/users/" + id))
                .andExpect(status().isNoContent());
    }
}
