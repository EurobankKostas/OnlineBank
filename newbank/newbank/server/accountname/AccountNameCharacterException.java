package newbank.server.exception.accountname;

public class AccountNameCharacterException extends AccountNameException {
    public AccountNameCharacterException() {
        super("Account name must contain only alphabetic characters and numeric digits");
    }
}
