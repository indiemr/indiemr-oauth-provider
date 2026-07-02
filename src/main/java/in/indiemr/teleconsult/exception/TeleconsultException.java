package in.indiemr.teleconsult.exception;

public class TeleconsultException extends RuntimeException {
    private final int status;

    public TeleconsultException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}