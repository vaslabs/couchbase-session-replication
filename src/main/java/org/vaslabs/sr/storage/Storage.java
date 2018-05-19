package org.vaslabs.sr.storage;

import com.couchbase.client.java.AsyncBucket;

import java.time.Duration;

public class Storage {
    private Storage() {
        throw new AssertionError("NOPE!!!");
    }

    public static <Session> SessionStorageStrategy kryoBasedStrategy(AsyncBucket bucket, Duration sessionTimeToLive, Class<Session> clazz) {
        return new KryoSessionStorageStrategy(bucket, sessionTimeToLive, clazz);
    }
}
