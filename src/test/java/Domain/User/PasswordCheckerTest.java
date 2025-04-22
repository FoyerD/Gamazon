package Domain.User;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordCheckerTest {

    @Test
    public void testValidPassword() {
        // A valid password that meets all criteria
        String validPassword = "StrongP@ssw0rd";
        
        try {
            PasswordChecker.check(validPassword);
        } catch (IllegalStateException e) {
            fail("Password should be valid, but it failed: " + e.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordTooShort() {
        // Password is too short (less than 8 characters)
        String shortPassword = "Short1!";
        PasswordChecker.check(shortPassword);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordWithoutUppercase() {
        // Password is missing an uppercase letter
        String noUpperCasePassword = "lowercase1!";
        PasswordChecker.check(noUpperCasePassword);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordWithoutLowercase() {
        // Password is missing a lowercase letter
        String noLowerCasePassword = "UPPERCASE1!";
        PasswordChecker.check(noLowerCasePassword);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordWithoutDigit() {
        // Password is missing a digit
        String noDigitPassword = "NoDigit!";
        PasswordChecker.check(noDigitPassword);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordWithoutSpecialCharacter() {
        // Password is missing a special character
        String noSpecialCharPassword = "NoSpecial123";
        PasswordChecker.check(noSpecialCharPassword);
    }

    @Test(expected = IllegalStateException.class)
    public void testPasswordWithWhitespace() {
        // Password contains whitespace
        String passwordWithWhitespace = "Password 1!";
        PasswordChecker.check(passwordWithWhitespace);
    }
}
