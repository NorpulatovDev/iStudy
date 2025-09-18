package com.ogabek.istudy.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String password;   // optional, if you want to allow password updates
    private String role;       // can be converted to Role enum in service layer
    private Long branchId;     // instead of embedding Branch entity
}
