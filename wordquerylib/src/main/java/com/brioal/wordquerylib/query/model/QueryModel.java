package com.brioal.wordquerylib.query.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.brioal.wordquerylib.bean.WordBean;
import com.brioal.wordquerylib.db.QueryDBHelper;
import com.brioal.wordquerylib.interfaces.OnDataLoadListener;
import com.brioal.wordquerylib.query.contract.QueryContract;
import com.brioal.wordquerylib.utils.StringUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
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


    public QueryModel(Context context) {
        mContext = context;
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    /**
     * 查询本地的单词
     *
     * @param word
     * @param loadListener
     */
    @Override
    public void transLocal(final String word, final OnDataLoadListener<WordBean> loadListener) {
        if (loadListener == null) {
            return;
        }
        if (!StringUtil.isAvailable(word)) {
            loadListener.failed("数据错误");
            return;
        }
        Observer observer = new Observer<WordBean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(WordBean bean) {
                Logger.e("在本地查询到单词");
                loadListener.success(bean);
            }

            @Override
            public void onError(Throwable e) {
                Logger.e("本地没有该单词");
                e.printStackTrace();
                loadListener.failed(e.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
        Observable.create(new ObservableOnSubscribe<WordBean>() {
            @Override
            public void subscribe(ObservableEmitter<WordBean> emitter) throws Exception {
                SQLiteDatabase database = QueryDBHelper.getDB(mContext);
                // 判断单词是否存在
                String sql = "select count from word where key = ?";
                int count = 0;
                Cursor cursor = database.rawQuery(sql, new String[]{
                        word
                });
                while (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
                if (count == 0) {
                    // 不存在
                    emitter.onError(new Exception("单词不存在"));
                    return;
                }
                // 单词存在
                // 添加查询的次数
                count += 1;
                String countQuery = "update word set count = ? where key = ?";
                database.execSQL(countQuery, new Object[]{
                        count,
                        word
                });
                // 查询数据
                String querySql = "select * from word where key = ?";
                Cursor queryCursor = database.rawQuery(querySql, new String[]{
                        word
                });
                while (queryCursor.moveToNext()) {
                    WordBean wordBean = convertFromCursor(queryCursor);
                    emitter.onNext(wordBean);
                    return;
                }
            }
        }).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    /**
     * 单词转换
     *
     * @param cursor
     * @return
     */
    private WordBean convertFromCursor(Cursor cursor) {
        // 获取单词
        String key = cursor.getString(0);
        // 获取翻译字符串
        String tran = cursor.getString(1);
        // 获取读音
        String pron = cursor.getString(2);
        // 获取短语
        String phrase = cursor.getString(3);
        // 获取近义词
        String similar = cursor.getString(4);
        // 获取同根词
        String root = cursor.getString(5);
        // 获取例句
        String sentence = cursor.getString(6);
        // 获取时间
        long time = cursor.getLong(7);
        // 获取次数
        int queryCount = cursor.getInt(8);
        // 是否是中文
        boolean cn = cursor.getInt(9) == 1;

        //根据是否是中文设置参数
        WordBean wordBean = new WordBean();
        if (cn) {
            // 中文
            wordBean.setKey(key);
            wordBean.setTran(tran);
            wordBean.setTime(time);
            wordBean.setCount(queryCount);
            wordBean.setCn(cn);
        } else {
            // 英文
            wordBean.setKey(key);
            wordBean.setTran(tran);
            wordBean.setPron(pron);
            wordBean.setPhrase(phrase);
            wordBean.setSimilar(similar);
            wordBean.setRoot(root);
            wordBean.setSentence(sentence);
            wordBean.setTime(time);
            wordBean.setCount(queryCount);
            wordBean.setCn(cn);
        }
        return wordBean;
    }

    /**
     * 在线翻译
     *
     * @param word
     * @param loadListener
     */
    @Override
    public void transNet(final String word, final OnDataLoadListener<WordBean> loadListener) {
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
                            final WordBean bean = new WordBean();
                            //判断是中文还是英文
                            boolean en = word.matches("[a-zA-Z]+");
                            if (en) {
                                //英文
                                bean.setKey(getWords(document))//解析单词名称
                                        .setPron(getENPron(document))//返回英文拼写
                                        .setTran(getENTrans(document))//翻译
                                        .setPhrase(getENPhrases(document))//短语
                                        .setSimilar(getENSimliar(document))//近义词
                                        .setRoot(getENRoots(document))//同根词
                                        .setSentence(getENSentens(document))//例句
                                        .setCn(false)
                                ;
                            } else {
                                //中文
                                bean.setKey(getWords(document))//解析单词名称
                                        .setTran(getCNTrans(document))
                                        .setCn(true)
                                ;
                            }
                            // 保存到本地
                            saveToLocal(bean);
                            loadListener.success(bean);
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
     * 将单词保存到本地
     *
     * @param wordBean
     */
    @Override
    public void saveToLocal(final WordBean wordBean) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (wordBean == null) {
                    return;
                }
                SQLiteDatabase database = QueryDBHelper.getDB(mContext);
                //查询是否存在单词
                String sql_query = "select count from word where key = ?";
                Cursor cursor = database.rawQuery(sql_query, new String[]{
                        wordBean.getKey()
                });
                int count = 0;
                while (cursor.moveToNext()) {
                    count = cursor.getInt(0);
                }
                Logger.e("单词:" + wordBean.getKey() + "的次数为:" + count);
                if (count == 0) {
                    try {
                        String sql = "insert into word  values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )";
                        database.execSQL(sql, new Object[]{
                                wordBean.getKey(),
                                wordBean.getTran(),
                                wordBean.getPron(),
                                wordBean.getPhrase(),
                                wordBean.getSimilar(),
                                wordBean.getRoot(),
                                wordBean.getSentence(),
                                System.currentTimeMillis(),
                                1,
                                !wordBean.getKey().matches("[a-zA-Z]+")

                        });
                        Logger.e("保存单词" + wordBean.getKey() + "到数据库成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("保存单词" + wordBean.getKey() + "到数据库失败");
                    }
                } else {
                    //存在,增加次数
                    try {
                        String sql = "update word set count = ? where key = ?";
                        database.execSQL(sql, new Object[]{
                                count + 1,
                                wordBean.getKey()
                        });
                        Logger.e("增加单词:" + wordBean.getKey() + "的查询次数成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                        Logger.e("增加单词:" + wordBean.getKey() + "的查询次数失败");
                    }

                }

            }
        }).start();
    }

    /**
     * 查询本地的单词列表
     *
     * @param type
     * @param index
     * @param loadListener
     */
    @Override
    public void queryRecord(final int type, final int index, final OnDataLoadListener<List<WordBean>> loadListener) {
        if (loadListener == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = QueryDBHelper.getDB(mContext);
                String sortStr = "";
                switch (type) {
                    case 0:
                        // 默认按时间顺序降序
                        sortStr = " order by -time";
                        break;
                    case 1:
                        // 按时间升序
                        sortStr = " order by time";
                        break;
                    case 2:
                        //查询次数降序
                        sortStr = " order by -count";
                        break;
                    case 3:
                        //查询次数升序
                        sortStr = " order by count";
                        break;
                    case 4:
                        //首字母
                        sortStr = " order by key";
                        break;
                    default:
                        sortStr = " order by -time";
                }
                String sql = "select * from word" + sortStr ;
                Cursor cursor = database.rawQuery(sql, null);
                List<WordBean> result = new ArrayList<>();
                while (cursor.moveToNext()) {
                    WordBean wordBean = convertFromCursor(cursor);
                    result.add(wordBean);
                }
                loadListener.success(result);
                return;
            }
        }).start();
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
