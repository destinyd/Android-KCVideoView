package com.github.destinyd.kcvideoview.error;

/**
 * Created by dd on 14-4-22.
 */
public class UnsupportedLayoutError extends Error {

    public UnsupportedLayoutError() {
    }

    public UnsupportedLayoutError(String detailMessage) {
        super(detailMessage);
    }

    public UnsupportedLayoutError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnsupportedLayoutError(Throwable throwable) {
        super(throwable);
    }
}
