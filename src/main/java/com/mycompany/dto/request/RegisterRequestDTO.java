package com.mycompany.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class RegisterRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    String username;

    @NotBlank(message = "Password cannot be blank")
    String password;

    @NotBlank(message = "Confirm password cannot be blank")
    String confirmPassword;

    @NotBlank(message = "Name cannot be blank")
    String fullName;

}
