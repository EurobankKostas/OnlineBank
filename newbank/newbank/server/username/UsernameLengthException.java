package newbank.server.exception.username;

public class UsernameLengthException extends UsernameException {
    public UsernameLengthException() {
        super("Username must be between 5-25 characters long");
    }
}
