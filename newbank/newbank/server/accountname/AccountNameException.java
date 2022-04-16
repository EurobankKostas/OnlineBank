package newbank.server.exception.accountname;

public class AccountNameException extends Exception {
    protected String customMessage;

    public AccountNameException(String customMessage) {
        super();
        this.customMessage = customMessage;
    }

    @Override
    public String getMessage() {
        return customMessage;
    }
}
