package com.foxtelemetry.core;

public final class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Thread.UncaughtExceptionHandler previous;

    public CrashHandler(Thread.UncaughtExceptionHandler previous) {
        this.previous = previous;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            FoxCore core = FoxCore.getInstance();
            if (core != null) {
                core.report(throwable, "UNCAUGHT_EXCEPTION", true);
            }
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
