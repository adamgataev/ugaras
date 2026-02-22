package com.amac.ugaras.services;

import com.amac.ugaras.exceptions.ResourceNotFoundException;
import com.amac.ugaras.mappers.UserMapper;
import com.amac.ugaras.models.dtos.user.UserRequestDto;
import com.amac.ugaras.models.dtos.user.UserResponseDto;
import com.amac.ugaras.models.dtos.user.UserUpdateRequestDto;
import com.amac.ugaras.models.entities.User;
import com.amac.ugaras.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Transactional
    public UserResponseDto create(UserRequestDto request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException(
                    getMessage("exception.user.phone.duplicate", request.phoneNumber()));
        }
        if (request.email() != null && !request.email().isBlank()
                && userRepository.existsByEmail(request.email().trim())) {
            throw new IllegalArgumentException(
                    getMessage("exception.user.email.duplicate", request.email()));
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("exception.user.not.found", id)));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("exception.user.not.found", phoneNumber)));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserResponseDto update(UUID id, UserUpdateRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("exception.user.not.found", id)));

        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            if (userRepository.existsByPhoneNumber(request.phoneNumber())
                    && !request.phoneNumber().equals(user.getPhoneNumber())) {
                throw new IllegalArgumentException(
                        getMessage("exception.user.phone.duplicate", request.phoneNumber()));
            }
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.email() != null) {
            String email = request.email().isBlank() ? null : request.email().trim();
            if (email != null && userRepository.existsByEmail(email)
                    && !email.equals(user.getEmail())) {
                throw new IllegalArgumentException(
                        getMessage("exception.user.email.duplicate", email));
            }
            user.setEmail(email);
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("exception.user.not.found", id)));
        user.softDelete();
        userRepository.save(user);
    }

    private String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
