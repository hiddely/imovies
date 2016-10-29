package ch.eth.infsec.services;

public class InvalidCertificateException extends RuntimeException {

    public InvalidCertificateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCertificateException(String message) {
        super(message);
    }
}
