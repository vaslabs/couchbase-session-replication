package org.vaslabs.sr;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.SerializableDocument;
import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardSession;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by vnicolaou on 03/04/16.
 */
public class CouchbaseBackupSessionManager extends ManagerBase implements Lifecycle, Manager {

    private AsyncBucket sessionBucket;
    private static final String COUCHBASE_HOST = "192.168.101.101";
    private static final String COUCHBASE_BUCKET = "website-sessions";
    private static final String COUCHBASE_PASSWORD = "NotThePassword";

    @Override
    public void add(Session session) {
        super.add(session);
        _storeSession(session);
    }

    private void _storeSession(Session session) {
        String sessionId = session.getId();
        SerializableDocument serializableDocument =
                SerializableDocument.create(sessionId, (Serializable)session, session.getMaxInactiveInterval()*60L);
        sessionBucket.upsert(serializableDocument);
    }

    public void changeSessionId(Session session) {
        super.changeSessionId(session);
        _storeSession(session);
    }

    public Session createEmptySession() {
        Session session = super.createEmptySession();
        _storeSession(session);
        return session;
    }

    public Session createSession(String s) {
        Session session = super.createSession(s);
        _storeSession(session);
        return session;
    }

    public Session findSession(String s) throws IOException {
        Session session = super.findSession(s);
        if (session != null)
            return session;
        session = _fetchSession(s);
        return session;
    }

    private Session _fetchSession(String s) {
        SerializableDocument sd = sessionBucket.get(s, SerializableDocument.class).toBlocking().last();
        if (sd == null)
            return null;
        sessionBucket.upsert(sd);
        return (Session)sd.content();
    }

    public Session[] findSessions() {
        return super.findSessions();
    }

    public void load() throws ClassNotFoundException, IOException {

    }

    public void remove(Session session, boolean b) {
        super.remove(session, b);
        sessionBucket.remove(session.getId());
    }

    public void unload() throws IOException {
        sessionBucket.close();
    }

    @Override
    public void startInternal() throws LifecycleException {
        super.startInternal();
        sessionBucket = CouchbaseCluster.create(COUCHBASE_HOST)
                .openBucket(COUCHBASE_BUCKET, COUCHBASE_PASSWORD).async();
        setState(LifecycleState.STARTING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopInternal() throws LifecycleException {
        setState(LifecycleState.STOPPING);

        sessionBucket = CouchbaseCluster.create(COUCHBASE_HOST)
                .openBucket(COUCHBASE_BUCKET, COUCHBASE_PASSWORD).async();

        super.stopInternal();
    }
}
