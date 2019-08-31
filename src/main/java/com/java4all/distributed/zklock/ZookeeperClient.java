package com.java4all.distributed.zklock;

import com.java4all.distributed.constant.ZookeeperProperties;
import com.java4all.distributed.entity.ProductStock;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import javax.annotation.Resource;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 * zookeeper client
 * @author wangzhongxiang
 * @date 2018/12/28 10:06
 */
public class ZookeeperClient {

  @Autowired
  public ZookeeperProperties zookeeperProperties;

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperClient.class);
  public static volatile ZookeeperClient zookeeperClient;
  private final String HOST = zookeeperProperties.getZkHost();
  private static CountDownLatch countDownLatch = new CountDownLatch(1);

  public static synchronized ZookeeperClient getInstance(){
    if(null == zookeeperClient){
      synchronized (ZookeeperClient.class){
        if(null == zookeeperClient){
          zookeeperClient = new ZookeeperClient();
          System.out.println("锚点一----------------");
        }
      }
    }
    return zookeeperClient;
  }

  public ZooKeeper getClient(){
    try {
      ZooKeeper zooKeeper =
          new ZooKeeper(HOST,zookeeperProperties.getZkSessionTimeout(),new ZkWatcher());
      countDownLatch.await();
      return zooKeeper;
    }catch (IOException ex){
      ex.printStackTrace();
      return null;
    }catch (InterruptedException ex){
      ex.printStackTrace();
      return null;
    }

  }

  private class ZkWatcher implements Watcher{
    @Override
    public void process(WatchedEvent watchedEvent) {
      if(KeeperState.SyncConnected == watchedEvent.getState()){
        countDownLatch.countDown();
        LOGGER.info("【Zookeeper】连接成功......");
      }else {
        LOGGER.info("【Zookeeper】正在连接中......");
      }
    }
  }

  private ZookeeperClient() {
  }
}
