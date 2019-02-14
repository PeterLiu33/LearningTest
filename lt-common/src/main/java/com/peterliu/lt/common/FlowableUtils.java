package com.peterliu.lt.common;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by liujun on 13/08/2018.
 */
public abstract class FlowableUtils {

    public static void main(String[] args) throws InterruptedException {
        Flowable.fromCallable(()->{
            Thread.sleep(1000);
            return "helloWrold!";
        }).subscribeOn(Schedulers.io())
               .observeOn(Schedulers.single())
                .subscribe(System.out::println, System.err::println);
        Thread.sleep(2000);

        Flowable.range(1, 10)
                .observeOn(Schedulers.computation())
                .map(v -> v * v)
                .blockingSubscribe(System.out::println);

        Flowable.range(1, 10)
                .parallel()
                .runOn(Schedulers.computation())
                .map(v -> v * v)
                .sequential()
                .blockingSubscribe(System.out::println);

        Flowable.range(1, 10)
                .flatMap(v ->
                        Flowable.just(v)
                                .subscribeOn(Schedulers.computation())
                                .map(w -> w * w)
                )
                .blockingSubscribe(System.out::println);
    }
}
