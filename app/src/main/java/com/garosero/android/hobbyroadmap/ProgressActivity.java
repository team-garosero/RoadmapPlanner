package com.garosero.android.hobbyroadmap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.garosero.android.hobbyroadmap.helper.DBHelper;
import com.garosero.android.hobbyroadmap.main.MainActivity;
import com.garosero.android.hobbyroadmap.network.NetworkFactory;
import com.garosero.android.hobbyroadmap.network.request.ReadUserRequest;
import com.garosero.android.hobbyroadmap.network.request.RequestListener;
import com.garosero.android.hobbyroadmap.network.response.UserResponse;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

//6-7초 소요
public class ProgressActivity extends AppCompatActivity {
//    final String API_KEY = "CLaq8zUYyGtiDmwvSX0dVOYdP10C7dBQzSXaV9j%2BW%2BErU5N0jzyAomUqqPnzBWCR8YggYr6%2FBEbCvksX2BQCsw%3D%3D";
    final String API_KEY = "8J6CPUUandk0YLso7B5y/z0I67lSDrVpiQhJcmagUHOlDHLTkuQA84FEMIt3Ii//T2DDh/4lOxwMFhPJzFA6Mg==";
    final String TAG = "ProgressActivity";

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        DBHelper helper = new DBHelper(ProgressActivity.this, "newdb.db", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();

        // fetch api data
        if(helper.exists(db)){
            Log.d("ProgressActivity","DB already exists");
            startActivity(new Intent(this, MainActivity.class));
            db.close();
            finish();

        } else {
//            helper.onCreate(db);
            helper.onUpgrade(db,1,1);
            Log.d("ProgressActivity","create DB");
            new Thread(() -> {
                try {
//                long before = System.currentTimeMillis();
                    for (int i = 1; i < 25; i++) {
                        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B490007/ncsStudyModule/openapi21"); /*URL*/
                        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "="+API_KEY); /*Service Key*/
                        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
                        urlBuilder.append("&" + URLEncoder.encode("returnType","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*전송받을 페이지 타입*/
                        urlBuilder.append("&" + URLEncoder.encode("ncsLclasCd","UTF-8") + "=" + URLEncoder.encode("01", "UTF-8")); /*대분류코드*/
                        URL url = new URL(urlBuilder.toString());
//                        String lClassCd = i < 10 ? "0" + i : i + "";
//                        String queryUrl = "http://apis.data.go.kr/B490007/ncsStudyModule/openapi21?serviceKey=" + API_KEY
//                                + "&pageNo=" + "1" + "&numOfRows=" + "16" + "&returnType=xml" + "&ncsLclasCd=" + lClassCd;
//
//                        URL url = new URL(queryUrl);
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setRequestMethod("GET");
                        con.setDoOutput(true);
                        con.setDoInput(true);
                        con.setRequestProperty("Content-type", "application/xml");
                        con.setRequestProperty("Accept", "application/xml");

                        Log.e("progress",con.getResponseCode()+"");

//                        InputStream is = con.getInputStream();
                        if(con.getResponseCode() >= 200 && con.getResponseCode() <= 300) {
                        } else {
                            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = rd.readLine()) != null) {
                                sb.append(line);
                            }
                            rd.close();
                            Log.e("progress",sb.toString());
                        }

                        InputStream is  = con.getInputStream();

                        // xml parsing
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser xpp = factory.newPullParser();
                        xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream 으로부터 xml 입력받기

                        String tag = "";
                        xpp.next();
                        int eventType = xpp.getEventType();

                        ContentValues values = new ContentValues();

                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            switch (eventType) {
                                case XmlPullParser.START_DOCUMENT:
                                    break;

                                case XmlPullParser.START_TAG:
                                    tag = xpp.getName();//get tag name

                                    switch (tag) {
                                        case "row":
                                            values = new ContentValues();
                                            break;
                                        case "ncsLclasCd":  // 대분류코드
                                            xpp.next();
                                            values.put("l_class_code",xpp.getText());
                                            break;
                                        case "ncsLclasCdnm":  // 중분류코드명
                                            xpp.next();
                                            values.put("l_class_name",xpp.getText());
                                            break;
                                        case "ncsMclasCd":  // 중분류코드
                                            xpp.next();
                                            values.put("m_class_code",xpp.getText());
                                            break;
                                        case "ncsMclasCdnm":  // 중분류코드명
                                            xpp.next();
                                            values.put("m_class_name",xpp.getText());
                                            break;
                                        case "ncsSclasCd":  // 소분류코드
                                            xpp.next();
                                            values.put("s_class_code",xpp.getText());
                                            break;
                                        case "ncsSclasCdnm":  // 소분류코드명
                                            xpp.next();
                                            values.put("s_class_name",xpp.getText());
                                            break;
                                        case "ncsSubdCd":  // 세분류코드
                                            xpp.next();
                                            values.put("sub_class_code",xpp.getText());
                                            break;
                                        case "ncsSubdCdnm":  // 세분류코드명
                                            xpp.next();
                                            values.put("sub_class_name",xpp.getText());
                                            break;
                                        case "learnModulSeq":  // 학습모듈번호
                                            xpp.next();
                                            values.put("module_num",xpp.getText());
                                            break;
                                        case "learnModulName":  // 학습모듈명
                                            xpp.next();
                                            values.put("module_name",xpp.getText());
                                            break;
                                        case "learnModulText":  // 학습모듈내용
                                            xpp.next();
                                            values.put("module_text",xpp.getText());
                                            break;
                                    }
                                    break;

                                case XmlPullParser.TEXT:
                                    break;

                                case XmlPullParser.END_TAG:
                                    tag = xpp.getName();
                                    if (tag.equals("row")) {
                                        db.insertWithOnConflict("module_table",null,values,SQLiteDatabase.CONFLICT_REPLACE);
//                                    db.insert("module_table",null,values);
                                    }
                                    break;
                            }
                            eventType = xpp.next();
                        }
                        con.disconnect();
                    }
                    startActivity(new Intent(this, MainActivity.class));
                    db.close();
                    finish();
//                long after = System.currentTimeMillis();
//                Log.d(TAG, (after - before) / 1000 + "");
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }).start();

        } // end if

    }
}