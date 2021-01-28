package net.rahka.chess.configuration;

public class NonConfigurableClassException extends RuntimeException {

    public NonConfigurableClassException() {
        super();
    }

    public NonConfigurableClassException(String message) {
        super(message);
    }

    public NonConfigurableClassException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonConfigurableClassException(Throwable cause) {
        super(cause);
    }

    protected NonConfigurableClassException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
