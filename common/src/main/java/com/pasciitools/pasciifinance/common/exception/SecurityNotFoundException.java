package com.pasciitools.pasciifinance.common.exception;

import java.io.Serializable;

public class SecurityNotFoundException extends Exception implements Serializable {
    public SecurityNotFoundException (String message) {
        super(message);
    }

    public SecurityNotFoundException(String message, Throwable t) {
        super(message, t);
    }
}
