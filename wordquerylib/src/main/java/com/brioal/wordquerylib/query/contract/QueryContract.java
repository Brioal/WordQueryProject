package com.brioal.wordquerylib.query.contract;

import android.content.Context;

import com.brioal.wordquerylib.bean.WordBean;
import com.brioal.wordquerylib.interfaces.OnDataLoadListener;

import java.util.List;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by brioa on 2018/3/7.
 */

public interface QueryContract {
    interface Model {

        /**
         * 翻译网络
         *
         * @param word
         * @param loadListener
         */
        void transNet(String word, OnDataLoadListener<WordBean> loadListener);

        /**
         * 翻译长句子
         *
         * @param word
         * @param loadListener
         */
        void transLong(String word, OnDataLoadListener<WordBean> loadListener);

        /**
         * 翻译中文
         *
         * @param word
         * @param loadListener
         */
        void transCN(String word, OnDataLoadListener<WordBean> loadListener);

        /**
         * 翻译
         *
         * @param word
         * @param loadListener
         */
        void trans(String word, OnDataLoadListener<WordBean> loadListener);


    }

    interface View {
        void showLoading();//显示正在加载

        void showLoadDone(WordBean bean);//显示加载完成

        void showLoadFailed(String errorMsg);//显示加载失败

        String getWord();//返回搜索关键字

        Context getQueryContext();
    }


    interface Presenter {
        // 查询单词
        void query();
    }
}
