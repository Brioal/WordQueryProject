package com.brioal.wordquerylib.query.presenter;

import android.os.Handler;

import com.brioal.wordquerylib.interfaces.OnDataLoadListener;
import com.brioal.wordquerylib.bean.WordBean;
import com.brioal.wordquerylib.query.contract.QueryContract;
import com.brioal.wordquerylib.query.model.QueryModel;

import java.util.List;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by brioa on 2018/3/7.
 */

public class QueryPresenter implements QueryContract.Presenter {
    private QueryContract.View mView;
    private QueryContract.Model mModel;
    private Handler mHandler = new Handler();

    public QueryPresenter(QueryContract.View view) {
        mView = view;
        mModel = new QueryModel(mView.getQueryContext());
    }

    @Override
    public void query() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mView.showLoading();
            }
        });
        mModel.trans(mView.getWord(), new OnDataLoadListener<WordBean>() {
            @Override
            public void success(final WordBean bean) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mView.showLoadDone(bean);
                    }
                });
            }

            @Override
            public void failed(String errorMsg) {
                mModel.transNet(mView.getWord(), new OnDataLoadListener<WordBean>() {
                    @Override
                    public void success(final WordBean bean) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.showLoadDone(bean);
                            }
                        });
                    }

                    @Override
                    public void failed(final String errorMsg) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mView.showLoadFailed(errorMsg);
                            }
                        });
                    }
                });
            }
        });
    }
}
