package com.brioal.wordquerylib.interfaces;

/**
 * email:brioal@foxmail.com
 * github:https://github.com/Brioal
 * Created by Brioal on 2017/8/5.
 */

public interface OnDataLoadListener<T> {
    void success(T t);

    void failed(String errorMsg);
}
