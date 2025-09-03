package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class StudentDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long branchId;
    private String branchName;
    private LocalDateTime createdAt;
}
