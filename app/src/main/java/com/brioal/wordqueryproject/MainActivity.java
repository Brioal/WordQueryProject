package com.brioal.wordqueryproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brioal.wordquerylib.bean.WordBean;
import com.brioal.wordquerylib.query.contract.QueryContract;
import com.brioal.wordquerylib.query.presenter.QueryPresenter;
import com.brioal.wordquerylib.utils.StringUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.List;

public class MainActivity extends AppCompatActivity implements QueryContract.View {
    private EditText mEtKey;
    private Button mBtnQuery;
    private TextView mTvKey;
    private TextView mTvTrans;

    private QueryContract.Presenter mPresenter;

    private String mKey;
    private int mSortType = 0;//排序方式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.addLogAdapter(new AndroidLogAdapter());
        initIDs();
        initActions();
        initPresenter();
    }

    private void initPresenter() {
        mPresenter = new QueryPresenter(this);
    }

    private void initActions() {
        // 查询单词
        mBtnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKey = mEtKey.getText().toString().trim();
                mPresenter.query();
            }
        });
    }

    private void initIDs() {
        mEtKey = findViewById(R.id.main_et_key);
        mBtnQuery = findViewById(R.id.btn_query);
        mTvKey = findViewById(R.id.main_tv_key);
        mTvTrans = findViewById(R.id.main_tv_trans);
    }

    @Override
    public void showLoading() {
    }

    @Override
    public void showLoadDone(WordBean bean) {
        if (bean == null) {
            return;
        }
        mTvKey.setText(bean.getKey());
        mTvTrans.setText(bean.getTran());
    }

    @Override
    public void showLoadFailed(String errorMsg) {
        Logger.e("单词查询失败........");
    }

    @Override
    public String getWord() {
        return mKey;
    }

    @Override
    public Context getQueryContext() {
        return MainActivity.this;
    }

}
