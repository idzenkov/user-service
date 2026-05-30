package org.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

public class UpdateUserRequest {
    private String name;
    @Email(message = "Invalid email format")
    private String email;
    @Min(value = 0, message = "Age must be >= 0")
    private Integer age;

    // геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
}