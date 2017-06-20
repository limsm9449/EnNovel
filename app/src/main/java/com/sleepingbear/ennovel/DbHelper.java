package com.sleepingbear.ennovel;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DbHelper extends SQLiteOpenHelper {
    private static String DB_NAME ="ehdictandvoc.db";
    private String DB_PATH = "";
    private static final int DATABASE_VERSION = 9;
    private final Context mContext;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);

        mContext = context;

        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";

        initialize();
    }

    private void initialize() {
        if (databaseExists()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            int dbVersion = prefs.getInt("db_ver", 1);
            //System.out.println("dbVersion : " + dbVersion);

            if (DATABASE_VERSION != dbVersion) {
                File dbFile = new File(DB_PATH + DB_NAME);
                if (!dbFile.delete()) {
                    DicUtils.dicLog("Unable to update database");
                }
            }
        }
        if (!databaseExists()) {
            createDatabase();
        }
    }

    private boolean databaseExists() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void createDatabase() {
        //System.out.println("=============================================== DB Create =============================================================");

        String path = DB_PATH + DB_NAME;

        File file = new File(DB_PATH);
        if (!file.exists()) {
            if (!file.mkdir()) {
                DicUtils.dicLog("Unable to create database directory");
                return;
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = mContext.getAssets().open(DB_NAME);
            os = new FileOutputStream(path);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("db_ver", DATABASE_VERSION);
            editor.putString("db_new", "Y");
            editor.commit();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
