package org.ozzy.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.Destroyed;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

@ApplicationScoped
public class Kafka {
	private String kafkaUrl;

	private KafkaConsumer<String, String> consumer = null;

	@SuppressWarnings({ "unused", "rawtypes" })
	private ScheduledFuture pollingThread;

	public Kafka() {
		try{
			kafkaUrl = (String) new InitialContext().lookup("kafkaUrl");
		}catch(NamingException e){
			throw new IllegalStateException(e);
		}

		System.out.println("Building kafka for url "+kafkaUrl);

		if (System.getProperty("java.security.auth.login.config") == null) {
			System.out.println("Fudging jaas property.");
			System.setProperty("java.security.auth.login.config", "");
		}

		System.out.println("Building consumer.. ");
		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
		consumerProps.put("request.required.acks", "-1");
		consumerProps.put("group.id", "webmonitor");
		consumerProps.put("autocommit.enable", "true");
		consumerProps.put("autocommit.interval.ms", "1000");
		consumerProps.put("zk.sessiontimeout.ms", 1000);
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

		consumer = new KafkaConsumer<String, String>(consumerProps);

		Thread r = new Thread() {
			public void run() {
				ConsumerRecords<String, String> records = null;
				synchronized (this) {
					if (consumer != null) {
						records = consumer.poll(100);
					}
				}
				if (records != null && !records.isEmpty()) {
					BeanManager bm = CDI.current().getBeanManager();
					for (ConsumerRecord<String, String> record : records) {
						System.out.println("Firing event.. ");
						bm.fireEvent(new GameOnEvent(record.offset(), record.topic(), record.key(), record.value()));
						System.out.println("event fired");
					}
				}

			}
		};

		ManagedScheduledExecutorService executor;
		try {
			executor = (ManagedScheduledExecutorService) new InitialContext().lookup("java:comp/DefaultManagedScheduledExecutorService");
		} catch (NamingException e) {
			throw new IllegalStateException("Missing scheduler service!", e);
		}

		System.out.println("Registering polling thread");
		pollingThread = executor.scheduleWithFixedDelay(r, 100, 100, TimeUnit.MILLISECONDS);

		System.out.println("Subscribing to topics");
		consumer.subscribe(Arrays.asList(new String[] { "gameon" }));

	}

	public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
		//no op.. just creating this bean did all the work.
	}

  public void destroy(@Observes @Destroyed(ApplicationScoped.class) Object init) {
		System.out.println("Shutting down kafka polling thread");
		pollingThread.cancel(true);
		System.out.println("Closing kafka consumer.");
		consumer.close();
	}

}
