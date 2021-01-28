package com.example.db.callback;

public interface ProcessRunnable<T, P> {
    T process(P p);
}
