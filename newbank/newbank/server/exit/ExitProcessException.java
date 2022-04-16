package newbank.server.exception.exit;

public class ExitProcessException extends Exception {
    @Override
    public String getMessage() {
        return "User exited the process";
    }
}
