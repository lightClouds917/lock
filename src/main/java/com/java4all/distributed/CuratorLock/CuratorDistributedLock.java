package com.java4all.distributed.CuratorLock;

import com.java4all.distributed.constant.ZookeeperProperties;
import com.java4all.distributed.zklock.ZkClient;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 * Curator Distributed Lock
 * @author wangzhongxiang
 * @date 2019/01/01 15:51
 */
public class CuratorDistributedLock {
  @Resource
  private static ZookeeperProperties zookeeperProperties;
  private static final Logger LOGGER = LoggerFactory.getLogger(CuratorDistributedLock.class);
  private static final String ROOT_NODE_LOCK = "/ROOT_LOCK";
  private static final String HOST = zookeeperProperties.getZkHost();
  private static final Integer SESSION_TIMEOUT = zookeeperProperties.getZkSessionTimeout();


  public static void main(String[]args){
    try {

      //重试策略 重试间隔1s,重试3次 //TODO 写到配置文件里面
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
      //工厂创建连接
      CuratorFramework curator = CuratorFrameworkFactory.builder()
          .connectString(HOST)
          .sessionTimeoutMs(SESSION_TIMEOUT)
          .retryPolicy(retryPolicy)
          .build();
      //开启连接
      curator.start();
      //分布式锁
      InterProcessMutex lock = new InterProcessMutex(curator, ROOT_NODE_LOCK);
      //加锁 最多等待5s
      lock.acquire(5, TimeUnit.SECONDS);
      //释放锁
      lock.release();
    }catch (Exception ex){
      LOGGER.info("【Curator】Failed to acquire lock:",ex);
    }
  }
}
