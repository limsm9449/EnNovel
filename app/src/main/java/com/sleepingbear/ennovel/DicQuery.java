package com.sleepingbear.ennovel;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DicQuery {

    public static String getWriteData() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT '" + CommConstants.tag_code_ins + "'||':'||A.CODE_GROUP||':'||A.CODE||':'||A.CODE_NAME WRITE_DATA" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP IN ('MY_VOC','C01','C02')" + CommConstants.sqlCR);
        sql.append("   AND CODE NOT IN ('VOC0001','C010001')" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_note_ins + "'||':'||CODE||':'||SAMPLE_SEQ WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_NOTE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE IN (SELECT CODE FROM DIC_CODE WHERE CODE_GROUP IN ('C01','C02') )" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_voc_ins + "'||':'||A.KIND||':'||A.ENTRY_ID||':'||A.INS_DATE||':'||A.MEMORIZATION WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_history_ins + "'||':'||SEQ||':'||WORD WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_SEARCH_HISTORY" + CommConstants.sqlCR);

        sql.append("UNION" + CommConstants.sqlCR);
        sql.append("SELECT '" + CommConstants.tag_click_word_ins + "'||':'||ENTRY_ID||':'||INS_DATE WRITE_DATA " + CommConstants.sqlCR);
        sql.append(" FROM DIC_CLICK_WORD" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 나의 단어장 목록을 가져온다.
     * @return
     */
    public static String getVocabularyCategory() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 사전 상세 정보를 가져온다.
     * @param entryId
     * @return
     */
    public static String getVocDetailForEntryId(String entryId) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, WORD, MEAN, ENTRY_ID, SPELLING, TENSE, TYPE, (SELECT COUNT(*) FROM DIC_VOC WHERE ENTRY_ID = '" + entryId + "') MY_VOC, KIND" + CommConstants.sqlCR);
        sql.append("  FROM DIC" + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID = '" + entryId + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * CONTEXT MENU에 사용할 단어장 목록을 가져온다.
     * @return
     */
    public static String getSentenceViewContextMenu() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 2 _id, 2 ORD, CODE KIND, CODE_NAME||' 등록' KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,4" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 단어장의 정렬을 변경한다.
     * @return
     */
    public static String updVocRandom() {
        StringBuffer sql = new StringBuffer();

        sql.append("UPDATE DIC_VOC" + CommConstants.sqlCR);
        sql.append("   SET RANDOM_SEQ = RANDOM()" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 4지선다형에 사용할 답을 가져온다.
     * @param vocKind
     * @param answerCnt
     * @return
     */
    public static String getSampleAnswerForStudy(String vocKind, int answerCnt) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id," + CommConstants.sqlCR);
        sql.append("       WORD," + CommConstants.sqlCR);
        sql.append("       MEAN" + CommConstants.sqlCR);
        sql.append("FROM   DIC" + CommConstants.sqlCR);
        sql.append("WHERE  ENTRY_ID NOT IN (SELECT ENTRY_ID FROM DIC_VOC WHERE KIND = '" + vocKind + "')" + CommConstants.sqlCR);
        sql.append("AND    SPELLING != ''" + CommConstants.sqlCR);
        sql.append("ORDER  BY RANDOM()" + CommConstants.sqlCR);
        sql.append("LIMIT  " + answerCnt + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 단어장 목록을 가져온다.
     * @return
     */
    public static String getVocCategory() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME," + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append("                       WHERE KIND = A.CODE" + CommConstants.sqlCR);
        sql.append("                       GROUP BY  KIND),0) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 단어장에 있는 단어 갯수를 가져온다.
     * @return
     */
    public static String getVocabularyCount(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, COUNT(*) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 뉴스에서 클릭한 단어 조회
     * @return
     */
    public static String getNewsClickword() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT B.SEQ _id, A.SEQ, B.ENTRY_ID, B.WORD, B.MEAN, B.SPELLING, A.INS_DATE" + CommConstants.sqlCR);
        sql.append("FROM   DIC_CLICK_WORD A, DIC B" + CommConstants.sqlCR);
        sql.append("WHERE  A.ENTRY_ID = B.ENTRY_ID " + CommConstants.sqlCR);
        sql.append("ORDER  BY A.INS_DATE DESC, B.WORD" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 추가할 단어장 코드를 가져온다.
     * @param mDb
     * @return
     */
    public static String getInsCategoryCode(SQLiteDatabase mDb) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT MAX(CODE) CODE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        String insCategoryCode = "";
        Cursor maxCategoryCursor = mDb.rawQuery(sql.toString(), null);
        if ( maxCategoryCursor.moveToNext() ) {
            String max = maxCategoryCursor.getString(maxCategoryCursor.getColumnIndexOrThrow("CODE"));
            int maxCategory = Integer.parseInt(max.substring(3,max.length()));
            insCategoryCode = "VOC" + DicUtils.lpadding(Integer.toString(maxCategory + 1), 4, "0");
            DicUtils.dicSqlLog("insCategoryCode : " + insCategoryCode);
        }

        return insCategoryCode;
    }

    /**
     * 코드를 등록한다.
     * @param codeGroup
     * @param code
     * @param codeName
     * @return
     */
    public static String getInsNewCategory(String codeGroup, String code, String codeName) {
        StringBuffer sql = new StringBuffer();

        sql.append("INSERT INTO DIC_CODE(CODE_GROUP, CODE, CODE_NAME)" + CommConstants.sqlCR);
        sql.append("VALUES('" + codeGroup + "', '" + code + "', '" + codeName + "')" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getConversationStudyList(int difficult) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("  FROM DIC_SAMPLE" + CommConstants.sqlCR);
        if ( difficult == 1) {
            sql.append("  WHERE WORD_CNT BETWEEN 4 AND 6" + CommConstants.sqlCR);
        } else if ( difficult == 2) {
            sql.append("  WHERE WORD_CNT BETWEEN 7 AND 10" + CommConstants.sqlCR);
        } else {
            sql.append("  WHERE WORD_CNT > 10" + CommConstants.sqlCR);
        }
        sql.append(" ORDER BY RANDOM()" + CommConstants.sqlCR);
        sql.append(" LIMIT 1000" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getPatternList() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, PATTERN, DESC, SQL_WHERE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_PATTERN" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getPatternSampleList(String pattern) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("FROM    DIC_SAMPLE" + CommConstants.sqlCR);
        sql.append("WHERE   SENTENCE1 LIKE '" + pattern + "'" + CommConstants.sqlCR);
        sql.append("ORDER   BY ORD" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 회화 context Menu
     * @param isStudyAndDetail
     * @return
     */
    public static String getNoteKindContextMenu(boolean isStudyAndDetail) {
        StringBuffer sql = new StringBuffer();

        if ( isStudyAndDetail ) {
            sql.append("SELECT 1 ORD, 'M1' KIND, '회화 학습' KIND_NAME" + CommConstants.sqlCR);
            sql.append("UNION ALL" + CommConstants.sqlCR);
            sql.append("SELECT 2 ORD, 'M2' KIND, '문장 상세' KIND_NAME" + CommConstants.sqlCR);
            sql.append("UNION ALL" + CommConstants.sqlCR);
            sql.append("SELECT 3 ORD, CODE KIND, CODE_NAME||' 회화에 추가' KIND_NAME" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
            sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
            sql.append(" ORDER BY ORD, KIND_NAME" + CommConstants.sqlCR);
        } else {
            sql.append("SELECT 3 ORD, CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
            sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
            sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
            sql.append(" ORDER BY ORD, KIND_NAME" + CommConstants.sqlCR);
        }

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSample(String sampleSeq) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("FROM    DIC_SAMPLE" + CommConstants.sqlCR);
        sql.append("WHERE   SEQ = " + sampleSeq + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 노트 데이타
     * @param code
     * @return
     */
    public static String getNoteList(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  B.SEQ _id, B.SEQ, B.SENTENCE1, B.SENTENCE2" + CommConstants.sqlCR);
        sql.append("FROM    DIC_NOTE A" + CommConstants.sqlCR);
        sql.append("        ,DIC_SAMPLE B" + CommConstants.sqlCR);
        sql.append("WHERE   A.SAMPLE_SEQ = B.SEQ " + CommConstants.sqlCR);
        sql.append("AND     A.CODE = '" + code + "'" + CommConstants.sqlCR);
        if ( "C03".equals(code.substring(0, 3)) ) {
            //네이버 회화는 SEQ로...
            sql.append("ORDER   BY A.SEQ" + CommConstants.sqlCR);
        } else {
            sql.append("ORDER   BY B.ORD" + CommConstants.sqlCR);
        }

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 노트 그룹 종류
     * @return
     */
    public static String getConversationNoteGroupKind() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id,CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'CONVERSATION'" + CommConstants.sqlCR);
        sql.append(" ORDER BY CODE" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 노트 코드들..
     * @param groupCode
     * @return
     */
    public static String getConversationNoteKind(String groupCode) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id,CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + groupCode + "'" + CommConstants.sqlCR);
        if ( "C02".equals(groupCode) ) {
            sql.append(" ORDER BY CODE_NAME DESC" + CommConstants.sqlCR);
        } else {
            sql.append(" ORDER BY CODE_NAME" + CommConstants.sqlCR);
        }

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 코드 삭제
     * @param code
     * @return
     */
    public static String getDelCode(String groupCode, String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + groupCode + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 노트 전체 내용 삭제
     * @param code
     * @return
     */
    public static String getDelNote(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_NOTE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 코드명 변경
     * @param code
     * @param codeName
     * @return
     */
    public static String getUpdCode(String groupCode, String code, String codeName) {
        StringBuffer sql = new StringBuffer();

        sql.append("UPDATE DIC_CODE" + CommConstants.sqlCR);
        sql.append("   SET CODE_NAME = '" + codeName + "'" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + groupCode + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getVocabularyCategoryCount() {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT 1 _id, CODE KIND, CODE_NAME KIND_NAME," + CommConstants.sqlCR);
        sql.append("            COALESCE((SELECT COUNT(*)" + CommConstants.sqlCR);
        sql.append("                        FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append("                       WHERE KIND = A.CODE),0) CNT" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE A" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + CommConstants.vocabularyCode + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY 1,3" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getUpdCategory(String codeGroup, String code, String codeName) {
        StringBuffer sql = new StringBuffer();

        sql.append("UPDATE DIC_CODE" + CommConstants.sqlCR);
        sql.append("   SET CODE_NAME = '" + codeName + "'" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + codeGroup + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDelCategory(String codeGroup, String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = '" + codeGroup + "'" + CommConstants.sqlCR);
        sql.append("   AND CODE = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDelDicVoc(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("DELETE FROM DIC_VOC" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + code + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getSaveVocabulary(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT B.WORD, B.SPELLING, B.MEAN" + CommConstants.sqlCR);
        sql.append("  FROM DIC_VOC A, DIC B" + CommConstants.sqlCR);
        sql.append(" WHERE A.ENTRY_ID = B.ENTRY_ID" + CommConstants.sqlCR);
        sql.append("   AND A.KIND = '" + kind + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY B.WORD" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 나를 제외한 단어장 종류
     * @param code
     * @return
     */
    public static String getVocabularyKindMeExceptContextMenu(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'MY_VOC'" + CommConstants.sqlCR);
        sql.append("   AND CODE != '" + code + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY CODE_NAME" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getNaverCategoryList() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT _id, CATEGORY" + CommConstants.sqlCR);
        sql.append("  FROM NAVER_CATEGORY" + CommConstants.sqlCR);
        sql.append(" ORDER BY CATEGORY" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getNaverConversationList(String category) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT B.SEQ _id, B.SEQ, B.SENTENCE1, B.SENTENCE2" + CommConstants.sqlCR);
        sql.append("  FROM NAVER_CONVERSATION A, DIC_SAMPLE B" + CommConstants.sqlCR);
        sql.append(" WHERE A.SAMPLE_SEQ = B.SEQ" + CommConstants.sqlCR);
        sql.append("   AND A.CATEGORY = '" + category + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY A.ORD" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDaumKind() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT 1 _id, 'TOEIC' KIND, 'TOEIC' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 2 _id, 'TOEFL' KIND, 'TOEFL' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 3 _id, 'TEPS' KIND, 'TEPS' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 4 _id, '수능영어' KIND, '수능영어' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 5 _id, 'NEAT/NEPT' KIND, 'NEAT/NEPT' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 6 _id, '초중고영어' KIND, '초중고영어' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 7 _id, '회화' KIND, '회화' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 8 _id, '기타' KIND, '기타' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 9 _id, 'R1' KIND, '사용 빈도수별 Toeic Rank' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 10 _id, 'R2' KIND, '사용 빈도수별 수능영어 Rank' KIND_NAME" + CommConstants.sqlCR);
        sql.append("UNION ALL" + CommConstants.sqlCR);
        sql.append("SELECT 11 _id, 'R3' KIND, '사용 빈도수별 초중고영어 Rank' KIND_NAME" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDaumSubCategoryCount(String kind, int mOrder) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT CATEGORY_ID _id, KIND, CATEGORY_ID, CATEGORY_NAME, UPD_DATE, WORD_CNT, BOOKMARK_CNT" + CommConstants.sqlCR);
        sql.append("  FROM DAUM_CATEGORY" + CommConstants.sqlCR);
        sql.append(" WHERE KIND = '" + kind + "'" + CommConstants.sqlCR);
        if ( mOrder == 0 ) {
            sql.append(" ORDER BY BOOKMARK_CNT" + CommConstants.sqlCR);
        } else if ( mOrder == 1 ) {
            sql.append(" ORDER BY BOOKMARK_CNT DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 2 ) {
            sql.append(" ORDER BY UPD_DATE" + CommConstants.sqlCR);
        } else if ( mOrder == 3 ) {
            sql.append(" ORDER BY UPD_DATE DESC" + CommConstants.sqlCR);
        } else if ( mOrder == 4 ) {
            sql.append(" ORDER BY CATEGORY_NAME" + CommConstants.sqlCR);
        } else if ( mOrder == 5 ) {
            sql.append(" ORDER BY CATEGORY_NAME DESC" + CommConstants.sqlCR);
        }
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDaumCategory(String categoryId) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT *" + CommConstants.sqlCR);
        sql.append("  FROM DAUM_CATEGORY" + CommConstants.sqlCR);
        sql.append(" WHERE CATEGORY_ID = '" + categoryId + "'" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDaumVocabulary(String categoryId) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, ENTRY_ID, WORD, SPELLING, MEAN" + CommConstants.sqlCR);
        sql.append("  FROM DIC" + CommConstants.sqlCR);
        sql.append(" WHERE ENTRY_ID IN (SELECT ENTRY_ID FROM DAUM_VOCABULARY WHERE CATEGORY_ID = '" + categoryId + "')" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 나를 제외한 노트 종류
     * @param code
     * @return
     */
    public static String getConversationNoteContextMenuExceptMe(String code) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT CODE KIND, CODE_NAME KIND_NAME" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
        sql.append("   AND CODE != '" + code + "'" + CommConstants.sqlCR);
        sql.append(" ORDER BY CODE_NAME" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getDictionaryHistoryword() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, WORD" + CommConstants.sqlCR);
        sql.append("FROM   DIC_SEARCH_HISTORY" + CommConstants.sqlCR);
        sql.append("ORDER  BY SEQ DESC" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    /**
     * 새로 추가할 My 회화 코드
     * @param mDb
     * @return
     */
    public static String getInsMyNoteCode(SQLiteDatabase mDb) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT MAX(CODE) CODE" + CommConstants.sqlCR);
        sql.append("  FROM DIC_CODE" + CommConstants.sqlCR);
        sql.append(" WHERE CODE_GROUP = 'C01'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        String insMyNoteCode = "";
        Cursor maxCategoryCursor = mDb.rawQuery(sql.toString(), null);
        if ( maxCategoryCursor.moveToNext() ) {
            String max = maxCategoryCursor.getString(maxCategoryCursor.getColumnIndexOrThrow("CODE"));
            int maxCategory = Integer.parseInt(max.substring(3,max.length()));
            insMyNoteCode = "C01" + DicUtils.lpadding(Integer.toString(maxCategory + 1), 4, "0");
            DicUtils.dicSqlLog("insMyNoteCode : " + insMyNoteCode);
        }

        return insMyNoteCode;
    }

    public static String getIdiomList() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, IDIOM, DESC, SQL_WHERE, SAME_IDIOM" + CommConstants.sqlCR);
        sql.append("  FROM DIC_IDIOM" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getIdiomSampleList(String idiom) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, SEQ, SENTENCE1, SENTENCE2" + CommConstants.sqlCR);
        sql.append("FROM    DIC_SAMPLE" + CommConstants.sqlCR);
        sql.append("WHERE   SENTENCE1 LIKE '" + idiom + "'" + CommConstants.sqlCR);
        sql.append("ORDER   BY ORD" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getNovelList(String kind) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT  SEQ _id, TITLE, URL" + CommConstants.sqlCR);
        sql.append("FROM    DIC_NOVEL" + CommConstants.sqlCR);
        sql.append("WHERE   KIND = '" + kind + "'" + CommConstants.sqlCR);
        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMyNovel() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, TITLE, PATH, INS_DATE" + CommConstants.sqlCR);
        sql.append("FROM   DIC_MY_NOVEL" + CommConstants.sqlCR);
        sql.append("ORDER  BY INS_DATE DESC" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }


    public static String getMyFavorite() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT SEQ _id, SEQ, TITLE, PATH, INS_DATE" + CommConstants.sqlCR);
        sql.append("FROM   DIC_MY_NOVEL" + CommConstants.sqlCR);
        sql.append("WHERE  FAVORITE_FLAG = 'Y'" + CommConstants.sqlCR);
        sql.append("ORDER  BY INS_DATE DESC" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

    public static String getMyFavoriteMessage() {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT -1 _id, -1 SEQ, '즐겨찾기 목록이 없습니다.' TITLE, '영어소설 화면 리스트에서 소설을 길게 클릭하시면 즐겨찾기로 등록이 됩니다.' PATH, '' INS_DATE" + CommConstants.sqlCR);

        DicUtils.dicSqlLog(sql.toString());

        return sql.toString();
    }

}
