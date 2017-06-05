package com.smartgateway.app.fragment.drawer;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.smartgateway.app.R;

import ru.johnlife.lifetools.fragment.BaseAbstractFragment;

/**
 * Created by Terry on 8/7/16.
 */
public class WebFragment extends BaseAbstractFragment {
	private WebView webView;

	@Override
	protected String getTitle(Resources res) {
		return "";
	}

	@Override
	protected AppBarLayout getToolbar(LayoutInflater inflater, ViewGroup container) {
		return createToolbarFrom(R.layout.toolbar_small);
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_web, container, false);
		webView = (WebView) view.findViewById(R.id.aboutWebview);

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		CookieSyncManager.createInstance(getContext());
		webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		webView.clearHistory();
		webView.clearCache(true);
		webView.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}

			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

			}
		});
		webView.loadUrl("http://smartgateway.com.sg/");
		return view;
	}
}
