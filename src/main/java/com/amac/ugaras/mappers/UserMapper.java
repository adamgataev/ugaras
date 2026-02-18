package com.amac.ugaras.mappers;

import com.amac.ugaras.models.dtos.user.UserRequestDto;
import com.amac.ugaras.models.dtos.user.UserResponseDto;
import com.amac.ugaras.models.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    // Van Entity naar DTO (Output)
    public UserResponseDto toDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    // Van DTO naar Entity (Input)
    // We vullen het passwordHash nog niet in, dat doet de Service!
    public User toEntity(UserRequestDto dto) {
        return User.builder()
                .email(dto.email())
                // enabled is standaard true door @Builder.Default in de entity
                .build();
    }
}
