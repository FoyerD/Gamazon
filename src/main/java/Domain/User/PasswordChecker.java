package Domain.User;

import java.util.Arrays;

import org.passay.*;

public class PasswordChecker {

    /**
     * Checks if the given password meets the complexity requirements.
     * Requirements:
     * <ul>
     *   <li>At least 8 characters long</li>
     *   <li>At most 64 characters long</li>
     *   <li>At least 1 uppercase letter</li>
     *   <li>At least 1 lowercase letter</li>
     *   <li>At least 1 digit</li>
     *   <li>At least 1 special character</li>
     *   <li>No whitespace characters</li>
     * </ul>
     *
     * @param password The password to check.
     * @throws IllegalStateException if the password does not meet the requirements.
     */

    public static void check(String password) throws IllegalStateException {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
            new LengthRule(8, 64),
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            new CharacterRule(EnglishCharacterData.Digit, 1),
            new CharacterRule(EnglishCharacterData.Special, 1),
            new WhitespaceRule() // no spaces
        ));

        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            throw new IllegalStateException("Password does not meet complexity requirements: " + String.join(", ", validator.getMessages(result)));
        }
    }
}
