package com.pasciitools.pasciifinance.common.exception;

import java.io.Serializable;

public class NumberOutOfRangeException extends Exception implements Serializable {
    public NumberOutOfRangeException (String message) {
        super(message);
    }

    public NumberOutOfRangeException(String message, Throwable t) {
        super(message, t);
    }
}
