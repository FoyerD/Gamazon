package Domain.User;

import java.util.Arrays;

import org.passay.*;

public class PasswordChecker {
    public static void check(String password) {
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
