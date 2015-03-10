package org.fdroid.fdroid.updater;

/**
 * Created by zhaolei on 15/3/10.
 */
public class UpdateException extends Exception {

    private static final long serialVersionUID = -4492452418826132803L;

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(String message, Exception cause) {
        super(message, cause);
    }
}