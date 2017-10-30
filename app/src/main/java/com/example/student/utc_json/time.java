package com.example.student.utc_json;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by student on 2017/10/13.
 */

public class time {

    private static class sync {
        private static long UTC_SYS = 0;
        private static long UTC_NTP = 0;
        private static long UTC_DELTA = 0;
    }

    private static Gson gson = new Gson();
    private static JSON_UTC ntp;

    //    public static final SimpleDateFormat sdf = new SimpleDateFormat("HH時mm分");//定義好時間字串的格式
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日  HH時mm分ss秒SSS毫秒");//定義好時間字串的格式

    // ====== 截止時間換算 ======
    public static Calendar string2calendar(String t) {
        Calendar cal = Calendar.getInstance(); // 取得目前時間
        try {
            Date dt = sdf.parse(t);                              //將字串轉成Date型
            cal.setTime(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }


    public static long sysstamp() {
        return System.currentTimeMillis(); // 取得目前系統時間
    }

    public static Calendar systime() {
        return stamp2calendar(sysstamp());
    }

    public static Calendar adjtime(Calendar cal) {
        cal.add(Calendar.MILLISECOND, (int) sync.UTC_DELTA); //調整誤差
        return cal;
    }

    public static Calendar nowtime() {
        Calendar cal = adjtime(systime()); // 取得校正時間
        cal.add(Calendar.MILLISECOND, (int) sync.UTC_DELTA); //調整誤差
        return cal;
    }

    public static CharSequence info() {
        StringBuilder sb = new StringBuilder();
        sb.append("ＳＹＳ：");
        sb.append(calendar2string(stamp2calendar(sync.UTC_SYS)));
        sb.append("\n");
        sb.append("ＮＴＰ：");
        sb.append(calendar2string(stamp2calendar(sync.UTC_NTP)));
        sb.append("\n");
        sb.append("ＥＲＲ：");
        sb.append(sync.UTC_DELTA + "ms");
        sb.append("\n");
        sb.append("\n");

        Calendar c = time.systime();
        sb.append("ＺＯＮＥ：");
        sb.append(c.getTimeZone().getID());
        sb.append("\n");
        sb.append("ＲＡＷ：");
        sb.append(calendar2string(c));
        sb.append("\n");
        sb.append("ＡＤＪ：");
        sb.append(calendar2string(time.adjtime(c)));
        sb.append("\n");

        return sb;
    }

    public static Calendar settime(int hh, int mm) {
        Calendar cal = nowtime(); // 取得目前時間

        cal.set(Calendar.HOUR_OF_DAY, hh);
        cal.set(Calendar.MINUTE, mm);
//        cal.add(Calendar.HOUR, hh);        //小時+hh
//        cal.add(Calendar.MINUTE, mm);      //分+mm
        return cal;
    }

    // ========時間換字串 ======
    public static String calendar2string(Calendar cal) {
        Date d = cal.getTime();
        String dateStr = sdf.format(d);
        return dateStr;
    }

    public static Calendar stamp2calendar(long stamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(stamp);
        return cal;
    }

    public static void utc(Context ctx) {

        StringRequest request = new StringRequest(
                "http://ntp-b1.nict.go.jp/cgi-bin/json",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //取得系統時間
                        sync.UTC_SYS = sysstamp();

                        //用gson將json轉物件
                        ntp = gson.fromJson(response, JSON_UTC.class);

                        //誤差計算
                        sync.UTC_NTP = (long) (ntp.st * 1000);
                        sync.UTC_DELTA = sync.UTC_NTP - sync.UTC_SYS;

//                        Log.d("NET", response);
//                        Log.d("NET", ntp.id);
//                        Log.d("NET", ntp.it);
//                        Log.d("NET", String.valueOf(ntp.st));
//                        Log.d("NET", ntp.leap);
//                        Log.d("NET", ntp.next);
//                        Log.d("NET", ntp.step);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

//                        Log.d("NET", error.toString());

                    }
                }
        );

        RequestQueue rq = Volley.newRequestQueue(ctx);
        rq.add(request);
        rq.start();

    }


}
