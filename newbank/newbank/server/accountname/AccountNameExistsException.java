package newbank.server.exception.accountname;

public class AccountNameExistsException extends AccountNameException {
    public AccountNameExistsException() {
        super("Account name already exists");
    }
}
