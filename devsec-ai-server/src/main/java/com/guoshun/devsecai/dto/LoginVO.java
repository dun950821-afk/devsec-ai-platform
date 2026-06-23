package com.guoshun.devsecai.dto;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private UserInfoVO userInfo;
    private Long expireTime;
    
    @Data
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String realName;
        private String email;
        private String phone;
        private String role;
    }
}
