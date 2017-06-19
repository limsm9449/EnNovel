package com.sleepingbear.ennovel;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MainCursorAdapter adapter;

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        System.out.println("=============================================== App Start ======================================================================");
        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        //DB가 새로 생성이 되었으면 이전 데이타를 DB에 넣고 Flag를 N 처리함
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if ( "Y".equals(prefs.getString("db_new", "N")) ) {
            DicUtils.dicLog("backup data import");

            DicUtils.readInfoFromFile(this, db, "");

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("db_new", "N");
            editor.commit();
        }

        checkPermission();

        findViewById(R.id.my_b_novel).setOnClickListener(this);
        findViewById(R.id.my_b_news_word).setOnClickListener(this);

        findViewById(R.id.my_b_voc).setOnClickListener(this);
        findViewById(R.id.my_b_voc_study).setOnClickListener(this);

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);

        changeListView();
    }

    public void changeListView() {
        if ( db != null ) {
            Cursor listCursor = db.rawQuery(DicQuery.getMyFavorite(), null);
            if ( listCursor.getCount() == 0 ) {
                listCursor = db.rawQuery(DicQuery.getMyFavoriteMessage(), null);
            }
            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new MainCursorAdapter(this, listCursor, db, 0);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(itemClickListener);
            listView.setOnItemLongClickListener(itemLongClickListener);
            listView.setSelection(0);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            if ( cur.getInt(cur.getColumnIndexOrThrow("SEQ")) != -1 ) {
                Bundle bundle = new Bundle();

                String content = DicUtils.getMyNovelContent(cur.getString(cur.getColumnIndexOrThrow("PATH")));
                bundle.putString("novelTitle", cur.getString(cur.getColumnIndexOrThrow("TITLE")));
                bundle.putString("content", DicUtils.getHtmlString(content));

                Intent intent = new Intent(MainActivity.this, NovelViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cur = (Cursor) adapter.getItem(i);
            final String seq = cur.getString(cur.getColumnIndexOrThrow("TITLE"));
            new AlertDialog.Builder(MainActivity.this)
            .setTitle("알림")
                    .setMessage("즐겨찾기에서 삭제하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DicDb.updMyUnFavorite(db, seq);
                            changeListView();
                            DicUtils.setDbChange(getApplicationContext());  //변경 체크
                            Toast.makeText(getApplicationContext(), "즐겨찾기에서 삭제했습니다.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();

            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, "최고의 영어소설 어플");
            msg.putExtra(Intent.EXTRA_TEXT, "영어.. 참 어렵죠? '최고의 영어소설' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.ennovel ");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));

            return true;
        } else if (id == R.id.action_patch) {
            startActivity(new Intent(getApplication(), PatchActivity.class));

            return true;
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            Intent helpIntent = new Intent(getApplication(), HelpActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);

            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(getApplication(), SettingsActivity.class));

            return true;
        } else if (id == R.id.action_no_ad) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.sleepingbear.pehdictandvoc")));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.my_b_novel:
                Intent novelIntent = new Intent(getApplication(), MyNovelActivity.class);
                novelIntent.putExtras(bundle);
                startActivityForResult(novelIntent, CommConstants.a_MyNovel);

                break;
            case R.id.my_b_news_word:
                Intent newClickWordIntent = new Intent(getApplication(), NewsClickWordActivity.class);
                newClickWordIntent.putExtras(bundle);
                startActivity(newClickWordIntent);

                break;

            case R.id.my_b_voc:
                startActivity(new Intent(getApplication(), VocabularyNoteActivity.class));
                break;
            case R.id.my_b_voc_study:
                startActivity(new Intent(getApplication(), StudyActivity.class));

                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_MyNovel :
                if ( resultCode == RESULT_OK && "Y".equals(data.getStringExtra("isChange")) ) {
                    changeListView();
                }

                break;
        }
    }

    public boolean checkPermission() {
        Log.d(CommConstants.tag, "checkPermission");
        boolean isCheck = false;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            Log.d(CommConstants.tag, "권한 없음");
            if ( ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                //Toast.makeText(this, "(중요)파일로 내보내기, 가져오기를 하기 위해서 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
            Log.d(CommConstants.tag, "2222");
        } else {
            Log.d(CommConstants.tag, "권한 있음");
            isCheck = true;
        }

        return isCheck;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(CommConstants.tag, "권한 허가");
                } else {
                    Log.d(CommConstants.tag, "권한 거부");
                    Toast.makeText(this, "파일 권한이 없기 때문에 파일 내보내기, 가져오기를 할 수 없습니다.\n만일 권한 팝업이 안열리면 '다시 묻지 않기'를 선택하셨기 때문입니다.\n어플을 지우고 다시 설치하셔야 합니다.", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    private long backKeyPressedTime = 0;
    @Override
    public void onBackPressed() {
        //종료 시점에 변경 사항을 기록한다.
        if ( "Y".equals(DicUtils.getDbChange(getApplicationContext())) ) {
            DicUtils.writeInfoToFile(this, db, "");
            DicUtils.clearDbChange(this);
        }

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(), "'뒤로'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();

            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
        }
    }
}


class MainCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    int fontSize = 0;

    public MainCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_main_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_title)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_title)).setTextSize(fontSize);
    }
}
