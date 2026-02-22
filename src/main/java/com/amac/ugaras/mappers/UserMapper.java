package com.amac.ugaras.mappers;

import com.amac.ugaras.models.dtos.user.UserRequestDto;
import com.amac.ugaras.models.dtos.user.UserResponseDto;
import com.amac.ugaras.models.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    // Van DTO naar Entity (Input)
    // We vullen het passwordHash nog niet in, dat doet de Service!
    public User toEntity(UserRequestDto dto) {
        return User.builder()
                .phoneNumber(dto.phoneNumber())
                .email(dto.email())
                .enabled(true)
                .build();
    }
}
