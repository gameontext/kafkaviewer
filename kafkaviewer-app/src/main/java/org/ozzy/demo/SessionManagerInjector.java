package org.ozzy.demo;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Annotated;
import java.lang.annotation.Annotation;
import javax.inject.Inject;
import javax.websocket.server.ServerEndpoint;

public class SessionManagerInjector {

  @Inject
  SessionManagerStore managers;

  /**
    CDI producer that returns a session manager for each
    web socket endpoint, using the websocket Endpoint
    annotation to obtain the context path.
  */
  @Produces
  public SessionManager inject(InjectionPoint injection){
    Bean bean = injection.getBean();
    Class<?> beanClass = bean.getBeanClass();
    ServerEndpoint se = beanClass.getAnnotation(ServerEndpoint.class);
    if(se!=null){
      String contextPath = se.value();
      System.out.println("Obtaining session store for "+contextPath);
      return managers.getSessionManager(contextPath);
    }else{
      throw new IllegalStateException("Cannot create SessionManager for class without ServerEndpoint annotation");
    }
  }
}
