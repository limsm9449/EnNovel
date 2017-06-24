package com.sleepingbear.ennovel;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class MyNovelActivity extends AppCompatActivity implements View.OnClickListener {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private MyNovelCursorAdapter adapter;
    private boolean isAllCheck = false;

    public int mSelect = 0;

    private AppCompatActivity mMainActivity;

    private RelativeLayout editRl;

    private boolean isEditing;
    private boolean isChange = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_novel);

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

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(MyNovelActivity.this);
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
                        if ( mSelect == 3 ) {
                            FileChooser filechooser = new FileChooser(MyNovelActivity.this);
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
                        }
                        Bundle bundle = new Bundle();
                        if ( mSelect == 0 ) {
                            bundle.putString("SITE", CommConstants.novel_fullbooks);
                            bundle.putInt("SITE_IDX", mSelect);
                        } else if ( mSelect == 1 ) {
                            bundle.putString("SITE", CommConstants.novel_classicreader);
                            bundle.putInt("SITE_IDX", mSelect);
                        } else if ( mSelect == 2 ) {
                            bundle.putString("SITE", CommConstants.novel_loyalbooks);
                            bundle.putInt("SITE_IDX", mSelect);
                        }
                        Intent intent = new Intent(MyNovelActivity.this, NovelActivity.class);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, 1);
                    }
                });
                dlg.show();
            }
        });

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        findViewById(R.id.my_f_cw_all).setOnClickListener(this);
        findViewById(R.id.my_f_cw_delete).setOnClickListener(this);

        editRl = (RelativeLayout) findViewById(R.id.my_my_novel_rl);
        editRl.setVisibility(View.GONE);

        //리스트 내용 변경
        changeListView();

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    changeListView();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_my_novel, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_exit).setVisible(false);

        if ( isEditing ) {
            menu.findItem(R.id.action_exit).setVisible(true);
        } else {
            menu.findItem(R.id.action_edit).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.action_edit) {
            isEditing = true;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        } else if (id == R.id.action_exit) {
            isEditing = false;
            invalidateOptionsMenu();
            changeEdit(isEditing);
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_my_novel);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        if ( db != null ) {
            Cursor listCursor = db.rawQuery(DicQuery.getMyNovel(), null);
            if ( listCursor.getCount() == 0 ) {
                listCursor = db.rawQuery(DicQuery.getMyNovelMessage(), null);
                changeEdit(false);
                invalidateOptionsMenu();
            }
            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new MyNovelCursorAdapter(this, listCursor, db, 0);
            adapter.editChange(isEditing);
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
            if ( !isEditing ) {
                Cursor cur = (Cursor) adapter.getItem(position);

                Bundle bundle = new Bundle();

                String content = DicUtils.getMyNovelContent(cur.getString(cur.getColumnIndexOrThrow("PATH")));
                bundle.putString("novelTitle", cur.getString(cur.getColumnIndexOrThrow("TITLE")));
                int fontsize = Integer.parseInt( DicUtils.getPreferencesValue( getApplicationContext(), CommConstants.preferences_webViewFont ) );
                bundle.putString("content", DicUtils.getHtmlString(content, fontsize));

                Intent intent = new Intent(MyNovelActivity.this, NovelViewActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Cursor cur = (Cursor) adapter.getItem(i);
            if ( cur.getInt(cur.getColumnIndexOrThrow("SEQ") )!= -1 ) {
                final String seq = cur.getString(cur.getColumnIndexOrThrow("SEQ"));

                new AlertDialog.Builder(MyNovelActivity.this)
                        .setTitle("알림")
                        .setMessage("메인화면 리스트에 나오도록 즐겨찾기로 등록하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DicDb.updMyFavorite(db, seq);
                                isChange = true;
                                DicUtils.setDbChange(getApplicationContext());  //변경 체크
                                Toast.makeText(getApplicationContext(), "즐겨찾기로 등록되었습니다.\n메인 화면에서 리스트를 보실 수 있습니다.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
            return true;
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this.getApplication(), MyNovelActivity.class);
        intent.putExtra("isChange", (isChange ? "Y" : "N"));
        setResult(RESULT_OK, intent);

        finish();
    }
}

class MyNovelCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    public boolean[] isCheck;
    public int[] seq;
    public String[] path;
    private boolean isEditing = false;
    int fontSize = 0;

    public MyNovelCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
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


