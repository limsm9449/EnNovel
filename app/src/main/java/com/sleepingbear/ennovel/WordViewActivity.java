package com.sleepingbear.ennovel;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class WordViewActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private WordViewActivityCursorAdapter adapter;
    private String entryId;
    private String word;
    private String kind;
    private String site = "Naver";
    private WebView webView;
    private LinearLayout detailLl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle b = getIntent().getExtras();
        entryId = b.getString("entryId");
        getWordInfo();

        //상세정보, 예제 영역
        detailLl = (LinearLayout) this.findViewById(R.id.my_wv_ll_top);
        detailLl.setVisibility(View.GONE);

        //웹뷰 영역
        webView = (WebView) this.findViewById(R.id.my_wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.setWebViewClient(new WordViewActivity.MyWebViewClient());

        webDictionaryLoad();

        AdView av =(AdView)findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    public void getWordInfo() {
        Cursor wordCursor = db.rawQuery(DicQuery.getVocDetailForEntryId(entryId), null);
        if ( wordCursor.moveToNext() ) {
            word = wordCursor.getString(wordCursor.getColumnIndexOrThrow("WORD"));
            kind = wordCursor.getString(wordCursor.getColumnIndexOrThrow("KIND"));

            ActionBar ab = getSupportActionBar();
            ab.setTitle(word + " 검색");
        }
        wordCursor.close();
    }

    public void showListView() {
        if ( adapter == null ) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
            sql.append("  FROM DIC_SAMPLE " + CommConstants.sqlCR);
            sql.append(" WHERE (SENTENCE1 LIKE (SELECT '%'||WORD||'%' FROM DIC WHERE ENTRY_ID = '" + entryId + "')  " + CommConstants.sqlCR);
            sql.append("             OR SENTENCE2 LIKE (SELECT '%'||WORD||'%' FROM DIC WHERE ENTRY_ID = '" + entryId + "'))  " + CommConstants.sqlCR);
            sql.append("ORDER BY 2  " + CommConstants.sqlCR);
            sql.append(" LIMIT 200  " + CommConstants.sqlCR);

            DicUtils.dicLog(sql.toString());
            Cursor dicViewCursor = db.rawQuery(sql.toString(), null);

            ListView dicViewListView = (ListView) this.findViewById(R.id.my_lv);
            adapter = new WordViewActivityCursorAdapter(this, dicViewCursor, 0);
            dicViewListView.setAdapter(adapter);
            dicViewListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            dicViewListView.setOnItemClickListener(itemClickListener);
        }
    }

    /**
     * 문장이 선택되면 단어 상세창을 열어준다.
     */
    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);
            cur.moveToPosition(position);

            Bundle bundle = new Bundle();
            bundle.putString("foreign", cur.getString(cur.getColumnIndexOrThrow("SENTENCE1")));
            bundle.putString("han", cur.getString(cur.getColumnIndexOrThrow("SENTENCE2")));
            bundle.putString("sampleSeq", cur.getString(cur.getColumnIndexOrThrow("SEQ")));

            Intent intent = new Intent(getApplication(), SentenceViewActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_word_view, menu);

        MenuItem item = menu.findItem(R.id.action_web_dic);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.wordView, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if ( parent.getSelectedItemPosition() == 0 ) {
                    site = "Naver";
                    webDictionaryLoad();

                    webView.setVisibility(View.VISIBLE);
                    detailLl.setVisibility(View.GONE);
                } else if ( parent.getSelectedItemPosition() == 1 ) {
                    site = "Daum";
                    webDictionaryLoad();

                    webView.setVisibility(View.VISIBLE);
                    detailLl.setVisibility(View.GONE);
                } else if ( parent.getSelectedItemPosition() == 2 ) {
                    site = "Sample";
                    showListView();

                    webView.setVisibility(View.GONE);
                    detailLl.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        spinner.setSelection( Integer.parseInt( DicUtils.getPreferencesValue( getApplicationContext(), CommConstants.preferences_wordView ) ) );

        return true;
    }

    public void webDictionaryLoad() {
        String url = "";
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
        DicUtils.dicLog("url : " + url);
        webView.clearHistory();
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_wordView);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

class WordViewActivityCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public WordViewActivityCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_word_view_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv_foriegn = (TextView) view.findViewById(R.id.my_tv_foriegn);
        TextView tv_han = (TextView) view.findViewById(R.id.my_tv_han);

        String sentence1 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE1"))).trim();
        String sentence2 = DicUtils.getString(cursor.getString(cursor.getColumnIndexOrThrow("SENTENCE2"))).trim();

        tv_foriegn.setText((cursor.getPosition() + 1) + ". " + sentence1);
        tv_han.setText("   " + sentence2);

        //사이즈 설정
        tv_foriegn.setTextSize(fontSize);
        tv_han.setTextSize(fontSize);
    }
}
