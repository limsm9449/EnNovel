package com.sleepingbear.ennovel;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class VocabularyNoteActivity extends AppCompatActivity implements View.OnClickListener {
    private DbHelper dbHelper;
    private SQLiteDatabase db;
    private VocabularyNoteCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_note);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final View dialog_layout = inflater.inflate(R.layout.dialog_category_add, null);

                //dialog 생성..
                AlertDialog.Builder builder = new AlertDialog.Builder(VocabularyNoteActivity.this);
                builder.setView(dialog_layout);
                final AlertDialog alertDialog = builder.create();

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
        });

        ActionBar ab = (ActionBar) getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);

        dbHelper = new DbHelper(this);
        db = dbHelper.getWritableDatabase();

        changeListView();

        AdView av = (AdView)findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        av.loadAd(adRequest);
    }

    public void changeListView() {
        DicUtils.dicLog(this.getClass().toString() + " changeListView");

        Cursor cursor = db.rawQuery(DicQuery.getVocabularyCategoryCount(), null);

        ListView listView = (ListView) findViewById(R.id.my_a_vocabulary_note_lv);
        adapter = new VocabularyNoteCursorAdapter(this, cursor, 0);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(itemClickListener);
        listView.setOnItemLongClickListener(itemLongClickListener);

        listView.setSelection(0);
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Cursor cur = (Cursor) adapter.getItem(position);

            Bundle bundle = new Bundle();
            bundle.putString("kind", cur.getString(cur.getColumnIndexOrThrow("KIND")));
            bundle.putString("kindName", cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

            Intent intent = new Intent(getApplication(), VocabularyNoteViewActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };

    AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final Cursor cur = (Cursor) adapter.getItem(position);

            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            //layout 구성
            final View dialog_layout = inflater.inflate(R.layout.dialog_category_iud, null);

            //dialog 생성..
            AlertDialog.Builder builder = new AlertDialog.Builder(VocabularyNoteActivity.this);
            builder.setView(dialog_layout);
            final AlertDialog alertDialog = builder.create();

            ((TextView) dialog_layout.findViewById(R.id.my_d_category_tv_category)).setText("단어장 관리");

            final EditText et_upd = ((EditText) dialog_layout.findViewById(R.id.my_d_category_et_upd));
            et_upd.setText(cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));

            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upd)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upd)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String code = (String) v.getTag();
                    if ("VOC0001".equals(code)) {
                        Toast.makeText(getApplicationContext(), "기본 단어장은 수정할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        if ("".equals(et_upd.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "단어장 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            alertDialog.dismiss();

                            db.execSQL(DicQuery.getUpdCategory(CommConstants.vocabularyCode, code, et_upd.getText().toString()));

                            changeListView();

                            Toast.makeText(getApplicationContext(), "단어장 이름을 수정하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_del)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_del)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String code = (String) v.getTag();

                    if ("VOC0001".equals(code)) {
                        Toast.makeText(getApplicationContext(), "기본 단어장은 삭제할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        alertDialog.dismiss();
                    } else {
                        new AlertDialog.Builder(VocabularyNoteActivity.this)
                                .setTitle("알림")
                                .setMessage("삭제된 데이타는 복구할 수 없습니다. 삭제하시겠습니까?")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alertDialog.dismiss();

                                        db.execSQL(DicQuery.getDelCategory(CommConstants.vocabularyCode, code));
                                        db.execSQL(DicQuery.getDelDicVoc(code));

                                        DicUtils.setDbChange(getApplicationContext());  //DB 변경 체크

                                        changeListView();

                                        Toast.makeText(getApplicationContext(), "단어장을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                }
            });

            final EditText et_saveName = ((EditText) dialog_layout.findViewById(R.id.my_dc_et_voc_name));
            et_saveName.setText(cur.getString(cur.getColumnIndexOrThrow("KIND_NAME")));
            ((Button) dialog_layout.findViewById(R.id.my_dc__b_save)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
            ((Button) dialog_layout.findViewById(R.id.my_dc__b_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String code = (String) v.getTag();

                    String saveFileName = et_saveName.getText().toString();
                    if ("".equals(saveFileName)) {
                        Toast.makeText(getApplicationContext(), "저장할 파일명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    } else if (saveFileName.indexOf(".") > -1 && !"txt".equals(saveFileName.substring(saveFileName.length() - 3, saveFileName.length()).toLowerCase())) {
                        Toast.makeText(getApplicationContext(), "확장자는 txt 입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        String fileName = "";

                        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName);
                        if (!appDir.exists()) {
                            appDir.mkdirs();

                            if (saveFileName.indexOf(".") > -1) {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                            } else {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                            }
                        } else {
                            if (saveFileName.indexOf(".") > -1) {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName;
                            } else {
                                fileName = Environment.getExternalStorageDirectory().getAbsoluteFile() + CommConstants.folderName + "/" + saveFileName + ".txt";
                            }
                        }

                        File saveFile = new File(fileName);
                        if (saveFile.exists()) {
                            Toast.makeText(getApplicationContext(), "파일명이 존재합니다.", Toast.LENGTH_SHORT).show();
                            ;
                        } else {
                            try {
                                saveFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                            }

                            BufferedWriter bw = null;
                            try {
                                bw = new BufferedWriter(new FileWriter(saveFile, true));

                                Cursor cursor = db.rawQuery(DicQuery.getSaveVocabulary(code), null);
                                while (cursor.moveToNext()) {
                                    bw.write(cursor.getString(cursor.getColumnIndexOrThrow("WORD")) + ": " + cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")) + " -> " + cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
                                    bw.newLine();
                                }

                                bw.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (bw != null) try {
                                    bw.close();
                                } catch (IOException ioe2) {
                                }
                            }

                            Toast.makeText(getApplicationContext(), "단어장을 정상적으로 내보냈습니다.", Toast.LENGTH_SHORT).show();

                            alertDialog.dismiss();
                        }
                    }
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upload)).setTag(cur.getString(cur.getColumnIndexOrThrow("KIND")));
            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_upload)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String code = (String) v.getTag();

                    FileChooser filechooser = new FileChooser(VocabularyNoteActivity.this);
                    filechooser.setFileListener(new FileChooser.FileSelectedListener() {
                        @Override
                        public void fileSelected(final File file) {
                            FileInputStream fis = null;
                            try {
                                fis = new FileInputStream(new File(file.getAbsolutePath()));
                                InputStreamReader isr = new InputStreamReader(fis);
                                BufferedReader buffreader = new BufferedReader(isr);

                                String readString = buffreader.readLine();
                                while (readString != null) {
                                    String[] dicInfo = readString.split(":");
                                    String entryId = DicDb.getEntryIdForWord(db, dicInfo[0]);
                                    DicDb.insDicVoc(db, entryId, code);

                                    readString = buffreader.readLine();
                                }
                                isr.close();

                                DicUtils.setDbChange(getApplicationContext()); //변경여부 체크

                                changeListView();

                                Toast.makeText(getApplicationContext(), "단어장을 정상적으로 가져왔습니다.", Toast.LENGTH_SHORT).show();

                                alertDialog.dismiss();

                                changeListView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    filechooser.setExtension("txt");
                    filechooser.showDialog();
                }
            });

            ((Button) dialog_layout.findViewById(R.id.my_d_category_b_close)).setOnClickListener(new View.OnClickListener() {
                                                                                                     @Override
                                                                                                     public void onClick(View v) {
                                                                                                         alertDialog.dismiss();
                                                                                                     }
                                                                                                 }
            );

            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            return true;
        }
    };

    @Override
    public void onClick(View v) {
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
            bundle.putString("SCREEN", CommConstants.screen_vocabularyNote);

            Intent intent = new Intent(getApplication(), HelpActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}


class VocabularyNoteCursorAdapter extends CursorAdapter {
    int fontSize = 0;

    public VocabularyNoteCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);

        fontSize = Integer.parseInt( DicUtils.getPreferencesValue( context, CommConstants.preferences_font ) );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.content_vocabulary_note_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ((TextView) view.findViewById(R.id.my_tv_pattern)).setText(cursor.getString(cursor.getColumnIndexOrThrow("KIND_NAME")));
        ((TextView) view.findViewById(R.id.my_tv_cnt)).setText(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow("CNT"))));

        //사이즈 설정
        ((TextView) view.findViewById(R.id.my_tv_pattern)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.my_tv_cnt)).setTextSize(fontSize);
    }
}