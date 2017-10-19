package com.sleepingbear.ennovel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class WebDictionaryActivity extends AppCompatActivity {
    private WebView webView;
    private String kind;
    private String word;
    private String site;
    private ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_dictionary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        kind = getIntent().getExtras().getString("kind");
        word = getIntent().getExtras().getString("word");
        site = getIntent().getExtras().getString("site");

        if ( kind == null ) {
            kind = CommConstants.dictionaryKind_f;
        }
        if ( word == null ) {
            word = "";
        }
        if ( site == null ) {
            site = "Naver";
        }

        ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        webView = (WebView) this.findViewById(R.id.my_c_webdictionary_wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WebDictionaryActivity.MyWebViewClient());

        webDictionaryLoad();

        DicUtils.setAdView(this);
    }

    public void webDictionaryLoad() {
        if ( word.equals("") ) {
            ab.setTitle("Web 사전");
        } else {
            ab.setTitle(word + " 검색");
        }

        String url = "";
        if ( "".equals(word) ) {
            if ("Naver".equals(site)) {
                url = "http://endic.naver.com/";
            } else if ("Daum".equals(site)) {
                url = "http://alldic.daum.net/search.do?dic=eng";
            }
        } else {
            if ( kind.equals(CommConstants.dictionaryKind_f) ) {
                if ("Naver".equals(site)) {
                    url = "http://endic.naver.com/search.nhn?sLn=kr&searchOption=entry_idiom&query=" + word;
                } else if ("Daum".equals(site)) {
                    url = "http://alldic.daum.net/search.do?dic=eng&q=" + word;
                }
            } else {
                if ("Naver".equals(site)) {
                    url = "http://krdic.naver.com/search.nhn?kind=all&query=" + word;
                } else if ("Daum".equals(site)) {
                    url = "http://alldic.daum.net/search.do?dic=kor&q=" + word;
                }
            }
        }
        DicUtils.dicLog("url : " + url);
        webView.clearHistory();
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web_dic, menu);

        MenuItem item = menu.findItem(R.id.action_web_dic);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.webDic, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if ( parent.getSelectedItemPosition() == 0 ) {
                    site = "Naver";
                    webDictionaryLoad();
                } else if ( parent.getSelectedItemPosition() == 1 ) {
                    site = "Daum";
                    webDictionaryLoad();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        if ( "Naver".equals(site) ) {
            spinner.setSelection(0);
        } else {
            spinner.setSelection(1);
        }

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_webDictionary);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if ( webView.canGoBack() ) {
                        webView.goBack();
                    } else {
                        //Toast.makeText(getApplicationContext(), "상단의 Back 버튼을 클릭해주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

}

