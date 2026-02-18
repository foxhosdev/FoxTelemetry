package com.foxtelemetry.core;

import com.foxtelemetry.FoxTelemetry;

public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler previous;

    public CrashHandler(Thread.UncaughtExceptionHandler previous) {
        this.previous = previous;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            FoxTelemetry.report(throwable, "UNCAUGHT_EXCEPTION");
        } catch (Throwable ignored) {
            // never throw from crash handler
        }

        if (previous != null) {
            previous.uncaughtException(thread, throwable);
        } else {
            System.exit(1);
        }
    }
}
