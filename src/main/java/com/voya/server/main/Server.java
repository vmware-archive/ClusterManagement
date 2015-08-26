package com.voya.server.main;

import java.util.logging.Logger;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * The Server class is a launcher for a GemFire Server Peer Cache data node member configured with Spring Data GemFire.
 *
 * @author jb
 * @see org.springframework.context.ConfigurableApplicationContext
 */
public class Server {

  protected static final Logger log = Logger.getLogger(Server.class.getName());

  public static void main(final String... args) {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(
      "/META-INF/spring/gemfire/spring-gemfire-server-cache.xml");
    applicationContext.registerShutdownHook();
    log.info("Ready!");
  }

}
