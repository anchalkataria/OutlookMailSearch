package com.wk.mailsearch.exception;


/**
 * The Class GenericException.
 *
 * @author anchal.kataria
 */
public abstract class GenericException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The message. */
	private String message;
	
	/** The cause. */
	private Throwable cause;

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Throwable#getCause()
	 */
	public Throwable getCause() {
		return cause;
	}

	/**
	 * Sets the cause.
	 *
	 * @param cause the new cause
	 */
	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	/**
	 * Constructor to send Error message .
	 *
	 * @param message -An instance of String containing error messages
	 */
	public GenericException(String message) {
		this.message = message;
	}

	/**
	 * Constructor to send Error message And cause.
	 *
	 * @param message -An instance of String containing error messages
	 * @param cause - An object of type Throwable
	 */
	public GenericException(String message, Throwable cause) {
		super();
		this.message = message;
		this.cause = cause;
	}

}
