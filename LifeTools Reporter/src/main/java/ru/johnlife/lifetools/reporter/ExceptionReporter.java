package ru.johnlife.lifetools.reporter;

import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class ExceptionReporter implements Thread.UncaughtExceptionHandler {
    private static final String NN = "\n\n";
    private Thread.UncaughtExceptionHandler deh;

    public ExceptionReporter() {
        this.deh = Thread.getDefaultUncaughtExceptionHandler();
    }

    protected String getTag() {
        return getClass().getSimpleName();
    }

    @Override
    public synchronized void uncaughtException(final Thread thread, final Throwable ex) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            Log.d(getTag(), "Crash detected in UI Thread");
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    report(ex);
                    return null;
                }
            }.execute((Void)null);
        } else {
            Log.d(getTag(), "Crash detected in background Thread");
            report(ex);
        }
        try {
            deh.uncaughtException(thread, ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected abstract void report(Throwable ex);

    protected String getStackTrace(final Throwable ex) {
        StringWriter swr = new StringWriter();
        PrintWriter pwr = new PrintWriter(swr);
        ex.printStackTrace(pwr);
        Throwable cause = ex;
        while ((cause = cause.getCause()) != null) {
            pwr.print(NN+"Caused By:\n");
            cause.printStackTrace(pwr);
        }
        String value = swr.toString();
        pwr.close();
        try {
            swr.close();
        } catch (IOException e) {
            Log.e(getTag(), "Exception with close swr.", e);
        }
        return value;
    }

}
