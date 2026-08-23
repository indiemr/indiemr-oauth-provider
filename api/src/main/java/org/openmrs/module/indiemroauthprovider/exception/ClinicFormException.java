package org.openmrs.module.indiemroauthprovider.exception;

/**
 * Carries the HTTP status the controller should surface. Auth failures are NOT modelled here — an
 * APIAuthenticationException must keep propagating so BaseRestController maps it to 401/403 (R1).
 */
public class ClinicFormException extends RuntimeException {
	
	private final int status;
	
	public ClinicFormException(String message, int status) {
		super(message);
		this.status = status;
	}
	
	public ClinicFormException(String message, int status, Throwable cause) {
		super(message, cause);
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
}
