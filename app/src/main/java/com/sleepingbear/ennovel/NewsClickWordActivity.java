package com.sleepingbear.ennovel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class NewsClickWordActivity extends AppCompatActivity implements View.OnClickListener {

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private NewsClickwordCursorAdapter adapter;
    private boolean isAllCheck = false;

    public int mSelect = 0;

    private AppCompatActivity mMainActivity;

    private RelativeLayout editRl;

    private boolean isEditing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_click_word);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        ((ImageView) findViewById(R.id.my_f_cw_all)).setOnClickListener(this);
        ((ImageView) findViewById(R.id.my_f_cw_delete)).setOnClickListener(this);
        ((ImageView) findViewById(R.id.my_f_cw_save)).setOnClickListener(this);
        ((ImageView) findViewById(R.id.my_f_cw_new_save)).setOnClickListener(this);

        editRl = (RelativeLayout) findViewById(R.id.my_f_clickword_rl);
        editRl.setVisibility(View.GONE);

        //리스트 내용 변경
        changeListView();

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 상단 메뉴 구성
        getMenuInflater().inflate(R.menu.menu_news_click, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ((MenuItem)menu.findItem(R.id.action_edit)).setVisible(false);
        ((MenuItem)menu.findItem(R.id.action_exit)).setVisible(false);

        if ( isEditing ) {
            ((MenuItem)menu.findItem(R.id.action_exit)).setVisible(true);
        } else {
            ((MenuItem)menu.findItem(R.id.action_edit)).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
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
            bundle.putString("SCREEN", CommConstants.screen_newsClickWord);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeListView() {
        if ( db != null ) {
            Cursor listCursor = db.rawQuery(DicQuery.getNewsClickword(), null);
            ListView listView = (ListView) findViewById(R.id.my_lv);
            adapter = new NewsClickwordCursorAdapter(this, listCursor, db, 0);
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

                final String entryId = cur.getString(cur.getColumnIndexOrThrow("ENTRY_ID"));
                final String word = cur.getString(cur.getColumnIndexOrThrow("WORD"));
                final String seq = cur.getString(cur.getColumnIndexOrThrow("_id"));

                Intent intent = new Intent(getApplication(), WordViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("entryId", entryId);
                bundle.putString("seq", seq);
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
                if ( isAllCheck ) {
                    isAllCheck = false;
                } else {
                    isAllCheck = true;
                }
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
            case R.id.my_f_cw_new_save :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(this, "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {

                    LayoutInflater inflater = (LayoutInflater) getSystemService(this.LAYOUT_INFLATER_SERVICE);
                    final View dialog_layout = inflater.inflate(R.layout.dialog_category_add, (ViewGroup) findViewById(R.id.my_d_category_root));

                    //dialog 생성..
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                    builder.setView(dialog_layout);
                    final android.app.AlertDialog alertDialog = builder.create();

                    ((TextView) dialog_layout.findViewById(R.id.my_d_category_add_tv_title)).setText("단어장 추가");
                    final EditText et_ins = ((EditText) dialog_layout.findViewById(R.id.my_d_category_add_et_ins));
                    ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_ins)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ("".equals(et_ins.getText().toString())) {
                                Toast.makeText(getApplication(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                            } else {
                                alertDialog.dismiss();

                                String insCategoryCode = DicQuery.getInsCategoryCode(db);
                                db.execSQL(DicQuery.getInsNewCategory(CommConstants.vocabularyCode, insCategoryCode, et_ins.getText().toString()));

                                adapter.save(insCategoryCode);
                                changeListView();

                                DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                                Toast.makeText(getApplicationContext(), "단어장에 추가하였습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    ((Button) dialog_layout.findViewById(R.id.my_d_category_add_b_close)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }

                break;
            case R.id.my_f_cw_save :
                if ( !adapter.isCheck() ) {
                    Toast.makeText(getApplicationContext(), "선택된 데이타가 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //메뉴 선택 다이얼로그 생성
                    Cursor cursor = db.rawQuery(DicQuery.getSentenceViewContextMenu(), null);
                    final String[] kindCodes = new String[cursor.getCount()];
                    final String[] kindCodeNames = new String[cursor.getCount()];

                    int idx = 0;
                    while (cursor.moveToNext()) {
                        kindCodes[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND"));
                        kindCodeNames[idx] = cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME"));
                        idx++;
                    }
                    cursor.close();

                    final AlertDialog.Builder dlg = new AlertDialog.Builder(this);
                    dlg.setTitle("단어장 선택");
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
                            adapter.save(kindCodes[mSelect]);
                            changeListView();

                            DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                            Toast.makeText(getApplicationContext(), "단어장에 추가하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dlg.show();
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

        adapter.editChange(isEditing);
    }
}

class NewsClickwordCursorAdapter extends CursorAdapter {
    private SQLiteDatabase mDb;
    public boolean[] isCheck;
    public int[] seq;
    public String[] entryId;
    private boolean isEditing = false;
    int fontSize = 0;

    public NewsClickwordCursorAdapter(Context context, Cursor cursor, SQLiteDatabase db, int flags) {
        super(context, cursor, 0);
        mDb = db;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );

        isCheck = new boolean[cursor.getCount()];
        seq = new int[cursor.getCount()];
        entryId = new String[cursor.getCount()];
        while ( cursor.moveToNext() ) {
            isCheck[cursor.getPosition()] = false;
            seq[cursor.getPosition()] = cursor.getInt(cursor.getColumnIndexOrThrow("SEQ"));
            entryId[cursor.getPosition()] = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        }
        cursor.moveToFirst();
    }

    static class ViewHolder {
        protected int position;
        protected CheckBox cb;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_news_click_word_item, parent, false);

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

        //seq[cursor.getPosition()] = cursor.getInt(cursor.getColumnIndexOrThrow("SEQ"));
        //entryId[cursor.getPosition()] = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));

        ((TextView) view.findViewById(R.id.my_tv_word)).setText(cursor.getString(cursor.getColumnIndexOrThrow("WORD")));
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setText(cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
        ((TextView) view.findViewById(R.id.my_tv_date)).setText(cursor.getString(cursor.getColumnIndexOrThrow("INS_DATE")));
        ((TextView) view.findViewById(R.id.my_tv_mean)).setText(cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_word)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_spelling)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_date)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_mean)).setTextSize(fontSize);

        if ( isCheck[cursor.getPosition()] ) {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_on_background);
        } else {
            ((CheckBox)view.findViewById(R.id.my_cb_check)).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }

        if ( isEditing ) {
            ((RelativeLayout) view.findViewById(R.id.my_f_ci_rl)).setVisibility(View.VISIBLE);
        } else {
            ((RelativeLayout) view.findViewById(R.id.my_f_ci_rl)).setVisibility(View.GONE);
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
                DicDb.delDicClickWord(mDb, seq[i]);
            }
        }
    }

    public void save(String kind) {
        for ( int i = 0; i < isCheck.length; i++ ) {
            if ( isCheck[i] ) {
                DicDb.insDicVoc(mDb, entryId[i], kind);
                DicDb.delDicClickWord(mDb, seq[i]);
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


