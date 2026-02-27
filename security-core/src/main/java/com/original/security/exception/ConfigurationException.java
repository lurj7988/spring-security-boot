package com.original.security.exception;

/**
 * Exception thrown when the framework configuration is invalid or missing required properties.
 * This exception is thrown during application startup when critical security configurations
 * fail validation.
 *
 * @author Naulu
 * @since 1.0.0
 */
public class ConfigurationException extends RuntimeException {

    /**
     * Constructs a new ConfigurationException with the specified detail message.
     *
     * @param message the detail message describing the configuration error
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new ConfigurationException with the specified detail message and cause.
     * This constructor allows wrapping underlying exceptions while providing a custom message.
     *
     * @param message the detail message describing the configuration error
     * @param cause the underlying cause of this exception
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ConfigurationException with the specified cause.
     *
     * @param cause the underlying cause of this exception
     */
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
