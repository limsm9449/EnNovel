package com.sleepingbear.ennovel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class StudyActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    //private StudyCursorAdapter adapter;

    private String mVocKind = "MY";
    private int mStudyKind = 1;
    private String mStudyKindName = "";
    private String mMemorization = "ALL";

    private ArrayList<String> mVocKindAl;
    private ArrayList<String> mVocKindNameAl;

    ArrayAdapter studyAdapter;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        getVocKind();
        Spinner vocKind = (Spinner) findViewById(R.id.my_f_stu_s_vockind);
        ArrayAdapter<String> vocAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mVocKindNameAl);
        vocAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vocKind.setAdapter(vocAdapter);
        vocKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mVocKind = mVocKindAl.get(parent.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Spinner studyKind = (Spinner) findViewById(R.id.my_f_stu_s_studykind);
        studyAdapter = ArrayAdapter.createFromResource(this, R.array.studyKind, android.R.layout.simple_spinner_item);
        studyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        studyKind.setAdapter(studyAdapter);
        studyKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                mStudyKind = parent.getSelectedItemPosition();
                mStudyKindName = studyAdapter.getItem(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        RadioButton rb_all = (RadioButton) findViewById(R.id.my_f_stu_rb_all);
        rb_all.setOnClickListener(this);

        RadioButton rb_m = (RadioButton) findViewById(R.id.my_f_stu_rb_m);
        rb_m.setOnClickListener(this);

        RadioButton rb_m_not = (RadioButton) findViewById(R.id.my_f_stu_rb_m_not);
        rb_m_not.setOnClickListener(this);

        Button b_start = (Button) findViewById(R.id.my_f_stu_b_start);
        b_start.setOnClickListener(this);

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new  AdRequest.Builder().build();
        av.loadAd(adRequest);
    }


    public void getVocKind() {
        if ( mVocKindAl == null ) {
            mVocKindAl = new ArrayList<String>();
            mVocKindNameAl = new ArrayList<String>();
        }  else {
            mVocKindAl.clear();
            mVocKindNameAl.clear();
        }
        Cursor cursor = db.rawQuery(DicQuery.getVocCategory(), null);

        while ( cursor.moveToNext() ) {
            mVocKindAl.add(cursor.getString(cursor.getColumnIndexOrThrow("KIND")));
            mVocKindNameAl.add(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")));
        }
    }

    @Override
    public void onClick(View v) {
        if ( v.getId() == R.id.my_f_stu_rb_all ) {
            mMemorization = "";
        } else if ( v.getId() == R.id.my_f_stu_rb_m ) {
            mMemorization = "Y";
        } else if ( v.getId() == R.id.my_f_stu_rb_m_not ) {
            mMemorization = "N";
        } else if ( v.getId() == R.id.my_f_stu_b_start ) {
            Bundle bundle = new Bundle();
            bundle.putString("vocKind", mVocKind);
            bundle.putString("studyKindName", mStudyKindName);
            bundle.putString("memorization", mMemorization);

            Cursor cursor = db.rawQuery(DicQuery.getVocabularyCount(mVocKind), null);
            if ( cursor.moveToNext() ) {
                if ( cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) == 0 ) {
                    new android.support.v7.app.AlertDialog.Builder(this)
                            .setTitle("알림")
                            .setMessage("단어장에 등록된 단어가 없습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();

                    return;
                }
            }

            if ( mStudyKind == CommConstants.studyKind1 ) {
                Intent intent = new Intent(this.getApplication(), Study1Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            } else if ( mStudyKind == CommConstants.studyKind2 ) {
                Intent intent = new Intent(this.getApplication(), Study2Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            } else if ( mStudyKind == CommConstants.studyKind3 ) {
                Intent intent = new Intent(this.getApplication(), Study3Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            } else if ( mStudyKind == CommConstants.studyKind4 ) {
                Intent intent = new Intent(this.getApplication(), Study4Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            } else if ( mStudyKind == CommConstants.studyKind5 ) {
                Intent intent = new Intent(this.getApplication(), Study5Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            } else if ( mStudyKind == CommConstants.studyKind6 ) {
                Intent intent = new Intent(this.getApplication(), Study6Activity.class);
                intent.putExtras(bundle);
                this.startActivity(intent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
