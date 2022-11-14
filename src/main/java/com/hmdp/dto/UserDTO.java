package com.hmdp.dto;

import lombok.Data;
//为什么要使用UserDTO
/*
因为用User存在session并且返回前端的话，就会出现两个问题：
太占内存，用户信息会泄露
就涉及到了一个存储粒度的问题
* */
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
