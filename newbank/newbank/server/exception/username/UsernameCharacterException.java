package newbank.server.exception.username;

public class UsernameCharacterException extends newbank.server.exception.username.UsernameException {
    public UsernameCharacterException() {
        super("Username must contain only alphabetic characters and numeric digits");
    }
}
