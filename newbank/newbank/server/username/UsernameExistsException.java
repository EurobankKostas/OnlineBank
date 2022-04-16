package newbank.server.exception.username;

public class UsernameExistsException extends UsernameException {
    public UsernameExistsException() {
        super("Username already exists");
    }
}
