package com.java4all.distributed.zklock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description:
 * zookeeper distributed lock util
 * @author wangzhongxiang
 * @date 2018/12/28 10:39
 */
public class ZkDistributedLock {

  private static  final Logger logger = LoggerFactory.getLogger(ZkDistributedLock.class);
  private static ZooKeeper zookeeper = null;

  private static final String ROOT_NODE_LOCK = "/ROOT_LOCK";
  private static String currentLockId;
  private static final String DATA = "node";
  private static final CountDownLatch countDownLatch = new CountDownLatch(1);


  //TODO 这里单例session还是？
  static {
    ZkClient zkClient = ZkClient.getInstance();
    zookeeper = zkClient.getClient();
  }


  /**check root node*/
  public static void checkRootNode(){
    try {
      Stat stat = zookeeper.exists(ROOT_NODE_LOCK, false);
      if(null == stat){
        zookeeper.create(ROOT_NODE_LOCK,DATA.getBytes(),
            Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
      }
    }catch (Exception ex){
      logger.info("【Zookeeper】Failed to create root node",ex);
    }
  }

  /**
   * try acquire
   * @param key
   * @param time
   * @param unit
   * @throws Exception
   */
  public static boolean acquire(String key,long time, TimeUnit unit) {
    try {
      //判断根节点
      checkRootNode();
      //根节点下创建 临时 顺序 节点   临时节点保证不会出现死锁，即使服务加锁后挂了，那么在会话过期后，也会自动删除节点
      currentLockId =
          zookeeper.create(ROOT_NODE_LOCK + "/", DATA.getBytes(),
              Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
      //获取根节点下的子节点集合   这里不需要watch,避免羊群效应
      List<String> children = zookeeper.getChildren(ROOT_NODE_LOCK, false);
      //子节点集合排序
      Collections.sort(children);
      //获取第一个子节点
      String firstNode = children.get(0);
      //当前节点
      String currentNode = currentLockId.substring(currentLockId.lastIndexOf("/") + 1);
      //如果当前节点是第一个子节点  加锁成功
      if (currentNode.equals(firstNode)) {
        return true;
      }
      //获取当前节点索引
      int index = children.indexOf(currentNode);
      if (index > 0) {
        //获取当前节点的前一个节点   只获取监听前一个子节点，而不要获取他之前的所有节点，避免羊群效应
        String preNode = children.get(index - 1);
        //TODO 这里的读和删除的原子性？
        zookeeper.exists(ROOT_NODE_LOCK + "/" + preNode, new Watcher() {
          @Override
          public void process(WatchedEvent watchedEvent) {
            //监听前一个节点的删除事件
            if (watchedEvent.getType().equals(EventType.NodeDeleted)) {
              countDownLatch.countDown();
            }
          }
        });
        //当前一个节点删除了，加锁成功
        countDownLatch.await();
        return true;
      }
    }catch (Exception ex){
      logger.info("【Zookeeper】Failed to acquire lock:",ex);
    }
    return false;
  }


  /**
   * try release
   * @param key
   * @throws Exception
   */
  public static boolean release(String key) throws Exception {
    try {
      //删除当前子节点
      zookeeper.delete(currentLockId,-1);
      return true;
    }catch (Exception ex){
      logger.info("【Zookeeper】Failed to release lock:",ex);
    }
    return false;
  }
}
