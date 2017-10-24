package com.sleepingbear.ennovel;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class Study1Activity extends AppCompatActivity implements View.OnClickListener {
    private String mVocKind;
    private String mMemorization;
    private String mSort = "QUESTION ASC";

    private String mWordMean;

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private Study1CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        Bundle b = this.getIntent().getExtras();
        mVocKind = b.getString("vocKind");
        mMemorization = b.getString("memorization");
        mWordMean = "WORD";

        ActionBar ab = getSupportActionBar();
        ab.setTitle(b.getString("studyKindName"));
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        RadioButton rb_all = (RadioButton) findViewById(R.id.my_a_study1_rb_all);
        rb_all.setOnClickListener(this);

        RadioButton rb_m = (RadioButton) findViewById(R.id.my_a_study1_rb_m);
        rb_m.setOnClickListener(this);

        RadioButton rb_m_not = (RadioButton) findViewById(R.id.my_a_study1_rb_m_not);
        rb_m_not.setOnClickListener(this);

        RadioButton rb_word = (RadioButton) findViewById(R.id.my_a_study1_rb_word);
        rb_word.setOnClickListener(this);

        RadioButton rb_mean = (RadioButton) findViewById(R.id.my_a_study1_rb_mean);
        rb_mean.setOnClickListener(this);

        findViewById(R.id.my_rb_sort_asc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_desc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_random).setOnClickListener(this);

        if ( "".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study1_rb_all)).setChecked(true);
        } else if ( "Y".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study1_rb_m)).setChecked(true);
        } else if ( "N".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study1_rb_m_not)).setChecked(true);
        }

        getListView();

        DicUtils.setAdView(this);
    }

    public void getListView() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT B.SEQ _id," + CommConstants.sqlCR);
        sql.append("       B.SEQ," + CommConstants.sqlCR);
        if ( "WORD".equals(mWordMean) ) {
            sql.append("       B.WORD QUESTION," + CommConstants.sqlCR);
            sql.append("       B.MEAN ANSWER," + CommConstants.sqlCR);
        } else {
            sql.append("       B.WORD ANSWER," + CommConstants.sqlCR);
            sql.append("       B.MEAN QUESTION," + CommConstants.sqlCR);
        }
        sql.append("       B.ENTRY_ID," + CommConstants.sqlCR);
        sql.append("       A.MEMORIZATION" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.KIND = '" + mVocKind + "' " + CommConstants.sqlCR);
        if (mMemorization.length() == 1) {
            sql.append("   AND A.MEMORIZATION = '" + mMemorization + "' " + CommConstants.sqlCR);
        }
        sql.append(" ORDER BY " + mSort + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.getCount() == 0 ) {
            new AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("데이타가 없습니다.\n암기 여부를 조정해 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }

        ListView listView = (ListView) findViewById(R.id.my_a_study1_lv);
        adapter = new Study1CursorAdapter(getApplicationContext(), cursor, this, db, mWordMean);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_a_study1_rb_all) {
            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_study1_rb_m) {
            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_study1_rb_m_not) {
            mMemorization = "N";
            getListView();
        } else if (v.getId() == R.id.my_a_study1_rb_word) {
            mWordMean = "WORD";
            getListView();
        } else if (v.getId() == R.id.my_a_study1_rb_mean) {
            mWordMean = "MEAN";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_asc) {
            mSort = "QUESTION ASC";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_desc) {
            mSort = "QUESTION DESC";
            getListView();
        } else if (v.getId() == R.id.my_rb_sort_random) {
            mSort = "RANDOM_SEQ";
            db.execSQL(DicQuery.updVocRandom());
            getListView();
        }
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
            bundle.putString("SCREEN", CommConstants.screen_study1);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}


class Study1CursorAdapter extends CursorAdapter {
    int fontSize = 0;
    private String mWordMean;
    private Activity mActivity;
    private SQLiteDatabase mDb;
    private Cursor mCursor;

    boolean[] isItemView;

    private int mSelect;

    static class ViewHolder {
        protected CheckBox memorizationCheck;
        protected int position;
        protected String enrtyId;
        protected String seq;
    }

    public Study1CursorAdapter(Context context, Cursor cursor, Activity activity, SQLiteDatabase db, String wordMean) {
        super(context, cursor, 0);
        mCursor = cursor;
        mActivity = activity;
        mDb = db;

        mWordMean = wordMean;

        //초기화
        isItemView = new boolean[cursor.getCount()];
        for ( int i = 0; i < isItemView.length; i++ ) {
            isItemView[i] = false;
        }

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;

        view = LayoutInflater.from(context).inflate(R.layout.content_study1_item, parent, false);

        //암기 체크
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.memorizationCheck = (CheckBox) view.findViewById(R.id.my_cb_memorization);
        viewHolder.memorizationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] params = ((String) v.getTag()).split(":");

                StringBuffer sql = new StringBuffer();
                sql.append("UPDATE DIC_VOC " + CommConstants.sqlCR);
                sql.append("   SET MEMORIZATION = '" + ( ((CheckBox)v.findViewById(R.id.my_cb_memorization)).isChecked() ? "Y" : "N") + "'" + CommConstants.sqlCR);
                sql.append(" WHERE ENTRY_ID = '" + params[0] + "' " + CommConstants.sqlCR);
                mDb.execSQL(sql.toString());

                mCursor.requery();
                mCursor.move(Integer.parseInt(params[1]));
                notifyDataSetChanged();
            }
        });

        //Item 선택
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();
                isItemView[viewHolder.position] = true;

                notifyDataSetChanged();
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();

                final AlertDialog.Builder dlg = new AlertDialog.Builder(mActivity);
                dlg.setTitle("메뉴 선택");
                dlg.setSingleChoiceItems(new String[]{"단어 보기", "전체 정답 보기"}, mSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSelect = arg1;
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mSelect == 0) {
                            ViewHolder viewHolder = (ViewHolder) v.getTag();

                            Intent intent = new Intent(mActivity.getApplication(), WordViewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("entryId", viewHolder.enrtyId);
                            bundle.putString("seq", viewHolder.seq);
                            intent.putExtras(bundle);

                            mActivity.startActivity(intent);
                        } else {
                            for (int i = 0; i < isItemView.length; i++) {
                                isItemView[i] = true;
                            }
                            notifyDataSetChanged();
                        }
                    }
                });
                dlg.show();

                return true;
            }
        });

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder =(ViewHolder) view.getTag();

        viewHolder.memorizationCheck.setTag(cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")) + ":" + cursor.getPosition());
        viewHolder.enrtyId = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        viewHolder.seq = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));
        viewHolder.position = cursor.getPosition();

        ((TextView) view.findViewById(R.id.my_tv_question)).setText(cursor.getString(cursor.getColumnIndexOrThrow("QUESTION")));
        if ( isItemView[cursor.getPosition()] ) {
            ((TextView) view.findViewById(R.id.my_tv_answer)).setText(cursor.getString(cursor.getColumnIndexOrThrow("ANSWER")));
        } else {
            ((TextView) view.findViewById(R.id.my_tv_answer)).setText("?");
        }

        //암기 체크박스
        String memorization = cursor.getString(cursor.getColumnIndexOrThrow("MEMORIZATION"));
        CheckBox cb_memorization = (CheckBox) view.findViewById(R.id.my_cb_memorization);
        if ("Y".equals(memorization)) {
            cb_memorization.setChecked(true);
        } else {
            cb_memorization.setChecked(false);
        }

        //UI 수정
        if ( "WORD".equals(mWordMean) ) {
            ((TextView) view.findViewById(R.id.my_tv_question)).setTextSize(15);
            ((TextView) view.findViewById(R.id.my_tv_answer)).setTextSize(13);
        } else {
            ((TextView) view.findViewById(R.id.my_tv_question)).setTextSize(13);
            ((TextView) view.findViewById(R.id.my_tv_answer)).setTextSize(15);
        }

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_question)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_answer)).setTextSize(fontSize);
    }
}