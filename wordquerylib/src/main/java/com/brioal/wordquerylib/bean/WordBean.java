package com.brioal.wordquerylib.bean;


import com.brioal.wordquerylib.utils.StringUtil;
import com.orhanobut.logger.Logger;

import java.io.Serializable;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by Brioal on 2017/9/22.
 */

public class WordBean implements Serializable {
    // 单词
    private String key;
    // 解释
    private String tran;
    // 发音
    private String pron;
    // 短语
    private String phrase;
    // 同义词
    private String similar;
    // 同根词
    private String root;
    // 例句
    private String sentence;
    // 时间
    private long time;
    // 次数
    private int count;
    // 是否是中文
    private boolean cn;

    /**
     * 获取发音的url
     *
     * @return
     */
    public String getPronUrl() {
        if (!StringUtil.isAvailable(pron)) {
            return null;
        }
        if (!StringUtil.isAvailable(key)) {
            return null;
        }
        return "https://dict.youdao.com/dictvoice?audio=" + key + "&type=2";

    }

    public String getSimilar() {
        return similar;
    }

    public boolean isCn() {
        return cn;
    }

    public WordBean setCn(boolean cn) {
        this.cn = cn;
        return this;
    }

    public String getKey() {
        return key;
    }

    public WordBean setKey(String key) {
        this.key = key;
        return this;
    }

    public String getTran() {
        return tran;
    }

    public WordBean setTran(String tran) {
        this.tran = tran;
        return this;
    }

    public String getPron() {
        return pron;
    }

    public WordBean setPron(String pron) {
        this.pron = pron;
        return this;
    }

    public String getPhrase() {
        return phrase;
    }

    public WordBean setPhrase(String phrase) {
        this.phrase = phrase;
        return this;
    }


    public WordBean setSimilar(String similar) {
        this.similar = similar;
        return this;
    }

    public String getRoot() {
        return root;
    }

    public WordBean setRoot(String root) {
        this.root = root;
        return this;
    }

    public String getSentence() {
        return sentence;
    }

    public WordBean setSentence(String sentence) {
        this.sentence = sentence;
        return this;
    }

    public long getTime() {
        return time;
    }

    public WordBean setTime(long time) {
        this.time = time;
        return this;
    }

    public int getCount() {
        return count;
    }

    public WordBean setCount(int count) {
        this.count = count;
        return this;
    }

    /**
     * 打印当前的参数
     cn;
     */
    public void log() {
        Logger.e("----------------------------------");
        Logger.e("单词:"+key);
        Logger.e("翻译:"+tran);
        Logger.e("音节:"+pron);
        Logger.e("发音:"+getPronUrl());
        Logger.e("词组:"+phrase);
        Logger.e("近义词:"+similar);
        Logger.e("同根词:"+root);
        Logger.e("例句:"+sentence);
        Logger.e("时间:"+time);
        Logger.e("次数:"+count);
        Logger.e("中文?:"+cn);
        Logger.e("----------------------------------");
    }
}
