package ch.eth.infsec.services.pki;

public class PKIServiceException extends RuntimeException {

    public PKIServiceException(String message) {
        super(message);
    }

    public PKIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
