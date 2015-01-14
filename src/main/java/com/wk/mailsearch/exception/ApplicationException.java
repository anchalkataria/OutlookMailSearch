/**
 * 
 */
package com.wk.mailsearch.exception;

/**
 * The Class ApplicationException.
 * @author anchal.kataria
 *
 */
public class ApplicationException extends GenericException{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor to send Error Message .
	 *
	 * @param message - An instance of String containing error messages
	 */
	public ApplicationException(String message) {
		super(message);
	}
	
	/**
	 * Constructor to send Error message And cause.
	 *
	 * @param message -An instance of String containing error messages
	 * @param cause - An object of type Throwable
	 */
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
	}

}
