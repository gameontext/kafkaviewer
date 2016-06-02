
package org.ozzy.demo;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;

/**
 * App scoped bean that holds onto a map of context Paths to session manager
 * instances.. and will empty the map on scope desctruction.
 */
@ApplicationScoped
public class SessionManagerStore {

    private final ConcurrentMap<String, SessionManager> sessionManagers = new ConcurrentHashMap<String, SessionManager>();

    public SessionManagerStore() {
    }

    public SessionManager getSessionManager(String contextPath) {
        SessionManager sm = new SessionManager();
        SessionManager existing = sessionManagers.putIfAbsent(contextPath, sm);
        if (existing != null) {
            return existing;
        } else {
            return sm;
        }
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
        sessionManagers.clear();
    }

}
