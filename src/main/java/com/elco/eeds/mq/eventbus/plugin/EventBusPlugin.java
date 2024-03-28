package com.elco.eeds.mq.eventbus.plugin;

import com.elco.eeds.agent.mq.plugin.MQServicePlugin;
import com.elco.eeds.agent.mq.plugin.ReceiverMessagerHandler;
import com.elco.eeds.mq.eventbus.bean.EventBusProperties;
import com.elco.eeds.mq.eventbus.constant.RegistrationEnum;
import com.elco.eeds.mq.eventbus.handler.ExceptionHandler;
import com.elco.eeds.mq.eventbus.handler.RegistrationHandler;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName EventBusPlugin
 * @Description EventBusPlugin插件
 * @Author OuYang
 * @Date 2024/3/27 9:08
 * @Version 1.0
 */
public class EventBusPlugin extends MQServicePlugin<EventBus> {

  private static final Logger logger = LoggerFactory.getLogger(EventBusPlugin.class);

  private final Multimap<String, MessageConsumer<String>> multiMap = ArrayListMultimap.create();

  @Override
  public void publish(String topic, String data, Map<String, Object> extraParams) {
    checkTopic(topic);
    DeliveryOptions deliveryOptions = new DeliveryOptions();
    if (null != extraParams) {
      extraParams.forEach((key, value) -> {
        deliveryOptions.addHeader(key, value.toString());
      });
    }
    client.publish(topic, data, deliveryOptions);
  }

  @Override
  public void publish(String topic, String message) {
    publish(topic, message, null);
  }

  @Override
  public Object subscribe(String topic) {
    throw new RuntimeException("EventBus无同步订阅消费,请使用别的API");
  }

  @Override
  public void subscribe(String topic, ReceiverMessagerHandler messageHandler) {
    checkTopic(topic);
    MessageConsumer<String> consumer = client.consumer(topic, msg -> {
      try {
        String replay = messageHandler.handleRecData(topic, msg.body());
        if (null != replay && "".equals(replay)) {
          msg.reply(replay);
        }
      } catch (Exception e) {
        logger.error("订阅者未处理消息异常,异常信息:", e);
        throw new RuntimeException(e);
      }
    });
    //注册监听
    consumer.completionHandler(new RegistrationHandler(consumer, RegistrationEnum.REGISTER));
    consumer.exceptionHandler(new ExceptionHandler(consumer));
    addConsumer(topic, consumer);
  }

  @Override
  public void subscribeWithQueue(String topic, String queueName,
      ReceiverMessagerHandler messageHandler) {
    throw new RuntimeException("EventBus暂不支持分组消费,可采用Send方法来实现分组消费");
  }

  @Override
  public void unSubscribe(String topic) {
    checkTopic(topic);
    Collection<MessageConsumer<String>> consumers = getConsumers(topic);
    if (Objects.isNull(consumers)) {
      throw new RuntimeException("未找到该topic,所属的消费者");
    }
    for (MessageConsumer<String> consumer : consumers) {
      consumer.unregister(new RegistrationHandler(consumer, RegistrationEnum.UNREGISTER));
    }
    logger.info("移除topic:{},消息处理者数量:{}", topic, consumers.size());
    removeConsumerTopic(topic);
  }

  @Override
  public void send(String topic, String message) {
    send(topic, message, new DeliveryOptions());
  }

  public void send(String topic, String message, DeliveryOptions deliveryOptions) {
    checkTopic(topic);
    client.send(topic, message, deliveryOptions);
  }

  @Override
  public Future<String> request(String topic, String message) {
    return request(topic, message, new DeliveryOptions());
  }

  public Future<String> request(String topic, String message, DeliveryOptions deliveryOptions) {
    checkTopic(topic);
    CompletableFuture<String> reusltFuture = new CompletableFuture<>();
    client.request(topic, message, deliveryOptions).onComplete(ar -> {
      if (ar.succeeded()) {
        reusltFuture.complete(ar.result().body().toString());
      } else {
        reusltFuture.completeExceptionally(ar.cause());
      }
    });
    return reusltFuture;
  }

  @Override
  public void init(String driverInfo) throws Exception {
    Object object = Json.decodeValue(driverInfo);
    VertxOptions options = new VertxOptions();
    if (object instanceof JsonObject) {
      JsonObject jsonObject = new JsonObject(driverInfo);
      EventBusProperties eventBusProperties = jsonObject.mapTo(EventBusProperties.class);
      options.setWorkerPoolSize(eventBusProperties.getWorkerPoolSize());
    }
    Vertx vertx = Vertx.vertx(options);
    EventBus eventBus = vertx.eventBus();
    this.setClient(eventBus);
  }

  @Override
  public boolean checkMqType(String mqType) {
    return mqType.toLowerCase().equals(EventBusProperties.MQ_TYPE);
  }

  private void addConsumer(String topic, MessageConsumer<String> messageConsumer) {
    multiMap.put(topic, messageConsumer);
  }

  private Collection<MessageConsumer<String>> getConsumers(String topic) {
    Collection<MessageConsumer<String>> messageConsumers = multiMap.get(topic);
    return messageConsumers;
  }

  private void removeConsumerTopic(String topic) {
    multiMap.removeAll(topic);
  }

  private void checkTopic(String topic) {
    if (null == topic || "".equals(topic)) {
      throw new RuntimeException("topic不能为空");
    }
  }

}
