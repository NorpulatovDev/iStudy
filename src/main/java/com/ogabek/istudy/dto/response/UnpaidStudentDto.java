package com.ogabek.istudy.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter
public class UnpaidStudentDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private BigDecimal remainingAmount;
    private Long groupId;
    
    public UnpaidStudentDto(Long id, String firstName, String lastName, String phoneNumber, 
                           BigDecimal remainingAmount, Long groupId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.remainingAmount = remainingAmount;
        this.groupId = groupId;
    }
}