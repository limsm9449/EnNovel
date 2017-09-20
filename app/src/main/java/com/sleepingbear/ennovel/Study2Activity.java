package com.sleepingbear.ennovel;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Random;

public class Study2Activity extends AppCompatActivity implements View.OnClickListener {
    private String mVocKind;
    private String mMemorization;

    private String mWordMean;

    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private Study2CursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study2);
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

        RadioButton rb_all = (RadioButton) findViewById(R.id.my_a_study2_rb_all);
        rb_all.setOnClickListener(this);

        RadioButton rb_m = (RadioButton) findViewById(R.id.my_a_study2_rb_m);
        rb_m.setOnClickListener(this);

        RadioButton rb_m_not = (RadioButton) findViewById(R.id.my_a_study2_rb_m_not);
        rb_m_not.setOnClickListener(this);

        RadioButton rb_word = (RadioButton) findViewById(R.id.my_a_study2_rb_word);
        rb_word.setOnClickListener(this);

        RadioButton rb_mean = (RadioButton) findViewById(R.id.my_a_study2_rb_mean);
        rb_mean.setOnClickListener(this);

        Button b_random = (Button) findViewById(R.id.my_a_study2_b_random);
        b_random.setOnClickListener(this);

        if ( "".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study2_rb_all)).setChecked(true);
        } else if ( "Y".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study2_rb_m)).setChecked(true);
        } else if ( "N".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study2_rb_m_not)).setChecked(true);
        }

        getListView();

        AdView av = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);
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
        sql.append(" ORDER BY A.RANDOM_SEQ" + CommConstants.sqlCR);
        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.getCount() == 0 ) {
            new android.support.v7.app.AlertDialog.Builder(this)
                    .setTitle("알림")
                    .setMessage("데이타가 없습니다.\n암기 여부, 일자 조건을 조정해 주세요.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }

        //4지 선다형 답 데이타
        String[] sampleAnswer = getAnswer(mVocKind, cursor.getCount());
        int idx = 0;
        ArrayList<Study2Item> answerAl = new ArrayList<Study2Item>();
        for ( int i = 0; i < cursor.getCount(); i++ ) {
            Study2Item row = new Study2Item();

            if ( cursor.moveToNext() ) {
                //4지선다형 답
                row.answer1 = sampleAnswer[idx++];
                row.answer2 = sampleAnswer[idx++];
                row.answer3 = sampleAnswer[idx++];
                row.answer4 = sampleAnswer[idx++];

                Random r = new Random();
                int rnd = r.nextInt(4);
                row.answer = rnd + 1;
                if (row.answer == 1) {
                    row.answer1 = cursor.getString(cursor.getColumnIndexOrThrow("ANSWER"));
                } else if (row.answer == 2) {
                    row.answer2 = cursor.getString(cursor.getColumnIndexOrThrow("ANSWER"));
                } else if (row.answer == 3) {
                    row.answer3 = cursor.getString(cursor.getColumnIndexOrThrow("ANSWER"));
                } else if (row.answer == 4) {
                    row.answer4 = cursor.getString(cursor.getColumnIndexOrThrow("ANSWER"));
                }
            }

            answerAl.add(row);
        }

        ListView listView = (ListView) findViewById(R.id.my_a_study2_lv);
        cursor.moveToFirst();
        adapter = new Study2CursorAdapter(getApplicationContext(), cursor, this, db, mWordMean, answerAl);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listView.setSelection(0);
    }

    public String[] getAnswer(String vocKind, int answerCnt) {
        String[] sampleAnswer = new String[answerCnt * 4];

        int idx = 0;
        Cursor answerCursor = db.rawQuery(DicQuery.getSampleAnswerForStudy(mVocKind, answerCnt * 4), null);
        while ( answerCursor.moveToNext() ) {
            if ( "WORD".equals(mWordMean) ) {
                sampleAnswer[idx] = answerCursor.getString(answerCursor.getColumnIndexOrThrow("MEAN"));
            } else {
                sampleAnswer[idx] = answerCursor.getString(answerCursor.getColumnIndexOrThrow("WORD"));
            }

            idx++;
        }
        answerCursor.close();

        return sampleAnswer;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.my_a_study3_rb_all) {
            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_study2_rb_m) {
            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_study2_rb_m_not) {
            mMemorization = "N";
            getListView();
        } else if (v.getId() == R.id.my_a_study2_rb_word) {
            mWordMean = "WORD";
            getListView();
        } else if (v.getId() == R.id.my_a_study2_rb_mean) {
            mWordMean = "MEAN";
            getListView();
        } else if (v.getId() == R.id.my_a_study2_b_random) {
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
            bundle.putString("SCREEN", CommConstants.screen_study2);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

class Study2Item  {
    public int answer = -1;
    public int chkAnswer = -1;
    public String answer1 = "";
    public String answer2 = "";
    public String answer3 = "";
    public String answer4 = "";
    boolean isAnswerView = false;
}

class Study2CursorAdapter extends CursorAdapter {
    int fontSize = 0;

    private String mWordMean;
    private Activity mActivity;
    private SQLiteDatabase mDb;
    private Cursor mCursor;

    private int mSelect;

    private ArrayList<Study2Item> mAnswerAl;

    static class ViewHolder {
        protected String entryId;
        protected String seq;
        protected int position;

        protected CheckBox cb_answerCheck;
        protected TextView tv_result;
        protected TextView tv_question;
        protected RadioGroup rg_answer;
        protected RadioButton rb_answer1;
        protected RadioButton rb_answer2;
        protected RadioButton rb_answer3;
        protected RadioButton rb_answer4;
        protected CheckBox cb_memorizationCheck;
    }

    public Study2CursorAdapter(Context context, Cursor cursor, Activity activity, SQLiteDatabase db, String wordMean, ArrayList<Study2Item> answerAl) {
        super(context, cursor, 0);
        mCursor = cursor;
        mActivity = activity;
        mDb = db;

        mWordMean = wordMean;

        mAnswerAl = answerAl;

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        DicUtils.dicLog("newView : ==================");

        view = LayoutInflater.from(context).inflate(R.layout.content_study2_item, parent, false);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.tv_question = (TextView) view.findViewById(R.id.my_c_s2i_tv_question);

        viewHolder.rg_answer = (RadioGroup) view.findViewById(R.id.my_c_s2i_rg);
        viewHolder.rg_answer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ViewHolder viewHolder = (ViewHolder) group.getTag();

                switch (checkedId) {
                    case R.id.my_c_s2i_rb_answer1:
                        if ( viewHolder.rb_answer1.isChecked() ) {
                            mAnswerAl.get(viewHolder.position).chkAnswer = 1;
                        }
                        break;
                    case R.id.my_c_s2i_rb_answer2:
                        if ( viewHolder.rb_answer2.isChecked() ) {
                            mAnswerAl.get(viewHolder.position).chkAnswer = 2;
                        }
                        break;
                    case R.id.my_c_s2i_rb_answer3:
                        if ( viewHolder.rb_answer3.isChecked() ) {
                            mAnswerAl.get(viewHolder.position).chkAnswer = 3;
                        }
                        break;
                    case R.id.my_c_s2i_rb_answer4:
                        if ( viewHolder.rb_answer4.isChecked() ) {
                            mAnswerAl.get(viewHolder.position).chkAnswer = 4;
                        }
                        break;
                }
            }
        });
        viewHolder.rb_answer1 = (RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer1);
        viewHolder.rb_answer2 = (RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer2);
        viewHolder.rb_answer3 = (RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer3);
        viewHolder.rb_answer4 = (RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer4);

        //암기 체크
        viewHolder.cb_memorizationCheck = (CheckBox) view.findViewById(R.id.my_c_s2i_cb_memorization);
        viewHolder.cb_memorizationCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] params = ((String) v.getTag()).split(":");

                StringBuffer sql = new StringBuffer();
                sql.append("UPDATE DIC_VOC " + CommConstants.sqlCR);
                sql.append("   SET MEMORIZATION = '" + (((CheckBox) v.findViewById(R.id.my_c_s2i_cb_memorization)).isChecked() ? "Y" : "N") + "'" + CommConstants.sqlCR);
                sql.append(" WHERE ENTRY_ID = '" + params[0] + "' " + CommConstants.sqlCR);
                mDb.execSQL(sql.toString());

                mCursor.requery();
                mCursor.move(Integer.parseInt(params[1]));
                notifyDataSetChanged();
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                ViewHolder viewHolder = (ViewHolder) v.getTag();

                final android.support.v7.app.AlertDialog.Builder dlg = new android.support.v7.app.AlertDialog.Builder(mActivity);
                dlg.setTitle("메뉴 선택");
                dlg.setSingleChoiceItems(new String[]{"정답 보기", "단어 보기", "전체 정답 보기"}, mSelect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mSelect = arg1;
                    }
                });
                dlg.setNegativeButton("취소", null);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ViewHolder viewHolder = (ViewHolder) v.getTag();

                        if (mSelect == 0) {
                            mAnswerAl.get(viewHolder.position).isAnswerView = true;
                            notifyDataSetChanged();
                        } else if (mSelect == 1) {
                            Intent intent = new Intent(mActivity.getApplication(), WordViewActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("entryId", viewHolder.entryId);
                            bundle.putString("seq", viewHolder.seq);
                            intent.putExtras(bundle);

                            mActivity.startActivity(intent);
                        } else {
                            for (int i = 0; i < mAnswerAl.size(); i++) {
                                mAnswerAl.get(i).isAnswerView = true;
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
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.entryId = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        viewHolder.seq = cursor.getString(cursor.getColumnIndexOrThrow("SEQ"));
        viewHolder.position = cursor.getPosition();
        viewHolder.cb_memorizationCheck.setTag(cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")) + ":" + cursor.getPosition());
        viewHolder.rg_answer.setTag(viewHolder);

        ((TextView)view.findViewById(R.id.my_c_s2i_tv_question)).setText(cursor.getString(cursor.getColumnIndexOrThrow("QUESTION")));
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer1)).setText(mAnswerAl.get(cursor.getPosition()).answer1);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer2)).setText(mAnswerAl.get(cursor.getPosition()).answer2);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer3)).setText(mAnswerAl.get(cursor.getPosition()).answer3);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer4)).setText(mAnswerAl.get(cursor.getPosition()).answer4);

        //DicUtils.dicLog("chkAnswer : " +  cursor.getPosition() + " -> " + mAnswerAl.get(cursor.getPosition()).chkAnswer);
        if ( mAnswerAl.get(cursor.getPosition()).chkAnswer == -1 ) {
            viewHolder.rg_answer.clearCheck();
        } else {
            if (mAnswerAl.get(cursor.getPosition()).chkAnswer == 1) {
                ((RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer1)).setChecked(true);
            } else if (mAnswerAl.get(cursor.getPosition()).chkAnswer == 2) {
                ((RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer2)).setChecked(true);
            } else if (mAnswerAl.get(cursor.getPosition()).chkAnswer == 3) {
                ((RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer3)).setChecked(true);
            } else if (mAnswerAl.get(cursor.getPosition()).chkAnswer == 4) {
                ((RadioButton) view.findViewById(R.id.my_c_s2i_rb_answer4)).setChecked(true);
            }
        }

        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer1)).setTextColor(context.getResources().getColor(R.color.my_text_answer));
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer2)).setTextColor(context.getResources().getColor(R.color.my_text_answer));
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer3)).setTextColor(context.getResources().getColor(R.color.my_text_answer));
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer4)).setTextColor(context.getResources().getColor(R.color.my_text_answer));
        if ( mAnswerAl.get(cursor.getPosition()).isAnswerView ) {
            if ( mAnswerAl.get(cursor.getPosition()).answer == 1 ) {
                ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer1)).setTextColor(Color.RED);
            } else if ( mAnswerAl.get(cursor.getPosition()).answer == 2 ) {
                ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer2)).setTextColor(Color.RED);
            } else if ( mAnswerAl.get(cursor.getPosition()).answer == 3 ) {
                ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer3)).setTextColor(Color.RED);
            } else if ( mAnswerAl.get(cursor.getPosition()).answer == 4 ) {
                ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer4)).setTextColor(Color.RED);
            }

            if ( mAnswerAl.get(cursor.getPosition()).answer == mAnswerAl.get(cursor.getPosition()).chkAnswer ) {
                ((TextView) view.findViewById(R.id.my_c_s2i_tv_answer)).setText("정답");
            } else {
                ((TextView) view.findViewById(R.id.my_c_s2i_tv_answer)).setText("정답 ( " + mAnswerAl.get(cursor.getPosition()).answer + " )");
            }
        } else {
            ((TextView) view.findViewById(R.id.my_c_s2i_tv_answer)).setText("");
        }

        //암기 체크박스
        String memorization = cursor.getString(cursor.getColumnIndexOrThrow("MEMORIZATION"));
        CheckBox cb_memorization = (CheckBox) view.findViewById(R.id.my_c_s2i_cb_memorization);
        if ("Y".equals(memorization)) {
            cb_memorization.setChecked(true);
        } else {
            cb_memorization.setChecked(false);
        }

        //UI 수정
        ((TextView)view.findViewById(R.id.my_c_s2i_tv_question)).setTextSize(fontSize);
        ((TextView)view.findViewById(R.id.my_c_s2i_tv_answer)).setTextSize(fontSize);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer1)).setTextSize(fontSize);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer2)).setTextSize(fontSize);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer3)).setTextSize(fontSize);
        ((RadioButton)view.findViewById(R.id.my_c_s2i_rb_answer4)).setTextSize(fontSize);
    }
}