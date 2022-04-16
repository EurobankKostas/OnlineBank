package newbank.server.exception.accountname;

public class AccountNameLengthException extends newbank.server.exception.accountname.AccountNameException {
    public AccountNameLengthException() {
        super("Account name must be between 3-15 characters long");
    }
}
