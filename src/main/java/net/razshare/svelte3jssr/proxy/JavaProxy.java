package net.razshare.svelte3jssr.proxy;

import org.graalvm.polyglot.Value;

public class JavaProxy {
    public static void thread(Value callback){
        new Thread(()->{
            callback.execute(Thread.currentThread());
        }).start();
    }
}
