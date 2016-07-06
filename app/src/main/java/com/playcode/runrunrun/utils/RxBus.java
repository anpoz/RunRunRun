package com.playcode.runrunrun.utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * Created by anpoz on 2016/5/10.
 */
public class RxBus {
    private static volatile RxBus instance;
    private final SerializedSubject<Object, Object> mSubject;

    private RxBus() {
        mSubject = new SerializedSubject<>(PublishSubject.create());
    }

    public static RxBus getInstance() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    instance = new RxBus();
                }
            }
        }
        return instance;
    }

    public void post(Object object) {
        mSubject.onNext(object);
    }

    public <T> Observable<T> toObserable(final Class<T> type) {
        return mSubject.ofType(type);
    }

    public boolean hasObserables() {
        return mSubject.hasObservers();
    }
}
