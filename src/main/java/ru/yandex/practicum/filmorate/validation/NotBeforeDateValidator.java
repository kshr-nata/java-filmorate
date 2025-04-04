package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class NotBeforeDateValidator implements ConstraintValidator<NotBeforeDate, LocalDate> {
    private LocalDate minDate;

    @Override
    public void initialize(NotBeforeDate constraintAnnotation) {
        this.minDate = LocalDate.parse(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true;
        }
        return !date.isBefore(minDate);
    }
}