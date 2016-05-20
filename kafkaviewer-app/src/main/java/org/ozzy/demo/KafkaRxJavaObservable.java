package org.ozzy.demo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import javax.annotation.PostConstruct;
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
import javax.inject.Inject;
import javax.inject.Provider;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import rx.Observable;
import rx.Observer;
import rx.Observable.OnSubscribe;
import rx.observables.AsyncOnSubscribe;
import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import java.util.concurrent.LinkedBlockingQueue;

@ApplicationScoped
public class KafkaRxJavaObservable {

	@Inject
	private Provider<KafkaRxAsyncDataProvider> dataProvider;

	@Resource( lookup = "java:comp/DefaultManagedScheduledExecutorService" )
  ManagedScheduledExecutorService executor;

	public Observable<GameOnEvent> consume() {
		System.out.println("RXJava Observable ["+this.hashCode()+"] consume invoked. Obtaining dataProvider.");
		KafkaRxAsyncDataProvider dp = dataProvider.get();
		Action0 unsubscribeHandler = new Action0() {
			@Override
			public void call(){
				System.out.println("RXJava Observable ["+KafkaRxJavaObservable.this.hashCode()+"] unsubscribe called, shutting down dataProvider.");
				dp.shutdown();
				System.out.println("RXJava Observable ["+KafkaRxJavaObservable.this.hashCode()+"] dataProvider shutdown complete.");
			}
		};

		OnSubscribe<GameOnEvent> os = AsyncOnSubscribe.createStateless(dp.getCallback());

		//before we pass back the Observable, we'll hook to it's unsubscribe, and
		//move subscribers to a new thread so they don't block on the data.
		Observable<GameOnEvent> goo = Observable
		.create(os)
		.doOnUnsubscribe(() -> unsubscribeHandler.call())
		.subscribeOn(Schedulers.from(executor));
		return goo;
	}

}
