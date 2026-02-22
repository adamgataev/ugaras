package com.amac.ugaras.services;

import com.amac.ugaras.exceptions.ResourceNotFoundException;
import com.amac.ugaras.mappers.UserMapper;
import com.amac.ugaras.models.dtos.user.UserRequestDto;
import com.amac.ugaras.models.dtos.user.UserResponseDto;
import com.amac.ugaras.models.dtos.user.UserUpdateRequestDto;
import com.amac.ugaras.models.entities.User;
import com.amac.ugaras.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("create encodes password and returns DTO")
    void createSuccess() {
        UserRequestDto request = new UserRequestDto("+31612345678", "a@b.com", "password123");
        User entity = User.builder().phoneNumber(request.phoneNumber()).email(request.email()).build();
        entity.setId(UUID.randomUUID());
        UserResponseDto dto = new UserResponseDto(entity.getId(), entity.getPhoneNumber(), entity.getEmail(), true, null);

        when(userRepository.existsByPhoneNumber("+31612345678")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(entity);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(dto);

        UserResponseDto result = userService.create(request);

        assertThat(result).isEqualTo(dto);
        verify(userRepository).save(argThat(u -> "hashed".equals(u.getPasswordHash())));
    }

    @Test
    @DisplayName("create throws when phone already exists")
    void createDuplicatePhone() {
        UserRequestDto request = new UserRequestDto("+31612345678", "a@b.com", "password123");
        when(userRepository.existsByPhoneNumber("+31612345678")).thenReturn(true);
        when(messageSource.getMessage(eq("exception.user.phone.duplicate"), any(), any())).thenReturn("Duplicate phone");

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate phone");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("getById returns DTO when found")
    void getByIdFound() {
        UUID id = UUID.randomUUID();
        User user = User.builder().phoneNumber("+316").email("x@y.com").passwordHash("h").build();
        user.setId(id);
        UserResponseDto dto = new UserResponseDto(id, "+316", "x@y.com", true, null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        assertThat(userService.getById(id)).isEqualTo(dto);
    }

    @Test
    @DisplayName("getById throws ResourceNotFoundException when not found")
    void getByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq("exception.user.not.found"), any(), any())).thenReturn("Not found");

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Not found");
    }

    @Test
    @DisplayName("update changes only provided fields")
    void updatePartial() {
        UUID id = UUID.randomUUID();
        User user = User.builder().phoneNumber("+316111").email("old@x.com").passwordHash("old").enabled(true).build();
        user.setId(id);
        UserUpdateRequestDto update = new UserUpdateRequestDto("+316222", null, null, null);
        UserResponseDto dto = new UserResponseDto(id, "+316222", "old@x.com", true, null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneNumber("+316222")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(dto);

        UserResponseDto result = userService.update(id, update);

        assertThat(result.phoneNumber()).isEqualTo("+316222");
        verify(userRepository).save(argThat(u -> "+316222".equals(u.getPhoneNumber()) && "old@x.com".equals(u.getEmail())));
    }

    @Test
    @DisplayName("delete soft-deletes user")
    void deleteSuccess() {
        UUID id = UUID.randomUUID();
        User user = User.builder().phoneNumber("+316").email("x@y.com").passwordHash("h").build();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.delete(id);

        verify(userRepository).save(argThat(u -> u.getDeletedAt() != null));
    }
}
