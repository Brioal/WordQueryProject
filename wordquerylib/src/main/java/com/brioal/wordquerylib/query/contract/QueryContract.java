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
         * 查询本地的单词
         *
         * @param word
         * @param loadListener
         */
        void transLocal(String word, OnDataLoadListener<WordBean> loadListener);

        /**
         * 翻译网络
         *
         * @param word
         * @param loadListener
         */
        void transNet(String word, OnDataLoadListener<WordBean> loadListener);

        /**
         * 保存结果到本地
         *
         * @param wordBean
         */
        void saveToLocal(WordBean wordBean);

        /**
         * 查询本地单词
         *
         * @param type
         * @param index
         */
        void queryRecord(int type, int index, OnDataLoadListener<List<WordBean>> loadListener);


    }

    interface View {
        void showLoading();//显示正在加载

        void showLoadDone(WordBean bean);//显示加载完成

        void showLoadFailed(String errorMsg);//显示加载失败

        /**
         * 显示本地单词记录
         *
         * @param list
         */
        void showRecord(List<WordBean> list);

        /**
         * 显示下一页的单词
         *
         * @param list
         */
        void showNextPageRecord(List<WordBean> list);

        String getWord();//返回搜索关键字


        Context getQueryContext();

        /**
         * 返回单词记录的排序发过誓
         *
         * @return
         */
        int getSort();

        /**
         * 返回单词记录的下标
         *
         * @return
         */
        int getIndex();
    }


    interface Presenter {
        void query();//查询单词

        /**
         * 获取本地单词列表
         */
        void wordList();

        /**
         * 下一页列表
         */
        void wordListNext();
    }
}
