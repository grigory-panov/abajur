package online.abajur;

public class DownloadPageException extends AppException {
    public DownloadPageException() {
    }

    public DownloadPageException(String message) {
        super(message);
    }

    public DownloadPageException(String message, Throwable cause) {
        super(message, cause);
    }

    public DownloadPageException(Throwable cause) {
        super(cause);
    }
}
