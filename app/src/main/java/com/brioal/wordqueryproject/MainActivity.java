package com.brioal.wordqueryproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    private Button mBtnList;
    private Spinner mSpinner;

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
        //单词列表
        mBtnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.wordList();
            }
        });
        //排序方式
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSortType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initIDs() {
        mEtKey = findViewById(R.id.main_et_key);
        mBtnQuery = findViewById(R.id.btn_query);
        mBtnList = findViewById(R.id.btn_list);
        mSpinner = findViewById(R.id.main_spinner);
    }

    @Override
    public void showLoading() {
        Logger.e("加载中........");
    }

    @Override
    public void showLoadDone(WordBean bean) {
        Logger.e("单词查询完成........");
        bean.log();
    }

    @Override
    public void showLoadFailed(String errorMsg) {
        Logger.e("单词查询失败........");
    }

    @Override
    public void showRecord(List<WordBean> list) {
        Logger.e("单词列表大小:" + list.size());
        for (int i = 0; i < list.size(); i++) {
            WordBean bean = list.get(i);
            System.out.println(bean.getKey());
        }
    }

    @Override
    public void showNextPageRecord(List<WordBean> list) {
        Logger.e("下一页单词列表大小:" + list.size());
    }

    @Override
    public String getWord() {
        return mKey;
    }

    @Override
    public Context getQueryContext() {
        return MainActivity.this;
    }

    @Override
    public int getSort() {
        return mSortType;
    }

    @Override
    public int getIndex() {
        return 0;
    }
}
