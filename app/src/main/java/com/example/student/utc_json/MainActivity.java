package com.example.student.utc_json;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Handler han = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        han.post(new runn());

    }

    class runn implements Runnable {
        @Override
        public void run() {

            han.postDelayed(this, 1000);

            time.utc(MainActivity.this);

            TextView tv = (TextView) findViewById(R.id.textViewShow);
            tv.setText(time.info());

        }
    }


}

