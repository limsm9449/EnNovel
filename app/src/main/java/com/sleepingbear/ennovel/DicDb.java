package com.sleepingbear.ennovel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class DicDb {

    /**
     * 단어장에 등록한다.
     * @param db
     * @param entryId
     * @param kind
     */
    public static void insDicVoc(SQLiteDatabase db, String entryId, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + kind + "', ENTRY_ID, 'N', RANDOM(), '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".")  + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 단어장에서 삭제한다.
     * @param db
     * @param entryId
     */
    public static void delDicVocAll(SQLiteDatabase db, String entryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 뜻을 가져온다.
     * @param db
     * @param word
     * @return
     */
    public static HashMap getMean(SQLiteDatabase db, String word) {
        HashMap rtn = new HashMap();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SPELLING, MEAN, ENTRY_ID  " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word.toLowerCase().replaceAll("'", " ") + "' OR TENSE LIKE '% " + word.toLowerCase().replaceAll("'", " ") + " %'" + CommConstants.sqlCR);
        sql.append("ORDER  BY SPELLING DESC " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn.put("SPELLING", cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            rtn.put("MEAN", cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
            rtn.put("ENTRY_ID", cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")));
        } else {
            rtn = getMeanOther(db, word);
        }
        cursor.close();

        return rtn;
    }

    public static HashMap getMeanOther(SQLiteDatabase db, String word) {
        HashMap rtn = new HashMap();
        String findWord = "";

        if ( "s".indexOf(word.substring(word.length() - 1)) > -1 ) {
            findWord = word.substring(0, word.length() - 1);
        } else if ( word.length() > 2 && "es,ed,ly".indexOf(word.substring(word.length() - 2)) > -1 ) {
            findWord = word.substring(0, word.length() - 2);
        } else if ( word.length() > 3 && "ing".indexOf(word.substring(word.length() - 3))  > -1 ) {
            findWord = word.substring(0, word.length() - 3);
        } else {
            findWord = word;
        }
        DicUtils.dicLog("findWord : " + findWord);

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT SPELLING, MEAN, ENTRY_ID  " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + findWord.toLowerCase().replaceAll("'", " ") + "'" +  CommConstants.sqlCR);
        sql.append("ORDER  BY SPELLING DESC " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn.put("SPELLING", cursor.getString(cursor.getColumnIndexOrThrow("SPELLING")));
            rtn.put("MEAN", cursor.getString(cursor.getColumnIndexOrThrow("MEAN")));
            rtn.put("ENTRY_ID", cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID")));
        }
        cursor.close();

        return rtn;
    }

    /**
     * 클릭한 단어를 저장한다.
     * @param db
     * @param entryId
     * @param insDate
     */
    public static void insDicClickWord(SQLiteDatabase db, String entryId, String insDate) {
        if ( "".equals(insDate) ) {
            insDate = DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".");
        }

        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_CLICK_WORD (ENTRY_ID, INS_DATE) " + CommConstants.sqlCR);
        sql.append("VALUES ( '" + entryId + "','" + insDate + "') " + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicClickWord(SQLiteDatabase db, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        sql.append(" WHERE SEQ = " + seq + "" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void initDicClickWord(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_CLICK_WORD " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insConversationStudy(SQLiteDatabase db, String sampleSeq, String insDate) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("FROM    DIC_CODE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE_GROUP = 'C02'" +  CommConstants.sqlCR);
        sql.append("AND     CODE = '" + insDate + "'" + CommConstants.sqlCR);
        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            if ( cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) == 0 ) {
                sql.setLength(0);
                sql.append("INSERT INTO DIC_CODE(CODE_GROUP, CODE, CODE_NAME) " + CommConstants.sqlCR);
                sql.append("VALUES('C02', '" + insDate + "', '" + insDate + "')" + CommConstants.sqlCR);
                db.execSQL(sql.toString());
            }
        }
        cursor.close();

        sql.setLength(0);
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + insDate + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + insDate + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("UPDATE  DIC_CODE " + CommConstants.sqlCR);
        sql.append("SET     CODE_NAME = CODE || ' - ' || ( SELECT COUNT(*) FROM DIC_NOTE WHERE CODE = DIC_CODE.CODE ) || '개를 학습 하셨습니다.'  " + CommConstants.sqlCR);
        sql.append("WHERE   CODE_GROUP = 'C02' AND CODE = '" + insDate + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insConversationToNote(SQLiteDatabase db, String code, String sampleSeq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + code + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + code + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static boolean isExistMySample(SQLiteDatabase db, String sampleSeq) {
        boolean rtn = false;

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) CNT  " + CommConstants.sqlCR);
        sql.append("  FROM DIC_NOTE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE LIKE 'C01%'" + CommConstants.sqlCR);
        sql.append("   AND SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) > 0) {
                rtn = true;
            }
        }
        cursor.close();

        return rtn;
    }

    public static String getEntryIdForWord(SQLiteDatabase db, String word) {
        String rtn = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT ENTRY_ID  " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn = cursor.getString(cursor.getColumnIndexOrThrow("ENTRY_ID"));
        }
        cursor.close();

        return rtn;
    }

    public static void updMemory(SQLiteDatabase db, String entryId, String memoryYn) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE DIC_VOC " + CommConstants.sqlCR);
        sql.append("   SET MEMORIZATION = '" + memoryYn + "'" + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "' " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDicVoc(SQLiteDatabase db, String entryId, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void moveDicVoc(SQLiteDatabase db, String currKind, String copyKind, String entryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + copyKind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + copyKind + "', ENTRY_ID, 'N', RANDOM(), '" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + currKind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void updDaumCategoryInfo(SQLiteDatabase db, String categoryId, String categoryName, String updDate, String bookmarkCnt) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE DAUM_CATEGORY " + CommConstants.sqlCR);
        sql.append("SET    CATEGORY_NAME = '" + categoryName + "'" + CommConstants.sqlCR);
        sql.append("       ,UPD_DATE = '" + updDate + "'" + CommConstants.sqlCR);
        sql.append("       ,BOOKMARK_CNT = '" + bookmarkCnt + "'" + CommConstants.sqlCR);
        sql.append("WHERE  CATEGORY_ID = '" + categoryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDaumCategoryInfo(SQLiteDatabase db, String kind, String categoryId, String categoryName, String updDate, String wordCnt, String bookmarkCnt) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DAUM_CATEGORY(KIND, CATEGORY_ID, CATEGORY_NAME, UPD_DATE, WORD_CNT, BOOKMARK_CNT) " + CommConstants.sqlCR);
        sql.append("VALUES ('" + kind + "','" + categoryId + "','" + categoryName + "','" + updDate + "'," + wordCnt + "," + bookmarkCnt + ")" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delDaumVocabulary(SQLiteDatabase db, String categoryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DAUM_VOCABULARY " + CommConstants.sqlCR);
        sql.append(" WHERE CATEGORY_ID = '" + categoryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDaumVocabulary(SQLiteDatabase db, String categoryId, ArrayList wordAl) {
        StringBuffer sql = new StringBuffer();

        String words = "";
        for ( int i = 0; i < wordAl.size(); i++ ) {
            if ( i == 0 ) {
                words += ((String)((HashMap)wordAl.get(i)).get("WORD")).replaceAll("'","");
            } else {
                words += "," + ((String)((HashMap)wordAl.get(i)).get("WORD")).replaceAll("'","");
            }
        }

        sql.delete(0, sql.length());
        sql.append("INSERT INTO DAUM_VOCABULARY (CATEGORY_ID, WORD, ENTRY_ID) " + CommConstants.sqlCR);
        sql.append("SELECT '" + categoryId + "' CODE, WORD, ENTRY_ID" + CommConstants.sqlCR);
        sql.append("FROM   DIC " + CommConstants.sqlCR);
        sql.append("WHERE  KIND = 'F'" + CommConstants.sqlCR);
        sql.append("AND    WORD IN ('" + words.replaceAll(",","','") + "')" + CommConstants.sqlCR);
        sql.append("AND    SPELLING != ''" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void updDaumCategoryWordCount(SQLiteDatabase db, String categoryId) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE DAUM_CATEGORY " + CommConstants.sqlCR);
        sql.append("SET    WORD_CNT = (SELECT COUNT(*) FROM DAUM_VOCABULARY WHERE CATEGORY_ID = '" + categoryId + "')" + CommConstants.sqlCR);
        sql.append("WHERE  CATEGORY_ID = '" + categoryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delConversationFromNote(SQLiteDatabase db, String code, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append(" WHERE CODE = '" + code + "'" + CommConstants.sqlCR);
        sql.append("   AND SAMPLE_SEQ = " + seq + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void moveConversationToNote(SQLiteDatabase db, String currKind, String copyKind, int sampleSeq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + copyKind + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = '" + sampleSeq + "'" + CommConstants.sqlCR);
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_NOTE (CODE, SAMPLE_SEQ) " + CommConstants.sqlCR);
        sql.append("VALUES('" + copyKind + "', " + sampleSeq + ") " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("DELETE  FROM DIC_NOTE " + CommConstants.sqlCR);
        sql.append("WHERE   CODE = '" + currKind + "'" + CommConstants.sqlCR);
        sql.append("AND     SAMPLE_SEQ = " + sampleSeq + CommConstants.sqlCR);
        db.execSQL(sql.toString());
    }

    /**
     * 단어장 초기화
     * @param db
     */
    public static void initVocabulary(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE != 'VOC0001'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void initHistory(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 학습 회화 초기화
     * @param db
     */
    public static void initConversationNote(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP = 'C02') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C02'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * My 학습 초기화
     * @param db
     */
    public static void initMyConversationNote(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOTE WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP = 'C01') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
        sql.append("   AND CODE != 'C010001'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 코드 등록
     * @param db
     * @param groupCode
     * @param code
     * @param codeName
     */
    public static void insCode(SQLiteDatabase db, String groupCode, String code, String codeName) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_CODE (CODE_GROUP, CODE, CODE_NAME) " + CommConstants.sqlCR);
        sql.append("VALUES('" + groupCode + "', '" + code + "', '" + codeName + "') " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insDicVoc(SQLiteDatabase db, String kind, String entryId, String insDate, String memory) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_VOC " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append("   AND ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_VOC (KIND, ENTRY_ID, MEMORIZATION,RANDOM_SEQ, INS_DATE) " + CommConstants.sqlCR);
        sql.append("SELECT '" + kind + "', ENTRY_ID, '" + memory + "', RANDOM(), '" + insDate + "' " + CommConstants.sqlCR);
        sql.append("  FROM DIC " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static boolean isExistDaumVocabulary(SQLiteDatabase db, String categoryId) {
        boolean rtn = false;

        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) CNT  " + CommConstants.sqlCR);
        sql.append("  FROM DAUM_VOCABULARY " + CommConstants.sqlCR);
        sql.append(" WHERE CATEGORY_ID = '" + categoryId + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if (cursor.moveToNext()) {
            if (cursor.getInt(cursor.getColumnIndexOrThrow("CNT")) > 0) {
                rtn = true;
            }
        }
        cursor.close();

        return rtn;
    }

    public static void insSearchHistory(SQLiteDatabase db, String word) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY " + CommConstants.sqlCR);
        sql.append(" WHERE WORD = '" + word + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.setLength(0);
        sql.append("INSERT INTO DIC_SEARCH_HISTORY (WORD) " + CommConstants.sqlCR);
        sql.append("VALUES( '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insSearchHistory(SQLiteDatabase db, String seq, String word) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_SEARCH_HISTORY (SEQ, WORD) " + CommConstants.sqlCR);
        sql.append("VALUES( '" + seq + "', '" + word + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delSearchHistory(SQLiteDatabase db, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_SEARCH_HISTORY " + CommConstants.sqlCR);
        sql.append(" WHERE SEQ = " + seq + "" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insToday10(SQLiteDatabase db, String today) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_TODAY(TODAY, ENTRY_ID) " + CommConstants.sqlCR);
        sql.append("SELECT '" + today + "', ENTRY_ID FROM (" + CommConstants.sqlCR);
        sql.append("SELECT ENTRY_ID, WORD, RANDOM() RND" + CommConstants.sqlCR);
        sql.append("  FROM DAUM_VOCABULARY " + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID NOT IN ( SELECT ENTRY_ID FROM DIC_TODAY ) " + CommConstants.sqlCR);
        sql.append(" ) ORDER BY RND LIMIT 10" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    /**
     * 오늘이 단어 초기화
     * @param db
     */
    public static void initToday(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_TODAY" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void delNovel(SQLiteDatabase db, String kind) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_NOVEL" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insNovel(SQLiteDatabase db, String kind, String title, String url) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_NOVEL(KIND, TITLE, URL) " + CommConstants.sqlCR);
        sql.append("VALUES ('" + kind + "','" + title.replaceAll("'", "") + "','" + url.replaceAll("'", "") + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static int getNovelCount(SQLiteDatabase db, String kind) {
        int rtn = 0;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT COUNT(*) CNT  " + CommConstants.sqlCR);
        sql.append("  FROM DIC_NOVEL " + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        Cursor cursor = db.rawQuery(sql.toString(), null);
        if ( cursor.moveToNext() ) {
            rtn = cursor.getInt(cursor.getColumnIndexOrThrow("CNT"));
        }
        cursor.close();

        return rtn;
    }

    public static void delMyNovel(SQLiteDatabase db, int seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_MY_NOVEL " + CommConstants.sqlCR);
        sql.append(" WHERE SEQ = " + seq + "" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insMyNovel(SQLiteDatabase db, String title, String path) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE  FROM DIC_MY_NOVEL" + CommConstants.sqlCR);
        sql.append("WHERE   TITLE = '" + title + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());

        sql.delete(0, sql.length());
        sql.append("INSERT INTO DIC_MY_NOVEL (TITLE, PATH, INS_DATE, FAVORITE_FLAG)" + CommConstants.sqlCR);
        sql.append("VALUES('" + title + "','" + path + "','" + DicUtils.getDelimiterDate(DicUtils.getCurrentDate(), ".") + "','N')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void insMyNovel(SQLiteDatabase db, String title, String path, String insDate, String favoriteFlag) {
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO DIC_MY_NOVEL (TITLE, PATH, INS_DATE, FAVORITE_FLAG)" + CommConstants.sqlCR);
        sql.append("VALUES('" + title + "','" + path + "','" + insDate + "','" +  favoriteFlag + "')" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void updMyFavorite(SQLiteDatabase db, String seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE  DIC_MY_NOVEL" + CommConstants.sqlCR);
        sql.append("   SET  FAVORITE_FLAG = 'Y'" + CommConstants.sqlCR);
        sql.append("WHERE   SEQ = " + seq + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void updMyUnFavorite(SQLiteDatabase db, String seq) {
        StringBuffer sql = new StringBuffer();
        sql.append("UPDATE  DIC_MY_NOVEL" + CommConstants.sqlCR);
        sql.append("   SET  FAVORITE_FLAG = 'N'" + CommConstants.sqlCR);
        sql.append("WHERE   SEQ = " + seq + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

    public static void initMyNovel(SQLiteDatabase db) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM DIC_MY_NOVEL " + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());
        db.execSQL(sql.toString());
    }

}
