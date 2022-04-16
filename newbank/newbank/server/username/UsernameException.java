package newbank.server.exception.username;

public class UsernameException extends Exception {
    protected String customMessage;

    public UsernameException(String customMessage) {
        super();
        this.customMessage = customMessage;
    }

    @Override
    public String getMessage() {
        return customMessage;
    }
}
