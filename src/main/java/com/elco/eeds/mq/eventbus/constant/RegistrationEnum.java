package com.elco.eeds.mq.eventbus.constant;

/**
 * @ClassName RegistrationEnum
 * @Description RegistrationEnum
 * @Author OuYang
 * @Date 2024/3/27 15:23
 * @Version 1.0
 */
public enum RegistrationEnum {
  REGISTER("registration", "订阅"),
  UNREGISTER("unRegistration", "移除订阅");

  private String type;

  private String msg;

  RegistrationEnum(String type, String msg) {
    this.type = type;
    this.msg = msg;
  }

  public String getType() {
    return type;
  }
}
