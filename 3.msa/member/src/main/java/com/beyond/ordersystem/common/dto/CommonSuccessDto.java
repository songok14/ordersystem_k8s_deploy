package com.beyond.ordersystem.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonSuccessDto {
    private Object result;
    private int statusCode;
    private String statusMessage;
}
