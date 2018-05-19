package org.vaslabs.sr.storage;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.ByteArrayDocument;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.*;

import static org.junit.Assert.assertEquals;

public class StorageSpec extends CouchbaseServerBase {

    private SessionStorageStrategy<ByteArrayDocument, MockSession> storageStrategy;

    @Test
    public void storageSpec() {
        storageStrategy = Storage.kryoBasedStrategy(bucket, Duration.ofSeconds(15), MockSession.class);
        MockSession mockSession = new MockSession();
        storageStrategy.store("my_id", mockSession).toBlocking().last();
        MockSession recoveredSession = storageStrategy.get("my_id").toBlocking().last();
        assertEquals(mockSession.someData, recoveredSession.someData);
        assertEquals(mockSession.time, recoveredSession.time);
    }


    private static class MockSession {
        private final String someData = "some data";
        private final ZonedDateTime time = ZonedDateTime.now(TestClock);
    }

}
