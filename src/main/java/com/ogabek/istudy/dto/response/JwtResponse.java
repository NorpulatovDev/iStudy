package com.ogabek.istudy.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class JwtResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String role;
    private Long  branchId;
    private String branchName;

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
