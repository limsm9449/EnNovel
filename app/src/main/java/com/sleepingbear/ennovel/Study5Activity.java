package com.sleepingbear.ennovel;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class Study5Activity extends AppCompatActivity implements View.OnClickListener {
    private String mVocKind;
    private String mMemorization;
    private String mSort = "QUESTION ASC";

    private String mWordMean;

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    private Cursor mCursor;
    private TextView tv_question;
    private TextView tv_spelling;
    private TextView tv_answer1;
    private TextView tv_answer2;
    private TextView tv_answer3;
    private TextView tv_answer4;
    private TextView tv_ox;
    private TextView tv_orgAnswer;
    private TextView tv_o_cnt;
    private TextView tv_x_cnt;
    private TextView tv_pos;
    private TextView tv_total;
    private SeekBar sb;

    private Thread mThread;

    private ArrayList<Study5Item> answerAl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study5);
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

        findViewById(R.id.my_a_study5_rb_all).setOnClickListener(this);
        findViewById(R.id.my_a_study5_rb_m).setOnClickListener(this);
        findViewById(R.id.my_a_study5_rb_m_not).setOnClickListener(this);
        findViewById(R.id.my_a_study5_rb_word).setOnClickListener(this);
        findViewById(R.id.my_a_study5_rb_mean).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_asc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_desc).setOnClickListener(this);
        findViewById(R.id.my_rb_sort_random).setOnClickListener(this);
        findViewById(R.id.my_a_study5_b_a1).setOnClickListener(this);
        findViewById(R.id.my_a_study5_b_a2).setOnClickListener(this);
        findViewById(R.id.my_a_study5_b_a3).setOnClickListener(this);
        findViewById(R.id.my_a_study5_b_a4).setOnClickListener(this);
        findViewById(R.id.my_a_study5_ib_first).setOnClickListener(this);
        findViewById(R.id.my_a_study5_ib_prev).setOnClickListener(this);
        findViewById(R.id.my_a_study5_ib_next).setOnClickListener(this);
        findViewById(R.id.my_a_study5_ib_last).setOnClickListener(this);

        tv_question = (TextView) findViewById(R.id.my_a_study5_tv_question);
        tv_question.setText("");
        tv_spelling = (TextView) findViewById(R.id.my_a_study5_tv_spelling);
        tv_spelling.setText("");
        tv_answer1 = (TextView) findViewById(R.id.my_a_study5_tv_answer1);
        tv_answer1.setText("");
        tv_answer2 = (TextView) findViewById(R.id.my_a_study5_tv_answer2);
        tv_answer2.setText("");
        tv_answer3 = (TextView) findViewById(R.id.my_a_study5_tv_answer3);
        tv_answer3.setText("");
        tv_answer4 = (TextView) findViewById(R.id.my_a_study5_tv_answer4);
        tv_answer4.setText("");
        tv_ox = (TextView) findViewById(R.id.my_a_study5_tv_ox);
        tv_ox.setText("");
        tv_orgAnswer = (TextView) findViewById(R.id.my_a_study5_tv_orgAnswer);
        tv_orgAnswer.setText("");
        tv_o_cnt= (TextView) findViewById(R.id.my_a_study5_tv_o_cnt);
        tv_x_cnt = (TextView) findViewById(R.id.my_a_study5_tv_x_cnt);
        tv_pos = (TextView) findViewById(R.id.my_a_study5_tv_pos);
        tv_pos.setText("0");
        tv_total = (TextView) findViewById(R.id.my_a_study5_tv_total);
        tv_total.setText("0");

        if ( "".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study5_rb_all)).setChecked(true);
        } else if ( "Y".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study5_rb_m)).setChecked(true);
        } else if ( "N".equals(mMemorization) ) {
            ((RadioButton) findViewById(R.id.my_a_study5_rb_m_not)).setChecked(true);
        }

        int fontSize = Integer.parseInt( DicUtils.getPreferencesValue( this, CommConstants.preferences_font ) );
        tv_answer1.setTextSize(fontSize);
        tv_answer2.setTextSize(fontSize);
        tv_answer3.setTextSize(fontSize);
        tv_answer4.setTextSize(fontSize);
        tv_o_cnt.setTextSize(fontSize);
        tv_x_cnt.setTextSize(fontSize);

        sb = (SeekBar) findViewById(R.id.my_a_study5_sb);
        sb.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                  @Override
                  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                      if ( progress < mCursor.getCount() ) {
                          mCursor.moveToPosition(progress);
                          studyPlay();
                          tv_pos.setText(Integer.toString(progress + 1));
                      }
                  }

                  @Override
                  public void onStartTrackingTouch(SeekBar seekBar) {
                  }

                  @Override
                  public void onStopTrackingTouch(SeekBar seekBar) {
                  }
              }
        );

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
        sql.append("       A.MEMORIZATION," + CommConstants.sqlCR);
        sql.append("       B.SPELLING" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.KIND = '" + mVocKind + "' " + CommConstants.sqlCR);
        if (mMemorization.length() == 1) {
            sql.append("   AND A.MEMORIZATION = '" + mMemorization + "' " + CommConstants.sqlCR);
        }
        sql.append(" ORDER BY " + mSort + CommConstants.sqlCR);
        mCursor = db.rawQuery(sql.toString(), null);
        if ( mCursor.getCount() > 0 ) {
            //OX 답 데이타
            String[] sampleAnswer = getAnswer(mVocKind, mCursor.getCount());
            int idx = 0;

            answerAl = new ArrayList<Study5Item>();
            for (int i = 0; i < mCursor.getCount(); i++) {
                Study5Item row = new Study5Item();

                if ( mCursor.moveToNext() ) {
                    //4지선다형 답
                    row.answer1 = sampleAnswer[idx++];
                    row.answer2 = sampleAnswer[idx++];
                    row.answer3 = sampleAnswer[idx++];
                    row.answer4 = sampleAnswer[idx++];

                    Random r = new Random();
                    int rnd = r.nextInt(4);
                    row.answer = rnd + 1;
                    if (row.answer == 1) {
                        row.answer1 = mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER"));
                    } else if (row.answer == 2) {
                        row.answer2 = mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER"));
                    } else if (row.answer == 3) {
                        row.answer3 = mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER"));
                    } else if (row.answer == 4) {
                        row.answer4 = mCursor.getString(mCursor.getColumnIndexOrThrow("ANSWER"));
                    }

                    answerAl.add(row);
                }
            }

            mCursor.moveToFirst();
            sb.setMax(mCursor.getCount() - 1);
            sb.setProgress(mCursor.getPosition());
            tv_total.setText(Integer.toString(mCursor.getCount()));

            chgAnswerCnt();

            studyPlay();
        } else {
            /*sb.setMax(0);
            sb.setProgress(0);
            tv_pos.setText("0");
            tv_total.setText("0");

            tv_question.setText("");
            tv_spelling.setText("");
            tv_answer1.setText("");
            tv_answer2.setText("");
            tv_answer3.setText("");
            tv_answer4.setText("");
            tv_orgAnswer.setText("");
            tv_o_cnt.setText("");
            tv_x_cnt.setText("");
            tv_ox.setText("");
            tv_orgAnswer.setText("");*/

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
        if (v.getId() == R.id.my_a_study5_rb_all) {
            mMemorization = "";
            getListView();
        } else if (v.getId() == R.id.my_a_study5_rb_m) {
            mMemorization = "Y";
            getListView();
        } else if (v.getId() == R.id.my_a_study5_rb_m_not) {
            mMemorization = "N";
            getListView();
        } else if (v.getId() == R.id.my_a_study5_rb_word) {
            mWordMean = "WORD";
            tv_spelling.setVisibility(View.VISIBLE);
            getListView();
        } else if (v.getId() == R.id.my_a_study5_rb_mean) {
            mWordMean = "MEAN";
            tv_spelling.setVisibility(View.GONE);
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
        } else if (v.getId() == R.id.my_a_study5_b_a1 || v.getId() == R.id.my_a_study5_b_a2 || v.getId() == R.id.my_a_study5_b_a3 || v.getId() == R.id.my_a_study5_b_a4) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            tv_answer1.setTextColor(this.getResources().getColor(R.color.my_text_answer));
            tv_answer2.setTextColor(this.getResources().getColor(R.color.my_text_answer));
            tv_answer3.setTextColor(this.getResources().getColor(R.color.my_text_answer));
            tv_answer4.setTextColor(this.getResources().getColor(R.color.my_text_answer));

            if ( v.getId() == R.id.my_a_study5_b_a1 ) {
                answerAl.get(mCursor.getPosition()).chkAnswer = 1;
                tv_answer1.setTextColor(Color.RED);
            } else if ( v.getId() == R.id.my_a_study5_b_a2 ) {
                answerAl.get(mCursor.getPosition()).chkAnswer = 2;
                tv_answer2.setTextColor(Color.RED);
            } else if ( v.getId() == R.id.my_a_study5_b_a3 ) {
                answerAl.get(mCursor.getPosition()).chkAnswer = 3;
                tv_answer3.setTextColor(Color.RED);
            } else if ( v.getId() == R.id.my_a_study5_b_a4 ) {
                answerAl.get(mCursor.getPosition()).chkAnswer = 4;
                tv_answer4.setTextColor(Color.RED);
            }

            if ( answerAl.get(mCursor.getPosition()).answer == answerAl.get(mCursor.getPosition()).chkAnswer ) {
                tv_ox.setText("O");
                tv_orgAnswer.setText("");
                tv_ox.setVisibility(View.VISIBLE);
            } else {
                tv_ox.setText("X");
                if ( answerAl.get(mCursor.getPosition()).answer == 1 ) {
                    tv_orgAnswer.setText("정답 : 1번 - " + answerAl.get(mCursor.getPosition()).answer1);
                } else if ( answerAl.get(mCursor.getPosition()).answer == 2 ) {
                    tv_orgAnswer.setText("정답 : 2번 - " + answerAl.get(mCursor.getPosition()).answer2);
                } else if ( answerAl.get(mCursor.getPosition()).answer == 3 ) {
                    tv_orgAnswer.setText("정답 : 3번 - " + answerAl.get(mCursor.getPosition()).answer3);
                } else if ( answerAl.get(mCursor.getPosition()).answer == 4 ) {
                    tv_orgAnswer.setText("정답 : 4번 - " + answerAl.get(mCursor.getPosition()).answer4);
                }
                tv_ox.setVisibility(View.VISIBLE);
            }

            mThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (true) {
                            Thread.sleep(2000);

                            Message msg = handler.obtainMessage();
                            msg.arg1 = 0;
                            handler.sendMessage(msg);

                            break;
                        }
                    } catch ( InterruptedException e ) {
                        //interrupt 시 Thread 종료..
                    } finally {
                        //DicUtils.dicLog("Thread InterruptedException Close");
                    }
                }
            });
            mThread.start();

            chgAnswerCnt();
        } else if (v.getId() == R.id.my_a_study5_ib_first) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( mThread != null ) {
                mThread.interrupt();
            }

            mCursor.moveToFirst();
            studyPlay();
        } else if (v.getId() == R.id.my_a_study5_ib_prev) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( mThread != null ) {
                mThread.interrupt();
            }

            if ( !mCursor.isFirst() ) {
                mCursor.moveToPrevious();
                studyPlay();
            }
        } else if (v.getId() == R.id.my_a_study5_ib_next) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( mThread != null ) {
                mThread.interrupt();
            }

            if ( !mCursor.isLast() ) {
                mCursor.moveToNext();
                studyPlay();
            }
        } else if (v.getId() == R.id.my_a_study5_ib_last) {
            if ( mCursor.getCount() == 0 ) {
                return;
            }
            if ( mThread != null ) {
                mThread.interrupt();
            }

            mCursor.moveToLast();
            studyPlay();
        }
    }

    public void studyPlay() {
        sb.setProgress(mCursor.getPosition());

        tv_question.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("QUESTION")).replaceAll("2.", " 2.").replaceAll("3.", " 3.").replaceAll("4.", " 4.").replaceAll("5.", " 5."));
        tv_spelling.setText(mCursor.getString(mCursor.getColumnIndexOrThrow("SPELLING")));
        tv_answer1.setText("1번 : " + answerAl.get(mCursor.getPosition()).answer1.replaceAll("2.", " 2.").replaceAll("3.", " 3.").replaceAll("4.", " 4.").replaceAll("5.", " 5."));
        tv_answer2.setText("2번 : " + answerAl.get(mCursor.getPosition()).answer2.replaceAll("2.", " 2.").replaceAll("3.", " 3.").replaceAll("4.", " 4.").replaceAll("5.", " 5."));
        tv_answer3.setText("3번 : " + answerAl.get(mCursor.getPosition()).answer3.replaceAll("2.", " 2.").replaceAll("3.", " 3.").replaceAll("4.", " 4.").replaceAll("5.", " 5."));
        tv_answer4.setText("4번 : " + answerAl.get(mCursor.getPosition()).answer4.replaceAll("2.", " 2.").replaceAll("3.", " 3.").replaceAll("4.", " 4.").replaceAll("5.", " 5."));
        tv_orgAnswer.setText("");

        tv_answer1.setTextColor(this.getResources().getColor(R.color.my_text_answer));
        tv_answer2.setTextColor(this.getResources().getColor(R.color.my_text_answer));
        tv_answer3.setTextColor(this.getResources().getColor(R.color.my_text_answer));
        tv_answer4.setTextColor(this.getResources().getColor(R.color.my_text_answer));

        if (answerAl.get(mCursor.getPosition()).chkAnswer > 0) {
            if (answerAl.get(mCursor.getPosition()).answer == 1) {
                tv_answer1.setTextColor(Color.RED);
            } else if (answerAl.get(mCursor.getPosition()).answer == 2) {
                tv_answer2.setTextColor(Color.RED);
            } else if (answerAl.get(mCursor.getPosition()).answer == 3) {
                tv_answer3.setTextColor(Color.RED);
            } else if (answerAl.get(mCursor.getPosition()).answer == 4) {
                tv_answer4.setTextColor(Color.RED);
            }

            tv_ox.setVisibility(View.VISIBLE);
            if (answerAl.get(mCursor.getPosition()).answer == answerAl.get(mCursor.getPosition()).chkAnswer) {
                tv_ox.setText("O");
            } else {
                tv_ox.setText("X");

                if (answerAl.get(mCursor.getPosition()).answer == 1) {
                    tv_orgAnswer.setText("정답 : 1번 - " + answerAl.get(mCursor.getPosition()).answer1);
                } else if (answerAl.get(mCursor.getPosition()).answer == 2) {
                    tv_orgAnswer.setText("정답 : 2번 - " + answerAl.get(mCursor.getPosition()).answer2);
                } else if (answerAl.get(mCursor.getPosition()).answer == 3) {
                    tv_orgAnswer.setText("정답 : 3번 - " + answerAl.get(mCursor.getPosition()).answer3);
                } else if (answerAl.get(mCursor.getPosition()).answer == 4) {
                    tv_orgAnswer.setText("정답 : 4번 - " + answerAl.get(mCursor.getPosition()).answer4);
                }
            }
        } else {
            tv_ox.setVisibility(View.GONE);
        }
    }

    public void chgAnswerCnt() {
        int o_cnt = 0;
        int x_cnt = 0;
        for ( int i = 0; i < answerAl.size(); i++ ) {
            if ( answerAl.get(i).chkAnswer > 0 ) {
                if (answerAl.get(i).answer == answerAl.get(i).chkAnswer ) {
                    o_cnt++;
                } else {
                    x_cnt++;
                }
            }
        }

        tv_o_cnt.setText("정답 : " + Integer.toString(o_cnt));
        tv_x_cnt.setText("오답 : " + Integer.toString(x_cnt));
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
            if ( mThread != null ) {
                mThread.interrupt();
            }
            finish();
        } else if (id == R.id.action_help) {
            Bundle bundle = new Bundle();
            bundle.putString("SCREEN", CommConstants.screen_study5);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            //DicUtils.dicLog("Handler : " + msg.arg1 + " : " + mCursor.getPosition());
            if ( msg.arg1 == 0 ) {
                if ( !mCursor.isLast() ) {
                    mCursor.moveToNext();
                    studyPlay();

                    sb.setProgress(mCursor.getPosition());
                }
            }
        }
    };
}

class Study5Item  {
    public String answer1 = "";
    public String answer2 = "";
    public String answer3 = "";
    public String answer4 = "";
    public int answer = -1;
    public int chkAnswer = -1;
}