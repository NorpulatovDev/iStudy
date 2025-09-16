package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateUserRequest;
import com.ogabek.istudy.dto.request.LoginRequest;
import com.ogabek.istudy.dto.response.JwtResponse;
import com.ogabek.istudy.dto.response.UserDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.entity.Role;
import com.ogabek.istudy.entity.User;
import com.ogabek.istudy.repository.BranchRepository;
import com.ogabek.istudy.repository.UserRepository;
import com.ogabek.istudy.security.JwtUtils;
import com.ogabek.istudy.service.RefreshTokenService;
import com.ogabek.istudy.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        JwtResponse response = new JwtResponse(jwt, refreshToken.getToken());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole().name());

        if (user.getBranch() != null) {
            response.setBranchId(user.getBranch().getId());
            response.setBranchName(user.getBranch().getName());
        }

        return response;
    }

    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));
            user.setBranch(branch);
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAllWithBranch().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersByBranch(Long branchId) {
        return userRepository.findByBranchId(branchId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDto(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public JwtResponse refreshToken(String refreshTokenStr) {
        return refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateJwtToken(user.getUsername());
                    JwtResponse response = new JwtResponse(token, refreshTokenStr);
                    response.setUserId(user.getId());
                    response.setUsername(user.getUsername());
                    response.setRole(user.getRole().name());

                    if (user.getBranch() != null) {
                        response.setBranchId(user.getBranch().getId());
                        response.setBranchName(user.getBranch().getName());
                    }

                    return response;
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    @Transactional
    public UserDto updateUser(Long id, CreateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // Check if username is being changed and if new username already exists
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        // Update basic fields
        user.setUsername(request.getUsername());

        // Only update password if provided (not empty)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Update role
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        // Update branch assignment
        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new RuntimeException("Branch not found with id: " + request.getBranchId()));
            user.setBranch(branch);
        } else {
            user.setBranch(null); // Remove branch assignment (for SUPER_ADMIN)
        }

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());

        if (user.getBranch() != null) {
            dto.setBranchId(user.getBranch().getId());
            dto.setBranchName(user.getBranch().getName());
        }

        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}