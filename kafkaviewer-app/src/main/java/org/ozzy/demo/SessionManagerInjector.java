/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.ozzy.demo;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

public class SessionManagerInjector {

    @Inject
    SessionManagerStore managers;

    /**
     * CDI producer that returns a session manager for each web socket endpoint,
     * using the websocket Endpoint annotation to obtain the context path.
     */
    @Produces
    public SessionManager inject(InjectionPoint injection) {
        Bean<?> bean = injection.getBean();
        Class<?> beanClass = bean.getBeanClass();
        ServerEndpoint se = beanClass.getAnnotation(ServerEndpoint.class);
        if (se != null) {
            String contextPath = se.value();
            System.out.println("Obtaining session store for " + contextPath);
            return managers.getSessionManager(contextPath);
        } else {
            throw new IllegalStateException("Cannot create SessionManager for class without ServerEndpoint annotation");
        }
    }
}
