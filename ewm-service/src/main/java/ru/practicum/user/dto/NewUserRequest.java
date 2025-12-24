package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import ru.practicum.exceptions.CreateValidation;

@Data
public class NewUserRequest {

    @NotBlank(message = "Email не может быть пустым", groups = CreateValidation.class)
    @Email(message = "Некорректный формат почты", groups = CreateValidation.class)
    @Length(min = 6, max = 254, message = "Email должно содержать от 6 до 254 символов", groups = CreateValidation.class)
    private String email;

    @NotBlank(message = "Имя не может быть пустым", groups = CreateValidation.class)
    @Length(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов", groups = CreateValidation.class)
    private String name;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
