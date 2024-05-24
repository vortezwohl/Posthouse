package org.posthouse.util.intf;

public interface DataPersistenceProcessor {
    public abstract void append(String msg);

    public abstract void discard();
}
