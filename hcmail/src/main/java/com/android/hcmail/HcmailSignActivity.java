package com.android.hcmail;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.TopBarView;
import com.android.hcframe.email.R;

/**
 * Created by zhujiabin on 2017/3/20.
 */

public class HcmailSignActivity extends HcBaseActivity implements View.OnClickListener {

    private TopBarView mSignTopBar;
    private EditText mEdit;
    private TextView mSignNameSaveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hcmail_sign_name_set);
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        mSignTopBar = (TopBarView) findViewById(R.id.hcmail_sign_name_bar);
        mEdit = (EditText) findViewById(R.id.hcmail_edit);
        mSignNameSaveBtn = (TextView) findViewById(R.id.hcmail_sign_name_save_btn);
    }

    private void initData() {
        mSignTopBar.setTitle("签名设置");
    }

    private void initEvent() {
        mSignNameSaveBtn.setOnClickListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.hcmail_sign_name_save_btn) {
            String edit = mEdit.getText().toString().trim();
            HcEmailSharedHelper.setSign(this, edit);
            finish();
        }
    }
}
