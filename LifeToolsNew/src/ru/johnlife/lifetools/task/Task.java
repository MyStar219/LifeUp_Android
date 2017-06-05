package ru.johnlife.lifetools.task;

import android.os.AsyncTask;

/**
 * Created by yanyu on 5/11/2016.
 */
public abstract class Task extends AsyncTask<Void, Void, Void> {
    protected abstract void doInBackground();

    @Override
    protected Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    public void execute() {
        execute((Void)null);
    }
}
