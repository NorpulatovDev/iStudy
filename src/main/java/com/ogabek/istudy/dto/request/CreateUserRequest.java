package com.ogabek.istudy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserRequest {
    @NotBlank(message = "Username is required!")
    private String username;
    @NotBlank(message = "Password is required!")
    private String password;

    private String role;
    private Long branchId; // Required for Admin, null for SuperAdmin
}
