package com.example.dto;


import lombok.Data;

@Data
public class UserDTO {
    private String name;
    private String email;
    private String password;
    private String roles;
}
