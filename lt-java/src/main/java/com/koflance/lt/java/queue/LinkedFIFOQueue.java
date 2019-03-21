package com.koflance.lt.java.queue;

import java.util.LinkedList;

/**
 * Created by liujun on 2018/2/2.
 */
public class LinkedFIFOQueue<T>{

    private LinkedList<T> queue;

    public LinkedFIFOQueue(){
        this.queue = new LinkedList<T>();
    }

    public synchronized void push(T t){
        this.queue.add(0, t);
    }

    public synchronized T pop(){
        if(queue.isEmpty()){
            return null;
        }
        return queue.removeFirst();
    }

    public int size(){
        return queue.size();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
