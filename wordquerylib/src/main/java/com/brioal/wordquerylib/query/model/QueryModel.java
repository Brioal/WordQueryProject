package com.brioal.wordquerylib.query.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brioal.wordquerylib.bean.WordBean;
import com.brioal.wordquerylib.interfaces.OnDataLoadListener;
import com.brioal.wordquerylib.query.contract.QueryContract;
import com.brioal.wordquerylib.utils.StringUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by brioa on 2018/3/7.
 */

public class QueryModel implements QueryContract.Model {
    private Context mContext;

    // 查询英文
    private final String URL_TRANS_EN = "http://www.youdao.com/w/eng/";
    // 查询英文句子
    private final String URL_TRANS_LONG = "http://www.iciba.com/index.php";


    public QueryModel(Context context) {
        mContext = context;
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    /**
     * 在线翻译
     *
     * @param word
     * @param loadListener
     */
    @Override
    public void transNet(final String word, final OnDataLoadListener<WordBean> loadListener) {
        boolean sentense = word.contains(" ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String finalUrl = URL_TRANS_EN + word;
                Request request = new Request.Builder()
                        .url(finalUrl)
                        .get()
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e1) {
                        e1.printStackTrace();
                        loadListener.failed(e1.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String content = response.body().string();
                            Document document = Jsoup.parse(content);
                            WordBean wordBean = getWordBean(word, content);
                            if (wordBean == null) {
                                loadListener.failed("没有数据");
                            } else {
                                loadListener.success(wordBean);
                            }
                            return;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            loadListener.failed(e2.getMessage());
                            return;
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 翻译长句子
     *
     * @param word
     * @param loadListener
     */
    @Override
    public void transLong(final String word, final OnDataLoadListener<WordBean> loadListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String finalUrl = "http://www.iciba.com/index.php?a=getWordMean&c=search&list=2&word="+word+"&_=1521590990331";
                Request request = new Request.Builder()
                        .url(finalUrl)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e1) {
                        e1.printStackTrace();
                        loadListener.failed(e1.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String content = response.body().string();
                            WordBean wordBean = getLongTrans(word, content);
                            // 保存到本地
                            if (wordBean == null) {
                                loadListener.failed("没有数据");
                            } else {
                                loadListener.success(wordBean);
                            }
                            return;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            loadListener.failed(e2.getMessage());
                            return;
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 返回长句翻译
     *
     * @param word
     * @param content
     * @return
     */
    private WordBean getLongTrans(String word, String content) {
        try {
            JSONObject object = new JSONObject(content);
            JSONObject baseInfo = object.getJSONObject("baesInfo");
            String trans = baseInfo.getString("translate_result");
            WordBean wordBean = new WordBean();
            wordBean.setKey(word)
                    .setTran(trans);
            return wordBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 翻译中文
     *
     * @param word
     * @param loadListener
     */
    @Override
    public void transCN(final String word, final OnDataLoadListener<WordBean> loadListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                String finalUrl = URL_TRANS_EN + word;
                Request request = new Request.Builder()
                        .url(finalUrl)
                        .get()
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e1) {
                        e1.printStackTrace();
                        loadListener.failed(e1.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            String content = response.body().string();
                            Document document = Jsoup.parse(content);
                            WordBean wordBean = getCnWordBean(word, content);
                            if (wordBean == null) {
                                loadListener.failed("没有数据");
                            } else {
                                loadListener.success(wordBean);
                            }
                            return;
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            loadListener.failed(e2.getMessage());
                            return;
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * 返回中文的翻译
     *
     * @param word
     * @param content
     * @return
     */
    private WordBean getCnWordBean(String word, String content) {
        WordBean bean = new WordBean();
        Document document = Jsoup.parse(content);
        bean.setKey(getWords(document))//解析单词名称
                .setTran(getCNTrans(document))
                .setCn(true);
        return bean;
    }

    /**
     * 返回单个单词
     *
     * @param word
     * @param content
     * @return
     */
    public WordBean getWordBean(String word, String content) {
        WordBean bean = new WordBean();
        Document document = Jsoup.parse(content);
        bean.setKey(getWords(document))//解析单词名称
                .setPron(getENPron(document))//返回英文拼写
                .setTran(getENTrans(document))//翻译
                .setPhrase(getENPhrases(document))//短语
                .setSimilar(getENSimliar(document))//近义词
                .setRoot(getENRoots(document))//同根词
                .setSentence(getENSentens(document))//例句
                .setCn(false);
        return bean;
    }

    @Override
    public void trans(String word, OnDataLoadListener<WordBean> loadListener) {
        int type = 0;
        Pattern pattern = Pattern.compile("[a-zA-Z]");
        boolean en = pattern.matcher(word).find();
        if (en) {
            type = 0;
            boolean sentense = word.contains(" ");
            if (sentense) {
                type = 2;
            } else {
                type = 0;
            }
        } else {
            type = 1;
        }
        // 根据类型加载单词
        WordBean bean = new WordBean();
        switch (type) {
            case 0:
                // 单词
                transNet(word, loadListener);
                break;
            case 1:
                // 中文
                transCN(word, loadListener);
                break;
            case 2:
                // 长句子
                transLong(word, loadListener);
                break;
        }
    }


    /**
     * 返回中文的翻译
     *
     * @param document
     * @return
     */
    private String getCNTrans(Document document) {
        StringBuffer buffer = new StringBuffer();
        Element transElement = document.getElementsByClass("trans-container").first();
        //获取所有的翻译类
        Elements words = transElement.getElementsByClass("contentTitle");
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i).text();
            word = word.replaceAll(";", "");
            word = word.trim();
            buffer.append(word + "   ");
            Logger.e("中文翻译:" + word);
        }
        return buffer.toString();
    }

    /**
     * 返回中文的翻译
     *
     * @param document
     * @return
     */
    private String getLongTrans(Document document) {
        StringBuffer buffer = new StringBuffer();
        Element transElement = document.getElementsByClass("trans-container").first();
        //获取所有的翻译类
        String word = transElement.getElementsByTag("p").get(1).text();
        return word;
    }


    /**
     * 返回双语例句
     *
     * @param document
     * @return
     */
    private String getENSentens(Document document) {
        StringBuffer buffer = new StringBuffer();
        try {
            Element element = document.getElementById("bilingual");
            Elements lis = element.getElementsByTag("li");
            for (int i = 0; i < lis.size(); i++) {
                Element singleLi = lis.get(i);
                String sentens = singleLi.getElementsByTag("p").first().text();
                String trans = singleLi.getElementsByTag("p").get(1).text();
                buffer.append(sentens);
                buffer.append("   ");
                buffer.append(trans);
                if (i < lis.size()) {
                    buffer.append("\n");
                }
            }
            Logger.e("例句:" + buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("返回双语例句出错");
        }
        return buffer.toString();
    }

    /**
     * 获取同根词
     *
     * @param document
     * @return
     */
    private String getENRoots(Document document) {
        StringBuffer buffer = new StringBuffer();
        try {
            Element element = document.getElementById("relWordTab");
            Elements ps = element.getElementsByTag("p");
            for (int i = 0; i < ps.size(); i++) {
                Element singleP = ps.get(i);
                String trans = singleP.ownText();
                String word = singleP.getElementsByTag("a").first().text();
                System.out.println("同根词" + word + "翻译" + trans);
                buffer.append(word);
                buffer.append("   ");
                buffer.append(trans);
                if (i < ps.size()) {
                    buffer.append("\n");
                }
            }
            Logger.e("同根词:" + buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("获取同根词出错");
        }
        return buffer.toString();
    }

    /**
     * 返回同义词
     *
     * @param document
     * @return
     */
    private String getENSimliar(Document document) {
        StringBuffer buffer = new StringBuffer();
        try {
            Element element = document.getElementById("synonyms");
            Elements lis = element.getElementsByTag("li");
            for (int i = 0; i < lis.size(); i++) {
                String trans = lis.get(i).text();
                Element p = element.getElementsByTag("p").get(i);
                Elements spans = p.getElementsByTag("span");
                for (int j = 0; j < spans.size(); j++) {
                    Element singleSpan = spans.get(j);
                    String word = singleSpan.getElementsByTag("a").text();
                    buffer.append(word);
                    buffer.append("   ");
                    buffer.append(trans);
                    if (i < lis.size() - 1) {
                        buffer.append("\n");
                    }
                }
            }
            Logger.e("同义词:" + buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    /**
     * 获取词组
     *
     * @param document
     * @return
     */
    private String getENPhrases(Document document) {
        StringBuffer buffer = new StringBuffer();
        try {
            Element element = document.getElementById("wordGroup2");
            Elements ps = element.getElementsByTag("p");
            for (int i = 0; i < ps.size(); i++) {
                Element singleP = ps.get(i);
                String trans = singleP.ownText();
                String word = singleP.getElementsByTag("a").first().text();
                buffer.append(word);
                buffer.append("   ");
                buffer.append(trans);
                if (i < ps.size() - 1) {
                    buffer.append("\n");
                }
            }
            Logger.e("词组:" + buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("获取词组出错");
        }
        return buffer.toString();
    }

    /**
     * 返回翻译
     *
     * @param document
     * @return
     */
    private String getENTrans(Document document) {
        String result = "";
        try {
            Element allTrans = document.getElementsByClass("trans-container").first();
            Elements elements = allTrans.getElementsByTag("li");
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < elements.size(); i++) {
                buffer.append(elements.get(i).text());
                if (i < elements.size() - 1) {
                    buffer.append("\n");
                }
            }
            result = buffer.toString();
            Logger.e("翻译内容:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
            Logger.e("翻译出错");
        }
        return result;
    }

    /**
     * 返回英文拼写
     *
     * @param document
     * @return
     */
    private String getENPron(Document document) {
        String result = "";
        try {
            Element element = document.getElementsByClass("phonetic").get(1);
            result = element.text();
            Logger.e("英文拼写:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            result = "";
            Logger.e("返回英文拼写出错");
        }
        return result;
    }

    /**
     * 解析单词名称
     *
     * @param document
     * @return
     */
    private String getWords(Document document) {
        String result = "";
        try {
            Element element = document.getElementsByClass("keyword").first();
            result = element.text();
            Logger.e("查询的单词:" + result);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return result;
    }
}
