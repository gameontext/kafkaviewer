package org.ozzy.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Resource;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Bean;
import java.util.Properties;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

/**
  Builds a kafkaconsumer with no subscriptions..
 */
public class KafkaConsumerInjector {

    @Resource( lookup = "kafkaUrl" )
    private String kafkaUrl;

    @Produces
    public KafkaConsumer<String,String> expose(InjectionPoint injection) {
  		System.out.println("Building kafka for url "+kafkaUrl+" for class "+injection.getBean().getBeanClass().getName());

  		if (System.getProperty("java.security.auth.login.config") == null) {
  			System.out.println("Fudging jaas property.");
  			System.setProperty("java.security.auth.login.config", "");
  		}

  		System.out.println("Building consumer.. ");
  		Properties consumerProps = new Properties();
  		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
  		consumerProps.put("request.required.acks", "-1");
  		consumerProps.put("group.id", "webmonitor");//."+injection.getBean().getBeanClass().getName());
  		//consumerProps.put("autocommit.enable", "true");
  		//consumerProps.put("autocommit.interval.ms", "1000");
  		//consumerProps.put("zk.sessiontimeout.ms", 1000);
  		consumerProps.put("session.timeout.ms", "30000");
  		consumerProps.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
  		consumerProps.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

  		// this is a cheat, we need to enable ssl when talking to message
  		// hub, and not to kafka locally. The easiest way to know which we are
  		// running on, is to check how many hosts are in kafkaUrl.
  		// Locally for kafka there'll only ever be one, and messagehub gives
  		// us a whole bunch..
  		boolean multipleHosts = kafkaUrl.indexOf(",") != -1;
  		if (multipleHosts) {
  			System.out.println("Initialising ssl consumer for kafka");
  			consumerProps.put("security.protocol", "SASL_SSL");
  			consumerProps.put("ssl.protocol", "TLSv1.2");
  			consumerProps.put("ssl.enabled.protocols", "TLSv1.2");
  			Path p = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
  			consumerProps.put("ssl.truststore.location", p.toString());
  			consumerProps.put("ssl.truststore.password", "changeit");
  			consumerProps.put("ssl.truststore.type", "JKS");
  			consumerProps.put("ssl.endpoint.identification.algorithm", "HTTPS");
  		}

  		return new KafkaConsumer<String, String>(consumerProps);
    }
}
