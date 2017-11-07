package com.example.student.utc_json;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Felix on 2017/11/6.
 */

public abstract class NTP_JSON {

    public abstract void onResult(RESULT result);

    public abstract void onResultFinish(List<RESULT> results);

    private final static String[] server = {
            //http://www.nict.go.jp/JST/http.html
            "https://ntp-a1.nict.go.jp/cgi-bin/json",
            "http://ntp-a1.nict.go.jp/cgi-bin/json",
            "https://ntp-b1.nict.go.jp/cgi-bin/json",
            "http://ntp-b1.nict.go.jp/cgi-bin/json"
    };

    public final class PACKAGE {
        public String id;
        public String it;
        public double st;
        public String leap;
        public String next;
        public String step;
//    id: "pkg-a1.nict.go.jp" サーバＩＤ
//    it: 1232963971.248 発信時刻（クライアントから送信された時刻）
//    st: 1232963971.920 サーバ時刻
//    leap: 33 next 以前の時点での UTC と TAI の差（秒）
//    next: 1230768000 次、または最後のうるう秒イベント時刻
//    step: 1 次、または最後のうるう秒イベントが挿入の場合 1、削除の場合 -1
    }

    public final class RESULT {
        private String host;
        private PACKAGE pkg;
        private long sentTime;
        private long recvTime;

        public RESULT(String host, long sentTime) {
            this.host = host;
            this.sentTime = sentTime;
        }

        public RESULT(String host, PACKAGE pkg, long sentTime, long recvTime) {
            this.host = host;
            this.pkg = pkg;
            this.sentTime = sentTime;
            this.recvTime = recvTime;
        }

        public String getHost() {
            return host;
        }

        public double responseTime() {
            if (recvTime <= sentTime) {
                return 0;
            }
            return recvTime - sentTime;
        }

        public double offsetTime() {
            if (pkg == null) {
                return 0;
            }
            return pkg.st - (recvTime / 1000.0);
        }
    }

    public final static String KEY_OFF = "offset";
    public final static String KEY_RSP = "response";
    public final static String KEY_TOL = "total";
    private Gson gson = new Gson();
    private List<RESULT> results;
    private long sentTime;

    public NTP_JSON(Context ctx) {

        results = new ArrayList<>();

        final RequestQueue rq = Volley.newRequestQueue(ctx);
        rq.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {

                if (results.size() >= server.length) {
                    //完成所有項目
                    onResultFinish(results);
                } else {
                    //開始剩餘項目
                    addRequest(rq);
                }

            }
        });

        rq.start();

        //開始第一個項目
        addRequest(rq);


    }

    private void addRequest(RequestQueue rq) {
        String host = server[results.size()];
        StringRequest req = newRequest(host);
        //            req.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_TIMEOUT_MS + 10000, DEFAULT_MAX_RETRIES, DEFAULT_BACKOFF_MULT));
        sentTime = System.currentTimeMillis();
        rq.add(req);
    }

    private StringRequest newRequest(final String host) {
        return new StringRequest(
                host,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //取得系統時間
                        long recvTime = System.currentTimeMillis();

                        //用gson將json轉物件
                        RESULT result = new RESULT(host, gson.fromJson(response, PACKAGE.class), sentTime, recvTime);

                        //完成一個項目
                        onResult(result);
                        results.add(result);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        RESULT result = new RESULT(host, null, 0, 0);
                        onResult(result);
                        results.add(result);
//                        Log.d("NET", error.toString());

                    }
                }
        );
    }

    public Map<String, Double> averageNTP(List<RESULT> lr) {

        double offsetAvg = 0;
        long responseAvg = 0;

        //過濾資訊
        for (int i = 0; i < lr.size(); ) {
            RESULT r = lr.get(i);
            if (r.pkg == null) {
                //削除未成功項目
                lr.remove(i);
            } else {
                //成功的項目加總
                offsetAvg += r.offsetTime();
                responseAvg += r.responseTime();
                i++;
            }
        }

        //成功的項目平均
        final int siz = lr.size();
        if (siz > 0) {
            offsetAvg /= siz;
            responseAvg /= siz;
        } else {
            offsetAvg = 0;
            responseAvg = 0;
        }

        //回傳平均結果
        final double finalOffsetAvg = offsetAvg;
        final long finalResponseAvg = responseAvg;
        return new HashMap<String, Double>() {
            {
                put(KEY_OFF, finalOffsetAvg);
                put(KEY_RSP, (double) finalResponseAvg);
                put(KEY_TOL, (double) siz);
            }
        };

    }

}
