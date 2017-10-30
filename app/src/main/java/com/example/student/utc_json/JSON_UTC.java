package com.example.student.utc_json;

/**
 * Created by student on 2017/10/27.
 */

public class JSON_UTC {

    public String id;   //サーバＩＤ
    public String it;   //発信時刻（クライアントから送信された時刻）
    public double st;   //サーバ時刻
    public String leap; //next以前の時点での UTC と TAI の差（秒）
    public String next; //次、または最後のうるう秒イベント時刻
    public String step; //

}
