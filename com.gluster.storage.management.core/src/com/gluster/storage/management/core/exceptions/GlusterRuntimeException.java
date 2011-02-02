package com.gluster.storage.management.core.exceptions;

public class GlusterRuntimeException extends RuntimeException {

	public GlusterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public GlusterRuntimeException(String message) {
		super(message);
	}	
}
