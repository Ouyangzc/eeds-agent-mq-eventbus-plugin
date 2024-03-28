package com.elco.eeds.mq.eventbus.handler;

import com.elco.eeds.mq.eventbus.constant.RegistrationEnum;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName RegistrationHandler
 * @Description 注册监听
 * @Author OuYang
 * @Date 2024/3/27 15:17
 * @Version 1.0
 */
public class RegistrationHandler implements Handler<AsyncResult<Void>> {

  private static final Logger logger = LoggerFactory.getLogger(RegistrationHandler.class);
  private MessageConsumer<String> consumer;

  private RegistrationEnum registrationEnum;

  public RegistrationHandler(MessageConsumer<String> consumer, RegistrationEnum registrationEnum) {
    this.consumer = consumer;
    this.registrationEnum = registrationEnum;
  }

  @Override
  public void handle(AsyncResult<Void> result) {
    String address = consumer.address();
    String type = registrationEnum.getType();
    if (result.succeeded()) {
      logger.info("topic " +type  + " has success,topic:{}",
          address);
    } else {
      logger.error("topic " + type + " failed,topic:{}", address);
    }
  }
}
