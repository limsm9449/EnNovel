package com.sleepingbear.ennovel;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.PatternSyntaxException;


public class DicUtils {
    public static String getString(String str) {
        if (str == null)
            return "";
        else
            return str.trim();
    }

    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return year + "" + (month + 1 > 9 ? "" : "0") + (month + 1) + "" + (day > 9 ? "" : "0") + day;
    }

    public static String getAddDay(String date, int addDay) {
        String mDate = date.replaceAll("[.-/]", "");

        int year = Integer.parseInt(mDate.substring(0, 4));
        int month = Integer.parseInt(mDate.substring(4, 6)) - 1;
        int day = Integer.parseInt(mDate.substring(6, 8));

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day + addDay);

        return c.get(Calendar.YEAR) + "" + (c.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (c.get(Calendar.MONTH) + 1) + "" + (c.get(Calendar.DAY_OF_MONTH) > 9 ? "" : "0") + c.get(Calendar.DAY_OF_MONTH);
    }

    public static String getDelimiterDate(String date, String delimiter) {
        if (getString(date).length() < 8) {
            return "";
        } else {
            return date.substring(0, 4) + delimiter + date.substring(4, 6) + delimiter + date.substring(6, 8);
        }
    }

    public static String getYear(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(0, 4);
        }
    }

    public static String getMonth(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(4, 6);
        }
    }

    public static String getDay(String date) {
        if (date == null) {
            return "";
        } else {
            String mDate = date.replaceAll("[.-/]", "");
            return mDate.substring(6, 8);
        }
    }

    public static void dicSqlLog(String str) {
        if (BuildConfig.DEBUG) {
            Log.d(CommConstants.tag + " ====>", str);
        }
    }

    public static void dicLog(String str) {
        if (BuildConfig.DEBUG) {
            Calendar cal = Calendar.getInstance();
            String time = cal.get(Calendar.HOUR_OF_DAY) + "시 " + cal.get(Calendar.MINUTE) + "분 " + cal.get(Calendar.SECOND) + "초";

            Log.d(CommConstants.tag + " ====>", time + " : " + str);
        }
    }

    public static String lpadding(String str, int length, String fillStr) {
        String rtn = "";

        for (int i = 0; i < length - str.length(); i++) {
            rtn += fillStr;
        }
        return rtn + (str == null ? "" : str);
    }

    public static String[] sentenceSplit(String sentence) {
        ArrayList<String> al = new ArrayList<String>();

        if ( sentence != null ) {
            String tmpSentence = sentence + " ";

            int startPos = 0;
            for (int i = 0; i < tmpSentence.length(); i++) {
                if (CommConstants.sentenceSplitStr.indexOf(tmpSentence.substring(i, i + 1)) > -1) {
                    if (i == 0) {
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    } else {
                        if (i != startPos) {
                            al.add(tmpSentence.substring(startPos, i));
                        }
                        al.add(tmpSentence.substring(i, i + 1));
                        startPos = i + 1;
                    }
                }
            }
        }

        String[] stringArr = new String[al.size()];
        stringArr = al.toArray(stringArr);

        return stringArr;
    }

    public static String getSentenceWord(String[] sentence, int kind, int position) {
        String rtn = "";
        if ( kind == 1 ) {
            rtn = sentence[position];
        } else if ( kind == 2 ) {
            if ( position + 2 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2];
                }
            }
        } else if ( kind == 3 ) {
            if ( position + 4 <= sentence.length - 1 ) {
                if ( " ".equals(sentence[position + 1]) && " ".equals(sentence[position + 3]) ) {
                    rtn = sentence[position] + sentence[position + 1] + sentence[position + 2] + sentence[position + 3] + sentence[position + 4];
                }
            }
        }

        //dicLog(rtn);
        return rtn;
    }

    public static String getOneSpelling(String spelling) {
        String rtn = "";
        String[] str = spelling.split(",");
        if ( str.length == 1 ) {
            rtn = spelling;
        } else {
            rtn = str[0] + "(" + str[1] + ")";
        }

        return rtn;
    }

    public static void readInfoFromFile(Context ctx, SQLiteDatabase db, String fileName) {
        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile start, " + fileName);

        //데이타 복구
        FileInputStream fis = null;
        try {
            //데이타 초기화
            DicDb.initVocabulary(db);
            DicDb.initDicClickWord(db);
            DicDb.initMyNovel(db);

            if ( "".equals(fileName) ) {
                fis = ctx.openFileInput(CommConstants.infoFileName);
            } else {
                fis = new FileInputStream(new File(fileName));
            }

            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader buffreader = new BufferedReader(isr);

            //출력...
            String readString = buffreader.readLine();
            while (readString != null) {
                dicLog(readString);

                String[] row = readString.split(":");
                if ( row[0].equals(CommConstants.tag_code_ins) ) {
                    DicDb.insCode(db, row[1], row[2], row[3]);
                } else if ( row[0].equals(CommConstants.tag_voc_ins) ) {
                    DicDb.insDicVoc(db, row[1], row[2], row[3], row[4]);
                } else if ( row[0].equals(CommConstants.tag_click_word_ins) ) {
                    DicDb.insDicClickWord(db, row[1], row[2]);
                } else if ( row[0].equals(CommConstants.tag_novel_ins) ) {
                    DicDb.insMyNovel(db, row[1], row[2], row[3], row[4]);
                }

                readString = buffreader.readLine();
            }

            isr.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dicLog(DicUtils.class.toString() + " : " + "readInfoFromFile end");
    }

    /**
     * 데이타 기록
     * @param ctx
     * @param db
     */
    public static void writeInfoToFile(Context ctx, SQLiteDatabase db, String fileName) {
        System.out.println("writeNewInfoToFile start");

        try {
            FileOutputStream fos = null;

            if ( "".equals(fileName) ) {
                fos = ctx.openFileOutput(CommConstants.infoFileName, Context.MODE_PRIVATE);
            } else {
                File saveFile = new File(fileName);
                try {
                    saveFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                fos = new FileOutputStream(saveFile);
            }

            Cursor cursor = db.rawQuery(DicQuery.getWriteData(), null);
            while (cursor.moveToNext()) {
                String writeData = cursor.getString(cursor.getColumnIndexOrThrow("WRITE_DATA"));
                DicUtils.dicLog(writeData);
                if ( writeData != null ) {
                    fos.write((writeData.getBytes()));
                    fos.write("\n".getBytes());
                }
            }
            cursor.close();

            fos.close();
        } catch (Exception e) {
            DicUtils.dicLog("File 에러=" + e.toString());
        }

        System.out.println("writeNewInfoToFile end");
    }

    public static boolean isHangule(String pStr) {
        boolean isHangule = false;
        String str = (pStr == null ? "" : pStr);
        try {
            isHangule = str.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }

        return isHangule;
    }

    public static Document getDocument(String url) throws Exception {
        Document doc = null;
        //while (true) {
        //    try {
                doc = Jsoup.connect(url).timeout(60000).get();
        //        break;
        //    } catch (Exception e) {
        //        System.out.println(e.getMessage());
        //    }
        //}

        return doc;
    }

    public static Element findElementSelect(Document doc, String tag, String attr, String value) throws Exception {
        Elements es = doc.select(tag);
        for (Element es_r : es) {
            if (value.equals(es_r.attr(attr))) {
                return es_r;
            }
        }

        return null;
    }

    public static Element findElementForTag(Element e, String tag, int findIdx) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i);
                } else {
                    idx++;
                }
            }
        }

        return null;
    }

    public static Element findElementForTagAttr(Element e, String tag, String attr, String value) throws Exception {
        if (e == null) {
            return null;
        }

        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName()) && value.equals(e.child(i).attr(attr))) {
                return e.child(i);
            }
        }

        return null;
    }

    public static String getAttrForTagIdx(Element e, String tag, int findIdx, String attr) throws Exception {
        if (e == null) {
            return null;
        }

        int idx = 0;
        for (int i = 0; i < e.children().size(); i++) {
            if (tag.equals(e.child(i).tagName())) {
                if (idx == findIdx) {
                    return e.child(i).attr(attr);
                } else {
                    idx++;
                }
            }
        }

        return "";
    }

    public static String getElementText(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.text();
        }
    }

    public static String getElementHtml(Element e) throws Exception {
        if (e == null) {
            return "";
        } else {
            return e.html();
        }
    }

    public static String getUrlParamValue(String url, String param) throws Exception {
        String rtn = "";

        if (url.indexOf("?") < 0) {
            return "";
        }
        String[] split_url = url.split("[?]");
        String[] split_param = split_url[1].split("[&]");
        for (int i = 0; i < split_param.length; i++) {
            String[] split_row = split_param[i].split("[=]");
            if (param.equals(split_row[0])) {
                rtn = split_row[1];
            }
        }

        return rtn;
    }

    public static Boolean isNetWork(AppCompatActivity context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService (Context.CONNECTIVITY_SERVICE);
        boolean isMobileAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
        boolean isMobileConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        boolean isWifiAvailable = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isAvailable();
        boolean isWifiConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

        return (isWifiAvailable && isWifiConnect) || (isMobileAvailable && isMobileConnect);
    }

    public static String getBtnString(String word){
        String rtn = "";

        if ( word.length() == 1 ) {
            rtn = "  " + word + "  ";
        } else if ( word.length() == 2 ) {
            rtn = "  " + word + " ";
        } else if ( word.length() == 3 ) {
            rtn = " " + word + " ";
        } else if ( word.length() == 4 ) {
            rtn = " " + word;
        } else {
            rtn = " " + word + " ";
        }

        return rtn;
    }

    public static void setDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "Y");
        editor.commit();

        dicLog(DicUtils.class.toString() + " setDbChange : " + "Y");
    }

    public static String getDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(CommConstants.flag_dbChange, "N");
    }

    public static void clearDbChange(Context mContext) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CommConstants.flag_dbChange, "N");
        editor.commit();
    }

    public static String getPreferencesValue(Context context, String preference) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String rtn = sharedPref.getString( preference, "" );
        if ( "".equals( rtn ) ) {
            if ( preference.equals(CommConstants.preferences_font) ) {
                rtn = "17";
            } else if ( preference.equals(CommConstants.preferences_wordView) ) {
                rtn = "0";
            } else if ( preference.equals(CommConstants.preferences_webViewFont) ) {
                rtn = "3";
            } else {
                rtn = "";
            }
        }

        DicUtils.dicLog(rtn);

        return rtn;
    }

    public static ArrayList gatherCategory(SQLiteDatabase db, String url, String codeGroup) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            boolean isBreak = false;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Element table_e = findElementSelect(doc, "table", "class", "tbl_wordbook");
                Element tbody_e = findElementForTag(table_e, "tbody", 0);
                for (int m = 0; m < tbody_e.children().size(); m++) {
                    HashMap row = new HashMap();

                    Element category = findElementForTag(tbody_e.child(m), "td", 1);

                    String categoryId = getUrlParamValue(category.child(0).attr("href"), "id").replace("\n", "");
                    String categoryName = category.text();
                    String wordCnt = findElementForTag(tbody_e.child(m), "td", 3).text();
                    String bookmarkCnt = findElementForTag(tbody_e.child(m), "td", 4).text();
                    String updDate = findElementForTag(tbody_e.child(m), "td", 5).text();
                    dicLog(codeGroup + " : " + categoryName + " : " + categoryId + " : " + categoryName + " : " + wordCnt + " : " + bookmarkCnt + " : " + updDate) ;
                    Cursor cursor = db.rawQuery(DicQuery.getDaumCategory(categoryId), null);
                    if (cursor.moveToNext()) {
                        if ( categoryId.equals(cursor.getString(cursor.getColumnIndexOrThrow("CATEGORY_ID"))) && updDate.equals(cursor.getString(cursor.getColumnIndexOrThrow("UPD_DATE"))) ) {
                            isBreak = true;
                            break;
                        } else {
                            //수정
                            DicDb.updDaumCategoryInfo(db, categoryId, categoryName, updDate, bookmarkCnt);
                        }
                    } else {
                        //입력
                        DicDb.insDaumCategoryInfo(db, codeGroup, categoryId, categoryName, updDate, wordCnt, bookmarkCnt);
                    }
                }

                if ( isBreak ) {
                    break;
                }

                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    dicLog("cnt : " + cnt);
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static ArrayList gatherCategoryWord(String url) {
        ArrayList wordAl = new ArrayList();
        try {
            int cnt = 1;
            while (true) {
                Document doc = getDocument(url + "&page=" + cnt);
                Element div_e = findElementSelect(doc, "div", "class", "list_word on");
                for (int is = 0; is < div_e.children().size(); is++) {
                    if ("div".equals(div_e.child(is).tagName())) {
                        HashMap row = new HashMap();

                        Element wordDiv = findElementForTagAttr(div_e.child(is), "div", "class", "txt_word");

                        row.put("WORD", wordDiv.child(0).child(0).text());
                        wordAl.add(row);
                    }
                }
                HashMap pageHm = new HashMap();
                Element div_paging = findElementSelect(doc, "div", "class", "paging_comm paging_type1");
                for (int is = 0; is < div_paging.children().size(); is++) {
                    if ("a".equals(div_paging.child(is).tagName())) {
                        HashMap row = new HashMap();

                        String page = getUrlParamValue(div_paging.child(is).attr("href"), "page");
                        pageHm.put(page, page);
                    }
                }
                // 페이지 정보중에 다음 페이지가 없으면 종료...
                if (!pageHm.containsKey(Integer.toString(cnt + 1))) {
                    break;
                } else {
                    cnt++;
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return wordAl;
    }

    public static void getNovelList0(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList1(SQLiteDatabase db, String url, String kind) {
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.titlelist li");

            DicDb.delNovel(db, kind);

            for (int m = 0; m < es.size(); m++) {
                DicDb.insNovel(db, kind, es.get(m).text(), es.get(m).child(0).attr("href"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static void getNovelList2(SQLiteDatabase db, String url, String kind) {
        dicLog("getNovelList2 : " + url);
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li.menu-li-bottom p.paginate-bar");
            String pageStr = es.get(0).text().trim().replaceAll("Page ","").replaceAll("of ","").split(" ")[1];
            int page = Integer.parseInt(pageStr);

            ArrayList al = new ArrayList();
            for ( int i = 1; i <= page; i++ ) {
                String pageUrl = url;
                if ( i > 1 ) {
                    doc = getDocument(url + "&page=" + i);
                }
                Elements es2 = doc.select("li.list-li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a.list-link");
                    Elements esImg = es2.get(m).select("img");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", esImg.get(0).attr("alt"));
                        al.add(hm);
                    }
                }
                es2 = doc.select("ul#s-list-ul li");
                for ( int m = 0; m < es2.size(); m++ ) {
                    //dicLog(i + " page " + m + " td");

                    Elements esA = es2.get(m).select("a");
                    if ( esA.size() > 0 ) {
                        HashMap hm = new HashMap();
                        hm.put("url", esA.get(0).attr("href"));
                        hm.put("title", es2.get(m).text().replaceAll("[:]", ""));
                        al.add(hm);
                    }
                }
            }

            DicDb.delNovel(db, kind);

            for (int i = 0; i < al.size(); i++) {
                DicDb.insNovel(db, kind, (String)((HashMap)al.get(i)).get("title"), (String)((HashMap)al.get(i)).get("url"));
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }
    }

    public static int getNovelPartCount0(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("li a");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static int getNovelPartCount1(String url) {
        int partSize = 0;
        try {
            Document doc = getDocument(url);
            Elements es = doc.select("ul.chapter-list li");
            partSize = es.size();
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return partSize;
    }

    public static String getNovelContent0(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td font");
            rtn = contents.get(1).html().replaceAll("<br /> <br />", "\n").replaceAll("&quot;","\"").replaceAll("<br />","");
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent1(String url) {
        String rtn = "";
        try {
            Document doc = getDocument(url);
            Elements contents = doc.select("td.chapter-text span.chapter-heading");
            if ( contents.size() > 0 ) {
                rtn += contents.get(0).text() + "\n\n\n";
            }

            contents = doc.select("td.chapter-text p");
            for ( int i = 0; i < contents.size(); i++ ) {
                rtn += contents.get(i).text() + "\n\n";
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn;
    }

    public static String getNovelContent2(String url) {
        StringBuffer rtn = new StringBuffer();
        try {
            Document doc = getDocument(url);
            Elements esA = doc.select("ul#book-ul a");
            for ( int i = 0; i < esA.size(); i++ ) {
                if ( esA.get(i).attr("href").indexOf(".txt") >= 0 ) {
                    InputStream inputStream = new URL("http://www.loyalbooks.com" + esA.get(i).attr("href")).openStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while((line = rd.readLine()) != null) {
                        rtn.append(line);
                        rtn.append('\n');
                    }
                    rd.close();
                }
            }
        } catch ( Exception e ) {
            Log.d(CommConstants.tag, e.getMessage());
        }

        return rtn.toString();
    }

    public static File getFIle(String folderName, String fileName) {
        File appDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File saveFile = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + folderName + "/" + fileName);

        return saveFile;
    }

    public static String getHtmlString(String contents, int fontSize) {
        StringBuffer sb = new StringBuffer();
        sb.append("<!doctype html>");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("</head>");
        sb.append("<script src='https://code.jquery.com/jquery-1.11.3.js'></script>");
        sb.append("<script>");
        sb.append("$( document ).ready(function() {");
        sb.append("    $('#contents').html(function(index, oldHtml) {");
        sb.append("        return oldHtml.replace(/<[^>]*>/g, '').replace(/(<br>)/g, '\\n').replace(/\\b(\\w+?)\\b/g,'<span class=\"word\">$1</span>').replace(/\\n/g, '<br>')");
        sb.append("    });");
        sb.append("    $('.word').click(function(event) {");
        sb.append("        window.android.setWord(event.target.innerHTML)");
        sb.append("    });");
        sb.append("});");
        sb.append("</script>");

        sb.append("<body>");
        sb.append("<font size='" + fontSize + "' face='돋움'><div id='contents'>");
        sb.append(contents);
        sb.append("</div></font></body>");
        sb.append("</html>");

        return sb.toString();
    }

    public static String getMyNovelContent(String path) {
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                content += temp + "\n";
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        return content;
    }

    public static String getFilePageContent(String path, int pageSize, int page) {
        //dicLog("getFilePageContent : " + pageSize + " : " + page);
        String content = "";
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            int getContentSize = 0;
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
                if ( getContentSize > ( page - 1 ) * pageSize && getContentSize < page * pageSize ) {
                    content += temp + "\n";
                } else if ( getContentSize > page * pageSize ) {
                    break;
                }
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        //dicLog("content length : " + content.length());
        return content;
    }

    public static int getFilePageCount(String path, int pageSize) {
        int getContentSize = 0;
        try {
            FileInputStream fis = new FileInputStream(new File(path));
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String temp = "";
            while( (temp = br.readLine()) != null) {
                getContentSize += temp.length();
            }

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                isr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        int pageCount = (int)Math.ceil(getContentSize / pageSize);
        if ( getContentSize - pageCount * pageSize > 0 ) {
            pageCount++;
        }
        //dicLog("content page : " + getContentSize + " : " + pageSize + " : " + pageCount);
        return pageCount;
    }

}