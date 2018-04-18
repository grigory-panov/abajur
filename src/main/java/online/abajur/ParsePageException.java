package online.abajur;

public class ParsePageException extends AppException {
    public ParsePageException() {
    }

    public ParsePageException(String message) {
        super(message);
    }

    public ParsePageException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsePageException(Throwable cause) {
        super(cause);
    }
}
