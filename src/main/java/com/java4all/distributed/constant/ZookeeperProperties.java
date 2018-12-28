package com.java4all.distributed.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author wangzhongxiang
 * @date 2018/12/28 10:10
 */
@Component
@Data
public class ZookeeperProperties {

  @Value("${zk.host}")
  private String zkHost;
  @Value("${zk.session.timeout}")
  private Integer zkSessionTimeout;
}
