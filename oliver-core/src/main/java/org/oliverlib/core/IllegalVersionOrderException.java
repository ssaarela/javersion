package org.oliverlib.core;

public class IllegalVersionOrderException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public IllegalVersionOrderException(long earlierRevision, long laterRevision) {
        super(String.format("Versions should be ordered by revision. Got %s after %s.", laterRevision, earlierRevision));
    }

}
