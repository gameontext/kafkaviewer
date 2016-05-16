package org.ozzy.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
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

	private static String localKafka = "127.0.0.1:9092";
	// private static String remoteKafka =
	// "kafka01-prod01.messagehub.services.us-south.bluemix.net:9093,kafka02-prod01.messagehub.services.us-south.bluemix.net:9093,kafka03-prod01.messagehub.services.us-south.bluemix.net:9093,kafka04-prod01.messagehub.services.us-south.bluemix.net:9093,kafka05-prod01.messagehub.services.us-south.bluemix.net:9093";
	private String kafkaUrl;

	private KafkaConsumer<String, String> consumer = null;

	@SuppressWarnings({ "unused", "rawtypes" })
	private ScheduledFuture pollingThread;

	public Kafka() {
		System.out.println("Building kafka");

		if (System.getProperty("java.security.auth.login.config") == null) {
			System.out.println("Fudging jaas property.");
			System.setProperty("java.security.auth.login.config", "");
		}

		System.out.println("Building consumer.. ");
		kafkaUrl = localKafka;

		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
		consumerProps.put("acks", "-1");
		consumerProps.put("group.id", "webmonitor");
		consumerProps.put("enable.auto.commit", "true");
		consumerProps.put("auto.commit.interval", "1000");
		consumerProps.put("zookeeper.session.timeout.ms", 1000);
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
			consumerProps.put("security.protocol", "SASL_SSL");
			consumerProps.put("ssl.protocol", "TLSv1.2");
			consumerProps.put("ssl.enabled.protocols", "TLSv1.2");
			Path p = Paths.get(System.getProperty("java.home"), "security", "cacerts");
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
					for (ConsumerRecord<String, String> record : records) {
						System.out.println("Firing event.. ");
						BeanManager bm = CDI.current().getBeanManager();
						bm.fireEvent(new GameOnEvent(record.offset(), record.topic(), record.key(), record.value()));
						System.out.println("event fired");
					}
				}

			}
		};

		ManagedScheduledExecutorService executor;
		try {
			executor = (ManagedScheduledExecutorService) new InitialContext().lookup("concurrent/execSvc");
		} catch (NamingException e) {
			throw new IllegalStateException("Missing scheduler service!", e);
		}

		System.out.println("Registering polling thread");
		pollingThread = executor.scheduleAtFixedRate(r, 100, 100, TimeUnit.MILLISECONDS);

		System.out.println("Subscribing to topics");
		consumer.subscribe(Arrays.asList(new String[] { "gameon" }));

	}

}