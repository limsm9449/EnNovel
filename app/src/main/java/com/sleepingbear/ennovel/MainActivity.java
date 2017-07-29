package com.sleepingbear.ennovel;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MainCursorAdapter adapter;
    private boolean isAllCheck = false;

    public int mSelect = 0;


    private RelativeLayout editRl;

    private boolean isEditing;
    private boolean isChange = false;
    private boolean isNoData = true;

    private static final int MY_PERMISSIONS_REQUEST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int[] kindCodes = new int[4];
                final String[] kindCodeNames = new String[4];

                int idx = 0;
                kindCodes[idx] = 0;
                kindCodeNames[idx++] = CommConstants.novel_fullbooks;
                kindCodes[idx] = 1;
                kindCodeNames[idx++] = CommConstants.novel_classicreader;
                kindCodes[idx] = 2;
                kindCodeNames[idx++] = CommConstants.novel_loyalbooks;
                kindCodes[idx] = 3;
                kindCodeNames[idx++] = CommConstants.novel_local;

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("사이트 선택");
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
                        if ( mSelect == 3 ) {
                            FileChooser filechooser = new FileChooser(MainActivity.this);
                            filechooser.setFileListener(new FileChooser.FileSelectedListener() {
                                @Override
                                public void fileSelected(final File file) {
                                    DicDb.insMyNovel(db, file.getName(), file.getAbsolutePath());
                                    changeListView();

                                    Toast.makeText(getApplicationContext(), "소설을 추가했습니다.", Toast.LENGTH_LONG).show();
                                }
                            });
                            filechooser.setExtension("txt");
                            filechooser.showDialog();
                        } else {
                            Bundle bundle = new Bundle();
                            if (mSelect == 0) {
                                bundle.putString("SITE", CommConstants.novel_fullbooks);
                                bundle.putInt("SITE_IDX", mSelect);
                            } else if (mSelect == 1) {
                                bundle.putString("SITE", CommConstants.novel_classicreader);
                                bundle.putInt("SITE_IDX", mSelect);
                            } else if (mSelect == 2) {
                                bundle.putString("SITE", CommConstants.novel_loyalbooks);
                                bundle.putInt("SITE_IDX", mSelect);
                            }
                            Intent intent = new Intent(MainActivity.this, NovelActivity.class);
                            intent.putExtras(bundle);
                            startActivityForResult(intent, CommConstants.a_MyNovel);
                        }
                    }
                });
                dlg.show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        findViewById(R.id.my_f_cw_all).setOnClickListener(this);
        findViewById(R.id.my_f_cw_delete).setOnClickListener(this);

        editRl = (RelativeLayout) findViewById(R.id.my_my_novel_rl);
        editRl.setVisibility(View.GONE);

        //리스트 내용 변경
        changeListView();

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            //} else {
            //    super.onBackPressed();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_novel_site) {
            Bundle bundle = new Bundle();
            Intent novelSiteIntent = new Intent(getApplication(), NovelSiteActivity.class);
            novelSiteIntent.putExtras(bundle);
            startActivity(novelSiteIntent);
        } else if (id == R.id.nav_novel_word) {
            Bundle bundle = new Bundle();
            Intent newClickWordIntent = new Intent(getApplication(), NewsClickWordActivity.class);
            newClickWordIntent.putExtras(bundle);
            startActivity(newClickWordIntent);
        } else if (id == R.id.nav_voc) {
            startActivity(new Intent(getApplication(), VocabularyNoteActivity.class));
        } else if (id == R.id.nav_study) {
            startActivity(new Intent(getApplication(), StudyActivity.class));
        } else if (id == R.id.nav_patch) {
            startActivity(new Intent(getApplication(), PatchActivity.class));
        } else if (id == R.id.nav_help) {
            Bundle bundle = new Bundle();
            Intent helpIntent = new Intent(getApplication(), HelpActivity.class);
            helpIntent.putExtras(bundle);
            startActivity(helpIntent);
        } else if (id == R.id.nav_setting) {
            startActivityForResult(new Intent(getApplication(), SettingsActivity.class), CommConstants.a_setting);
        } else if (id == R.id.nav_share) {
            Intent msg = new Intent(Intent.ACTION_SEND);
            msg.addCategory(Intent.CATEGORY_DEFAULT);
            msg.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            msg.putExtra(Intent.EXTRA_TEXT, "영어.. 참 어렵죠? '" + R.string.app_name + "' 어플을 사용해 보세요. https://play.google.com/store/apps/details?id=com.sleepingbear.ennovel ");
            msg.setType("text/plain");
            startActivity(Intent.createChooser(msg, "어플 공유"));
        } else if (id == R.id.nav_review) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } else if (id == R.id.nav_other_app) {
            String url ="http://blog.naver.com/limsm9449/221031416154";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (id == R.id.nav_mail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name);
            intent.putExtra(Intent.EXTRA_TEXT, "어플관련 문제점을 적어 주세요.\n빠른 시간 안에 수정을 하겠습니다.\n감사합니다.");
            intent.setData(Uri.parse("mailto:limsm9449@gmail.com"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_exit).setVisible(false);

        if ( !isNoData ) {
            if (isEditing) {
                menu.findItem(R.id.action_exit).setVisible(true);
            } else {
                menu.findItem(R.id.action_edit).setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            isEditing = true;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        } else if (id == R.id.action_exit) {
            isEditing = false;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        DicUtils.dicLog("onActivityResult : " + requestCode + " : " + resultCode);

        switch ( requestCode ) {
            case CommConstants.a_setting :
                changeListView();

                break;
            case CommConstants.a_MyNovel : {
                if (resultCode == Activity.RESULT_OK) {
                    changeListView();
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void changeListView() {
        if ( db != null ) {
            Cursor listCursor = db.rawQuery(DicQuery.getMyNovel(), null);
            if ( listCursor.getCount() == 0 ) {
                isNoData = true;
                listCursor = db.rawQuery(DicQuery.getMyNovelMessage(), null);
                changeEdit(false);
                invalidateOptionsMenu();
            } else {
                isNoData = false;
                invalidateOptionsMenu();
            }

            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new MainCursorAdapter(this, listCursor, db, 0);
            adapter.editChange(isEditing);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(itemClickListener);
            listView.setSelection(0);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ( !isEditing ) {
                Cursor cur = (Cursor) adapter.getItem(position);

                Bundle bundle = new Bundle();

                bundle.putString("novelTitle", cur.getString(cur.getColumnIndexOrThrow("TITLE")));
                bundle.putString("path", cur.getString(cur.getColumnIndexOrThrow("PATH")));

                Intent intent = new Intent(MainActivity.this, NovelViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };

    @Override
    public void onClick(View v) {
        DicUtils.dicLog("onClick");
        switch (v.getId()) {
            case R.id.my_f_cw_all :
                isAllCheck = !isAllCheck;
                adapter.allCheck(isAllCheck);
                break;
            case R.id.my_f_cw_delete :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    new android.app.AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("삭제하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    adapter.delete();
                                    changeListView();

                                    DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }

                break;
        }
    }

    public void changeEdit( boolean isEditing ) {
        //처음에 오류가 발생하는 경우가 있음
        if ( editRl == null ) {
            return;
        }

        this.isEditing = isEditing;

        if ( isEditing ) {
            editRl.setVisibility(View.VISIBLE);
        } else {
            editRl.setVisibility(View.GONE);
        }

        if ( adapter != null ) {
            adapter.editChange(isEditing);
        }
    }

}


class MainCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    public boolean[] isCheck;
    public int[] seq;
    public String[] path;
    private boolean isEditing = false;
    int fontSize = 0;

    public MainCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );

        isCheck = new boolean[cursor.getCount()];
        seq = new int[cursor.getCount()];
        path = new String[cursor.getCount()];
        while ( cursor.moveToNext() ) {
            isCheck[cursor.getPosition()] = false;
            seq[cursor.getPosition()] = cursor.getInt(cursor.getColumnIndexOrThrow("SEQ"));
            path[cursor.getPosition()] = cursor.getString(cursor.getColumnIndexOrThrow("PATH"));
        }
        cursor.moveToFirst();
    }

    static class ViewHolder {
        protected int position;
        protected CheckBox cb;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_my_novel_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.cb = (CheckBox) view.findViewById(R.id.my_cb_check);
        viewHolder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                ViewHolder viewHolder = (ViewHolder)buttonView.getTag();
                isCheck[viewHolder.position] = isChecked;
                notifyDataSetChanged();

                DicUtils.dicLog("onCheckedChanged : " + viewHolder.position);
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.position = cursor.getPosition();
        viewHolder.cb.setTag(viewHolder);

        ((TextView) view.findViewById(R.id.my_tv_title)).setText(cursor.getString(cursor.getColumnIndexOrThrow("TITLE")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));
        ((TextView) view.findViewById(R.id.my_tv_path)).setText(cursor.getString(cursor.getColumnIndexOrThrow("PATH")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_title)).setTextSize(fontSize);

        ((CheckBox)view.findViewById(R.id.my_cb_check)).setChecked(isCheck[cursor.getPosition()]);
        if ( isCheck[cursor.getPosition()] ) {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_on_background);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }

        if ( isEditing ) {
            view.findViewById(R.id.my_f_ci_rl).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.my_f_ci_rl).setVisibility(View.GONE);
        }
    }

    public void allCheck(boolean chk) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            isCheck[i] = chk;
        }

        notifyDataSetChanged();
    }

    public void delete() {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.delMyNovel(mDb, seq[i]);

                //파일 삭제
                File f = new File(path[i]);
                f.delete();
            }
        }
    }

    public boolean isCheck() {
        boolean rtn = false;
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                rtn = true;
                break;
            }
        }

        return rtn;
    }

    public void editChange(boolean isEditing) {
        this.isEditing = isEditing;
        notifyDataSetChanged();
    }
}

