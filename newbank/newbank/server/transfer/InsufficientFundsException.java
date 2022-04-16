package newbank.server.exception.transfer;

public class InsufficientFundsException extends Exception {
    @Override
    public String getMessage() {
        return "Insufficient funds in account";
    }
}
