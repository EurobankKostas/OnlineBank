package newbank.server.exception.exit;

public class LogoutException extends Exception {
    @Override
    public String getMessage() {
        return "User logged out to main menu";
    }
}
