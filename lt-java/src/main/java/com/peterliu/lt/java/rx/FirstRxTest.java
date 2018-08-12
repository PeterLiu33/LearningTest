package com.peterliu.lt.java.rx;

import com.peterliu.lt.common.AssistTools;
import com.peterliu.lt.common.LocalLoggerFactory;
import com.peterliu.lt.common.Logger;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import java.io.IOException;

/**
 * Created by liujun on 12/08/2018.
 */
public class FirstRxTest {

    private static Logger logger = LocalLoggerFactory.getLogger(FirstRxTest.class);

    public static void main(String[] args) throws IOException {
        Flowable.just("Hello World!").subscribe(System.out::println);
        // no backpressure
        Observable.create(emitter -> {
            while (!emitter.isDisposed()) {
                long time = System.currentTimeMillis();
                emitter.onNext(time);
                if (time % 2 != 0) {
                    emitter.onError(new IllegalStateException("old time"));
                    break;
                }
            }
        })
                .subscribe(System.out::println, logger::error);
        // This allows you to defer the execution of the function you specify until a Subscriber subscribes to the Publisher.
        // That is to say, it makes the function "lazy."
        Flowable.fromCallable(() -> {
            AssistTools.sleep(100);
            return "done";
        })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(System.out::println, logger::error);


        Flowable.range(1, 10).observeOn(Schedulers.computation()).map(v -> v * v).blockingSubscribe(logger::info);
        Flowable.range(1, 10).flatMap(v -> Flowable.just(v).subscribeOn(Schedulers.computation()).map(w -> w * w)).blockingSubscribe(logger::info);

    }
}
