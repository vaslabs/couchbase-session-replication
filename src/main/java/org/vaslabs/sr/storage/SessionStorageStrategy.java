package org.vaslabs.sr.storage;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.document.ByteArrayDocument;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import rx.Observable;
import com.esotericsoftware.kryo.Kryo;

import java.time.Duration;
import java.util.HashMap;

public interface SessionStorageStrategy<StoreFormat, ObjectClass>  {

    Observable<StoreFormat> store(String sessionId, ObjectClass session);

    Observable<ObjectClass> get(String sessionId);

}

class KryoSessionStorageStrategy<Session> implements SessionStorageStrategy<ByteArrayDocument, Session> {

    private final Kryo kryo = new Kryo();
    private final AsyncBucket sessionBucket;
    private final Duration expiry;
    private final Class<Session> clazz;
    private final int initialBufferSize;
    private final int maxBufferSize;

    KryoSessionStorageStrategy(AsyncBucket sessionBucket, Duration expiry, Class<Session> clazz) {
        this(sessionBucket, expiry, 1024, Integer.MAX_VALUE, clazz);
    }

    KryoSessionStorageStrategy
            (AsyncBucket sessionBucket, Duration expiry, int initialBufferSize, int maxBufferSize, Class<Session> clazz) {
        assert maxBufferSize >= initialBufferSize;
        this.sessionBucket = sessionBucket;
        this.expiry = expiry;
        this.initialBufferSize = initialBufferSize;
        this.maxBufferSize = maxBufferSize;
        this.clazz = clazz;
        kryo.register(HashMap.class, new MapSerializer());
    }

    @Override
    public Observable<ByteArrayDocument> store(String sessionId, Session session) {
        return Observable.fromCallable(
            () -> serialize(session)
        ).map(
              Output::getBuffer
        ).map(bytes ->
                ByteArrayDocument.create(
                    sessionId,
                    Math.toIntExact(expiry.getSeconds()), bytes)
        )
        .flatMap(
            sessionBucket::upsert
        );
    }


    private Output serialize(Session session) {
        final Output output = new Output(initialBufferSize, maxBufferSize);
        kryo.writeObject(output, session);
        return output;
    }

    @Override
    public Observable<Session> get(String sessionId) {
        return sessionBucket.get(sessionId, ByteArrayDocument.class)
            .map(
                ByteArrayDocument::content
            ).map(
                    this::deserializeSession
            );
    }

    private Session deserializeSession(byte[] data) {
        return kryo.readObject(new Input(data), clazz);
    }
}


