package com.android.hcframe;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.view.Window;
import android.view.WindowManager;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-13 14:28.
 */

public class LoadResActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super .onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN );
        overridePendingTransition(R.anim.null_anim, R.anim.null_anim);
        setContentView(R.layout.activity_load);
        new LoadDexTask().execute();
    }
    class LoadDexTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                MultiDex.install(getApplication());
                HcLog.D("$LoadDexTask #doInBackground install finish" );
                ((HcApplication) getApplication()).installFinish(getApplication());
            } catch (Exception e) {
                HcLog.D("$LoadDexTask #doInBackground e = " + e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Object o) {
            HcLog.D("$LoadDexTask #onPostExecute get install finish");
            finish();
            System.exit( 0);
        }
    }
    @Override
    public void onBackPressed() {
        //cannot backpress
    }
}
