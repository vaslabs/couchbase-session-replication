package org.vaslabs.sr.storage;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.ByteArrayDocument;
import org.junit.BeforeClass;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

public class CouchbaseServerBase {
    public static final Clock TestClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

    public static final String host = "localhost";
    public static final String bucketName = "sessions";
    public static final String password = "Session5";
    public static final AsyncBucket bucket = CouchbaseCluster.create(host)
            .openBucket(bucketName, password).async();
}
