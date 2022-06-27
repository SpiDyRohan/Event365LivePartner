package com.ebabu.event365live.host.bankdetails;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;

import com.ebabu.event365live.host.R;
import com.ebabu.event365live.host.databinding.ActivityLinkAccountWebViewBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class LinkAccountWebViewActivity extends AppCompatActivity {

    private ActivityLinkAccountWebViewBinding linkAccountWebViewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linkAccountWebViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_link_account_web_view);
        if (getIntent() != null) {
            String url = getIntent().getStringExtra("url");
            loadUrl(url);
        }

    }

    @Override
    public void onBackPressed() {
//        if (linkAccountWebViewBinding.webView.canGoBack()) {
//            linkAccountWebViewBinding.webView.goBack();
//        }else {
//
//        }
        activityResult();
       // super.onBackPressed();
    }

    public void loadUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            linkAccountWebViewBinding.webView.setWebViewClient(new WebViewClient());
            linkAccountWebViewBinding.webView.getSettings().setJavaScriptEnabled(true);
            linkAccountWebViewBinding.webView.getSettings().setLoadWithOverviewMode(true);
            linkAccountWebViewBinding.webView.getSettings().setUseWideViewPort(true);
            linkAccountWebViewBinding.webView.loadUrl(url);
        }
    }

    public void onBackOnClick(View view) {
        activityResult();
    }

    public class WebViewClient extends android.webkit.WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            linkAccountWebViewBinding.progressBar.setVisibility(View.VISIBLE);
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            linkAccountWebViewBinding.progressBar.setVisibility(View.GONE);
        }
    }

    private void activityResult() {
        setResult(Activity.RESULT_OK);
        finish();
    }

}
