package com.hotel.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {

    @NotNull(message = "請選擇商品")
    private Long productId;

    @NotNull(message = "請輸入數量")
    @Min(value = 1, message = "數量至少為 1")
    private Integer quantity;
}