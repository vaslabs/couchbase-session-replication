package org.vaslabs.sr;

import com.couchbase.client.java.AsyncBucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.ByteArrayDocument;
import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardSession;
import org.vaslabs.sr.storage.SessionStorageStrategy;
import org.vaslabs.sr.storage.Storage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by vnicolaou on 03/04/16.
 */
public class CouchbaseBackupSessionManager extends ManagerBase implements Lifecycle, Manager {

    private AsyncBucket sessionBucket;
    private String couchbaseHost;
    private String couchbaseBucket;
    private String couchbasePassword;
    private Duration sessionIdleTime = Duration.ofMinutes(15);
    private SessionStorageStrategy<ByteArrayDocument, HashMap<String, Object>> storageStrategy;


    public void setCouchbaseHost(String host) {
        couchbaseHost = host;
    }

    public void setCouchbaseBucket(String bucket) {
        couchbaseBucket = bucket;
    }

    public void setCouchbasePassword(String password) {
        couchbasePassword = password;
    }

    public void setSessionIdleTime(int idleTimeInSeconds) {
        this.sessionIdleTime = Duration.ofSeconds(idleTimeInSeconds);
    }

    protected CouchbaseBackupSessionManager withSessionStorageStrategy(
            SessionStorageStrategy<ByteArrayDocument, HashMap<String, Object>> strategy) {
        this.storageStrategy = strategy;
        return this;
    }

    @Override
    public void add(Session session) {
        super.add(session);
        _storeSession(session);
    }

    final static Field sessionDataField =  getAttributesField();


    private static Field getAttributesField() {
        Field attr = null;
        try {
            attr = StandardSession.class.getDeclaredField("attributes");
            attr.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new Error("Can't proceed, unrecognised session api. Upgraded tomcat?");
        }
        return attr;
    }

    private void _storeSession(Session session) {
        System.out.println("Store session");
        try {
            HashMap<String, Object> attributes = new HashMap((Map<String, Object>) sessionDataField.get(session));
            attributes.put("timestamp", ZonedDateTime.now(Clock.systemUTC()));
            final String sessionId = session.getIdInternal();
            if (sessionId == null)
                return;
            if (session instanceof StandardSession)
                storageStrategy.store(sessionId, attributes).toBlocking().last();

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

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
        System.out.println("Finding session: " + s);
        Session session = super.findSession(s);
        if (session != null)
            return session;
        session = _fetchSession(s);
        System.out.println("Found session: " + session);
        return session;
    }

    protected Session _fetchSession(String sessionId) {
        return storageStrategy.get(sessionId).map(
                data -> {
                    Session session = new StandardSession(this);
                    session.setValid(true);
                    session.setId(sessionId, false);
                    System.out.println(data);
                    data.forEach(((StandardSession) session)::setAttribute);
                    return session;
                }
        ).toBlocking().last();
    }

    public Session[] findSessions() {
        return super.findSessions();
    }

    public void load() {

    }

    public void remove(Session session, boolean b) {
        final String id;
        if ((id=session.getIdInternal()) != null)
            sessionBucket.remove(id).toBlocking().last();
        super.remove(session, b);
    }

    public void unload() {
        sessionBucket.close();
   }

    @Override
    public void startInternal() throws LifecycleException {
        System.out.println(String.format("startInternal with %s %s:%s",
                couchbaseHost, couchbaseBucket, couchbasePassword));
        super.startInternal();
        sessionBucket = CouchbaseCluster.create(couchbaseHost)
                .openBucket(couchbaseBucket, couchbasePassword).async();
        setState(LifecycleState.STARTING);
        storageStrategy = Storage.kryoBasedStrategy(sessionBucket, sessionIdleTime, HashMap.class);
        System.out.println("Starting (internal finished)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopInternal() throws LifecycleException {
        super.stopInternal();
        sessionBucket.environment().shutdown(10, TimeUnit.SECONDS);
        setState(LifecycleState.STOPPING);
    }

}
