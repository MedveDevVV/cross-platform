package autoservice.exception;

public class AuthenticationException extends AutoServiceException {

    public AuthenticationException(String message) {
        super(message, ErrorCodes.AUTH_FAILED);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, ErrorCodes.AUTH_FAILED, cause);
    }
}