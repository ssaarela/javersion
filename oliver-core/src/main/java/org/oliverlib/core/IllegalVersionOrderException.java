package org.oliverlib.core;

public class IllegalVersionOrderException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public IllegalVersionOrderException(long tip, long revision) {
        super(String.format("Versions should be ordered by revision. Got %s after %s.", revision, tip));
    }

}
