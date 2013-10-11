package org.oliverlib.core;

public class VersionNotFoundException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public final long revision;
    
    public VersionNotFoundException(long revision) {
        super(String.format("revision#%s not found", revision));
        this.revision = revision;
    }
    
}
