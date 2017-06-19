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
    private String novelPart = "";
    private int mSelect = 0;

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

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.novel, android.R.layout.simple_spinner_item);
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
        Cursor listCursor = db.rawQuery(DicQuery.getNovelList("C" + s_idx), null);
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
            Cursor cur = (Cursor) adapter.getItem(position);

            novelTitle = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            novelUrl = cur.getString(cur.getColumnIndexOrThrow("URL"));

            novelPart = "";

            taskKind = "NOVEL_PART";
            task = new NovelTask();
            task.execute();
        }
    };

    public void showPart(int novelPartCount) {
        final int[] kindCodes = new int[novelPartCount];
        final String[] kindCodeNames = new String[novelPartCount];

        int idx = 0;
        for (int i = 0; i < novelPartCount; i++) {
            kindCodes[idx] = i;
            kindCodeNames[idx] = "Part " + (i + 1);
            idx++;
        }

        final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(NovelActivity.this);
        dlg.setTitle("메뉴 선택");
        dlg.setSingleChoiceItems(kindCodeNames, mSelect, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                mSelect = arg1;
            }
        });
        dlg.setNegativeButton("취소", null);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                taskKind = "NOVEL_CONTENT";
                novelPart = "" + (mSelect + 1);
                task = new NovelTask();
                task.execute();
            }
        });
        dlg.show();
    }

    public void saveContent(String novelContent) {
        File file = DicUtils.getFIle(CommConstants.folderName + CommConstants.novelFolderName, novelUrl.split("[.]")[0] + novelPart + ".txt" );
        if ( !file.exists() ) {
            FileOutputStream fos = null;

            try {
                file.createNewFile();
                fos = new FileOutputStream(file);
                fos.write((novelContent.getBytes()));
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }

        String path = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + CommConstants.novelFolderName + "/" + novelUrl.split("[.]")[0] + novelPart + ".txt";

        if ( !"".equals(novelPart) ) {
            novelTitle = novelTitle + " Part " + novelPart;
        }
        DicDb.insMyNovel(db, novelTitle, path);

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);

        finish();
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
                DicUtils.getNovelList(db, "http://www.fullbooks.com/idx" + (s_idx + 1) + ".html", "C" + s_idx);
            } else if ( taskKind.equals("NOVEL_PART") ) {
                novelPartCount = DicUtils.getNovelPartCount("http://www.fullbooks.com/" + novelUrl);
                if ( novelPartCount == 0 ) {
                    taskKind = "NOVEL_CONTENT";
                    novelContent = DicUtils.getNovelContent("http://www.fullbooks.com/" + novelUrl);
                }
            } else if ( taskKind.equals("NOVEL_CONTENT") ) {
                String[] fileNameSplit = novelUrl.split("[.]");
                novelContent = DicUtils.getNovelContent("http://www.fullbooks.com/" + fileNameSplit[0] + novelPart + "." + fileNameSplit[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            pd.dismiss();
            task = null;

            if ( taskKind.equals("NOVEL_LIST") ) {
                changeListView();
            } else if ( taskKind.equals("NOVEL_PART") ) {
                showPart(novelPartCount);
            } else if ( taskKind.equals("NOVEL_CONTENT") ) {
                saveContent(novelContent);
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

