package com.hotel.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "姓名不可為空")
    @Size(max = 100, message = "姓名最多 100 字")
    private String name;

    @Size(max = 20, message = "電話最多 20 字")
    private String phone;

    @Size(max = 500, message = "大頭貼網址過長")
    private String avatarUrl;
}