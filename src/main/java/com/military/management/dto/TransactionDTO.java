package com.military.management.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionDTO {
    private String transactionType; // PURCHASE, TRANSFER, ASSIGNMENT, EXPENDITURE
    private Long equipmentTypeId;
    private Integer quantity;
    private Long fromBaseId;
    private Long toBaseId;
    private String personnelName;
    private String notes;
    private LocalDateTime createdAt;
}
