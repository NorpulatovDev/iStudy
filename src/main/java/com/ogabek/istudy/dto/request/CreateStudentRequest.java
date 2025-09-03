package com.ogabek.istudy.dto.request;


import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateStudentRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Long branchId;
}
