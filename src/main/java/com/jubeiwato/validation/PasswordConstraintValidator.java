package com.jubeiwato.validation;


import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSWORD_PATTERN =
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";
    //Password must contain at least 1 digit, 1 lowercase, 1 uppercase, 1 special character and be 8+ characters long        

    private Pattern pattern = Pattern.compile(PASSWORD_PATTERN); //Pattern.compile(...) compiles the string regex into a Pattern object.Done only once for efficiency (static pattern reuse).

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return pattern.matcher(password).matches();
    }
}