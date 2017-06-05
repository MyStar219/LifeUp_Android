package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by yanyu on 5/12/2016.
 */
public class OpenInBrowser {
    public static void url(Context context, String url) {
        if (!url.startsWith("https://") && !url.startsWith("http://")){
            url = "http://" + url;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }
}
