package ch.puzzle.itc.mobiliar.common.exception;

public class ConcurrentModificationException extends  AMWException{

    private static final long serialVersionUID = 1L;

    public ConcurrentModificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConcurrentModificationException(String message) {
        super(message);
    }
}
