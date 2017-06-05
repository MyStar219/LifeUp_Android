package ru.johnlife.lifetools.util;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * Created by Terry on 12/7/16.
 */
public class RxBus {

    private static volatile RxBus defaultInstance;

    private final rx.subjects.Subject<Object,Object> bus;

    public RxBus() {
        bus = new SerializedSubject<>(PublishSubject.create());
    }

    public static RxBus getDefaultInstance() {
        RxBus rxBus = defaultInstance;
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                rxBus = defaultInstance;
                if (defaultInstance == null){
                    rxBus = new RxBus();
                    defaultInstance = rxBus;
                }
            }
        }
        return rxBus;
    }

    public void post(Object o) {
        bus.onNext(o);
    }


    public <T> Observable<T> toObserverable (Class<T> eventType) {
        return bus.ofType(eventType);
    }

}
