package com.elco.eeds.mq.eventbus.handler;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName ExceptionHandler
 * @Description 异常处理
 * @Author OuYang
 * @Date 2024/3/27 15:12
 * @Version 1.0
 */
public class ExceptionHandler implements Handler<Throwable> {

  private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
  private MessageConsumer<String> consumer;

  public ExceptionHandler(MessageConsumer<String> consumer) {
    this.consumer = consumer;
  }

  @Override
  public void handle(Throwable event) {
    String address = consumer.address();
    logger.error("回调数据发生异常,topic:{},异常:{}", address, event);
    //移除该topic的订阅
    consumer.unregister();
    logger.info("回调数据发生异常,移除订阅:{}", address);
  }
}
