package com.sleepingbear.ennovel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class NovelActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    int fontSize = 0;

    private NovelCursorAdapter adapter;

    private Spinner s_novel;
    private int s_idx = 0;
    private String novelTitle = "";
    private String novelUrl = "";
    private String site = "";
    private int siteIdx = 0;

    NovelTask task;
    private String taskKind = "NOVEL_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novel);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        Bundle b = getIntent().getExtras();
        site = b.getString("SITE");
        siteIdx = b.getInt("SITE_IDX");

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        ArrayAdapter<CharSequence> adapter = null;
        if ( site.equals(CommConstants.novel_fullbooks) ) {
            adapter = ArrayAdapter.createFromResource(this, R.array.novel0, android.R.layout.simple_spinner_item);
        } else if ( site.equals(CommConstants.novel_classicreader) ) {
            adapter = ArrayAdapter.createFromResource(this, R.array.novel1, android.R.layout.simple_spinner_item);
        } else if ( site.equals(CommConstants.novel_loyalbooks) ) {
            adapter = ArrayAdapter.createFromResource(this, R.array.novel2, android.R.layout.simple_spinner_item);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_novel = (Spinner) findViewById(R.id.my_s_novel);
        s_novel.setAdapter(adapter);
        s_novel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                s_idx = parent.getSelectedItemPosition();

                task = new NovelTask();
                task.execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        s_novel.setSelection(0);

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_help, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_novel);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        Cursor listCursor = db.rawQuery(DicQuery.getNovelList("S" + siteIdx + "_" + s_idx), null);
        ListView listView = (ListView) findViewById(R.id.my_lv);
        adapter = new NovelCursorAdapter(this, listCursor, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final int pos = position;
            new AlertDialog.Builder(NovelActivity.this)
                .setTitle("알림")
                .setMessage("영문소설을 다운로드 하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cur = (Cursor) adapter.getItem(pos);

                        novelTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
                        novelUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

                        taskKind = "NOVEL_CONTENT";
                        task = new NovelTask();
                        task.execute();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
        }
    };

    public void saveContent(String novelContent, int Part) {
        File file = null;
        if ( site.equals(CommConstants.novel_fullbooks) ) {
            file = DicUtils.getFIle(CommConstants.folderName + CommConstants.novelFolderName, novelUrl.split("[.]")[0] + Part + ".txt");
        } else if ( site.equals(CommConstants.novel_classicreader)) {
            file = DicUtils.getFIle(CommConstants.folderName + CommConstants.novelFolderName, novelUrl.split("[/]")[2] + "_" + Part + ".txt");
        } else if ( site.equals(CommConstants.novel_loyalbooks)) {
            file = DicUtils.getFIle(CommConstants.folderName + CommConstants.novelFolderName, novelUrl.split("[/]")[2] + "_" + Part + ".txt");
        }

        try {
            FileOutputStream fos = null;
            file.createNewFile();
            fos = new FileOutputStream(file);
            fos.write((novelContent.getBytes()));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        String path = "";
        if ( site.equals(CommConstants.novel_fullbooks) ) {
            path = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.novelFolderName + "/" + novelUrl.split("[.]")[0] + Part + ".txt";
        } else if ( site.equals(CommConstants.novel_classicreader) ) {
            path = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.novelFolderName + "/" + novelUrl.split("[/]")[2] + "_" + Part + ".txt";
        } else if ( site.equals(CommConstants.novel_loyalbooks) ) {
            path = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.novelFolderName + "/" + novelUrl.split("[/]")[2] + "_" + Part + ".txt";
        }
        if ( Part > 0 ) {
            DicDb.insMyNovel(db, novelTitle.replaceAll("['\"]","`") + " Part " + Part, path);
        } else {
            DicDb.insMyNovel(db, novelTitle.replaceAll("['\"]","`"), path);
        }
    }

    @Override
    public void onClick(View v) {
    }

    private class NovelTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pd;
        private int novelPartCount = 0;
        private String novelContent;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(NovelActivity.this);
            pd.setIndeterminate(true);
            pd.setCancelable(false);
            pd.show();
            pd.setContentView(R.layout.custom_progress);

            pd.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            if ( taskKind.equals("NOVEL_LIST") ) {
                if ( site.equals(CommConstants.novel_fullbooks) ) {
                    DicUtils.getNovelList0(db, "http://www.fullbooks.com/idx" + (s_idx + 1) + ".html", "S" + siteIdx + "_" + s_idx);
                } else if ( site.equals(CommConstants.novel_classicreader) ) {
                    String[] novelCategory = getResources().getStringArray(R.array.novel1);
                    String arrValue = novelCategory[s_idx];
                    DicUtils.getNovelList1(db, "http://www.classicreader.com/list/titles/" + arrValue, "S" + siteIdx + "_" + s_idx);
                } else if ( site.equals(CommConstants.novel_loyalbooks) ) {
                    String[] novelCategory = getResources().getStringArray(R.array.novel2);
                    String arrValue = novelCategory[s_idx];
                    DicUtils.getNovelList2(db, "http://www.loyalbooks.com/genre/" + arrValue.replaceAll("[ ]","_"), "S" + siteIdx + "_" + s_idx);
                }
            } else if ( taskKind.equals("NOVEL_CONTENT") ) {
                if ( site.equals(CommConstants.novel_fullbooks) ) {
                    novelPartCount = DicUtils.getNovelPartCount0("http://www.fullbooks.com/" + novelUrl);

                    if ( novelPartCount == 0 ) {
                        novelContent = DicUtils.getNovelContent0("http://www.fullbooks.com/" + novelUrl);
                        saveContent(novelContent, 0);
                    } else {
                        for (int i = 1; i <= novelPartCount; i++) {
                            String[] fileNameSplit = novelUrl.split("[.]");
                            novelContent = DicUtils.getNovelContent0("http://www.fullbooks.com/" + fileNameSplit[0] + i + "." + fileNameSplit[1]);
                            saveContent(novelContent, i);
                        }
                    }
                } else if ( site.equals(CommConstants.novel_classicreader) ) {
                    novelPartCount = DicUtils.getNovelPartCount1("http://www.classicreader.com" + novelUrl);

                    if ( novelPartCount == 0 ) {
                        novelContent = DicUtils.getNovelContent1("http://www.classicreader.com" + novelUrl + 1);
                        saveContent(novelContent, 0);
                    } else {
                        for (int i = 1; i <= novelPartCount; i++) {
                            novelContent = DicUtils.getNovelContent1("http://www.classicreader.com" + novelUrl + i) + "\n";
                            saveContent(novelContent, i);
                        }
                    }
                } else if ( site.equals(CommConstants.novel_loyalbooks) ) {
                    novelContent = DicUtils.getNovelContent2("http://www.loyalbooks.com" + novelUrl);
                    saveContent(novelContent, 0);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            if ( taskKind.equals("NOVEL_LIST") ) {
                changeListView();
            } else if ( taskKind.equals("NOVEL_CONTENT") ) {
                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);

                finish();
            }

            super.onPostExecute(result);
        }
    }

}

class NovelCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public NovelCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_novel_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_novelTitle)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_novelTitle)).setTextSize(fontSize);
    }

}

