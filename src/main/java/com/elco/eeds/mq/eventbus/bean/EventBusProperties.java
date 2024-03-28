package com.elco.eeds.mq.eventbus.bean;

import com.elco.eeds.agent.mq.plugin.BasePluginInfo;
import io.vertx.core.impl.cpu.CpuCoreSensor;

/**
 * @ClassName EventBusProperties
 * @Description eventbus配置类
 * @Author OuYang
 * @Date 2024/3/28 9:59
 * @Version 1.0
 */
public class EventBusProperties extends BasePluginInfo {

  public static final int DEFAULT_EVENT_LOOP_POOL_SIZE = 2 * CpuCoreSensor.availableProcessors();

  public static final String MQ_TYPE = "eventbus";

  public static final int DEFAULT_WORKER_POOL_SIZE = 20;
  /**
   * EventBus工作线程池
   */
  private int workerPoolSize = DEFAULT_WORKER_POOL_SIZE;


  public EventBusProperties() {
    super(MQ_TYPE);
  }


  public EventBusProperties(int workerPoolSize) {
    super(MQ_TYPE);
    this.workerPoolSize = workerPoolSize;
  }

  public int getWorkerPoolSize() {
    return workerPoolSize;
  }
}
