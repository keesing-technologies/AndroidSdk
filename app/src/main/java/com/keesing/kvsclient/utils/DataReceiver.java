package com.keesing.kvsclient.utils;

public interface DataReceiver<T> {
    void run(T... params);
}
