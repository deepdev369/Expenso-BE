package com.holytrinity.expenso.user.application.dto;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Validate that the email value isn't taken yet.
 */
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = UserEmailUnique.UserEmailUniqueValidator.class)
public @interface UserEmailUnique {

    String message() default "{Exists.user.email}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class UserEmailUniqueValidator implements ConstraintValidator<UserEmailUnique, String> {

        private final UserPort userPort;
        private final HttpServletRequest request;

        public UserEmailUniqueValidator(final UserPort userPort,
                final HttpServletRequest request) {
            this.userPort = userPort;
            this.request = request;
        }

        @Override
        public boolean isValid(final String value, final ConstraintValidatorContext cvContext) {
            if (value == null) {
                // no value present
                return true;
            }
            @SuppressWarnings("unchecked")
            final Map<String, String> pathVariables = ((Map<String, String>) request
                    .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
            final String currentId = pathVariables.get("userId");
            if (currentId != null) {
                String currentEmail = userPort.loadUser(Long.parseLong(currentId))
                        .map(User::getEmail)
                        .orElse(null);
                if (value.equalsIgnoreCase(currentEmail)) {
                    // value hasn't changed
                    return true;
                }
            }
            return !userPort.existsByEmail(value);
        }

    }

}
