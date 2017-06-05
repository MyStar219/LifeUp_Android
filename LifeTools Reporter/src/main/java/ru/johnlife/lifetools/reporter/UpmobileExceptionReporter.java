package ru.johnlife.lifetools.reporter;

import android.content.Context;
import android.content.pm.PackageManager;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpmobileExceptionReporter extends ExceptionReporter {
    private final static String url = "http://up-mobile.org/error.php";
    private static UpmobileExceptionReporter instance = null;
    private String project;
    private String version;
    private Context context;

    public static UpmobileExceptionReporter getInstance(Context context) {
        if (null == instance) {
            instance = new UpmobileExceptionReporter(context.getApplicationContext());
        }
        return instance;
    }

    private UpmobileExceptionReporter(Context context) {
        this.context = context;
        this.project = context.getPackageName();
        try {
            this.version = context.getPackageManager().getPackageInfo(project, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

    }

    /**
     * Please, log only unexpected critical severe exceptions
     * @param e
     */
    public void logException(Exception e) {
        report(e);
    }

    public static void logIfAvailable(Exception e) {
        if (null == instance) return;
        instance.report(e);
    }

    @Override
    protected void report(Throwable ex) {
        RequestQueue q = Volley.newRequestQueue(context);
        q.add(new JsonObjectRequest(url, new JSONObject(getServiceParams(ex)), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {}
        }));
    }

    private Map<String, String> getServiceParams(Throwable ex) {
        Map<String, String> value = new HashMap<>();
        value.put("project", project);
        value.put("version", version);
        value.put("message", (ex.getMessage() == null || ex.getMessage().trim().length()<1) ? ex.getClass().getSimpleName() : ex.getMessage());
        value.put("trace", getStackTrace(ex));
        return value;
    }
}