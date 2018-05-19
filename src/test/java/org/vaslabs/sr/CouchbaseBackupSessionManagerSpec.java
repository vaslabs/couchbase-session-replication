package org.vaslabs.sr;

import com.couchbase.client.java.document.ByteArrayDocument;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.vaslabs.sr.storage.CouchbaseServerBase;
import org.vaslabs.sr.storage.Storage;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.Assert.assertEquals;

public class CouchbaseBackupSessionManagerSpec extends CouchbaseServerBase {


    private static CouchbaseBackupSessionManager sessionManager;
    private Context container;

    @BeforeClass
    public static void init() throws LifecycleException {
        sessionManager = new CouchbaseBackupSessionManager();
        sessionManager.setCouchbaseBucket(bucketName);
        sessionManager.setCouchbaseHost(host);
        sessionManager.setCouchbasePassword(password);
        sessionManager.setSessionIdleTime(15);
        sessionManager.withSessionStorageStrategy(Storage.kryoBasedStrategy(
                bucket, Duration.ofSeconds(15), HashMap.class
        ));
    }

    @Before
    public void setUp() {
        container = Mockito.mock(Context.class);
        sessionManager.setContainer(container);
        Mockito.when(container.getDistributable()).thenReturn(false);
    }

    @Test
    public void couchbaseManagerStoresSessions() throws IllegalAccessException {
        StandardSession session = new StandardSession(sessionManager);
        session.setValid(true);
        final String sessionId = "custom_id-" + Math.random();
        session.setId(sessionId, false);
        Map<String, Object> attributes =
                (Map<String, Object>) CouchbaseBackupSessionManager.sessionDataField.get(session);
        attributes.put("my_attribute", ZonedDateTime.now(TestClock));
        sessionManager.add(session);
        StandardSession recoveredSession =
                (StandardSession) sessionManager._fetchSession(sessionId);
        assertEquals(ZonedDateTime.now(TestClock),
                recoveredSession.getAttribute("my_attribute"));
    }
}
