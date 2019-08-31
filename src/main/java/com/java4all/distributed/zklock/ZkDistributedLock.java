package com.java4all.distributed.zklock;

import com.java4all.distributed.constant.ZookeeperProperties;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author wangzhongxiang
 * @date 2019年08月31日 11:18:43
 */
@Component
public class ZkDistributedLock {

    @Autowired
    private ZookeeperProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(ZkDistributedLock.class);
    private static final ThreadLocal<String> threadLocal = new ThreadLocal();
    private static final String ROOT_NODE_LOCK = "/ROOT_LOCK";
    private static String currentLockId;
    private static final String DATA = "node";
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);
    private static ZooKeeper zooKeeper;

    /**
     * try acquire
     * @throws Exception
     */
    public boolean acquire() {
        zooKeeper = this.getClient();
        try {
            //判断根节点
            this.checkRootNode();
            //根节点下创建 临时 顺序 节点   临时节点保证不会出现死锁，即使服务加锁后挂了，那么在会话过期后，也会自动删除节点
            currentLockId =
                    zooKeeper.create(ROOT_NODE_LOCK + "/", DATA.getBytes(),
                            Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            //获取根节点下的子节点集合   这里不需要watch,避免羊群效应
            List<String> children = zooKeeper.getChildren(ROOT_NODE_LOCK, false);
            //子节点集合排序
            Collections.sort(children);
            //获取第一个子节点
            String firstNode = children.get(0);
            //当前节点
            String currentNode = currentLockId.substring(currentLockId.lastIndexOf("/") + 1);
            threadLocal.set(currentLockId);
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
                zooKeeper.exists(ROOT_NODE_LOCK + "/" + preNode, new Watcher() {
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
            logger.info("【Zookeeper】Failed to acquire lock {} ",currentLockId,ex);
        }
        return false;
    }

    /**check root node*/
    public void checkRootNode(){
        try {
            Stat stat = zooKeeper.exists(ROOT_NODE_LOCK, false);
            if(null == stat){
                zooKeeper.create(ROOT_NODE_LOCK,DATA.getBytes(),
                        Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }catch (Exception ex){
            logger.info("【Zookeeper】Failed to create root node {} :",ROOT_NODE_LOCK,ex);
        }
    }


    /**
     * try release
     * @throws Exception
     */
    public boolean release() {
        try {
            //删除当前子节点
            if(!StringUtils.isEmpty(threadLocal.get())){
                zooKeeper.delete(threadLocal.get(),-1);
            }
            return true;
        }catch (Exception ex){
            logger.info("【Zookeeper】Failed to release lock:",ex);
        }
        return false;
    }

    private ZooKeeper getClient(){
        try {
            ZooKeeper zooKeeper =
                    new ZooKeeper(properties.getZkHost(),properties.getZkSessionTimeout(),new ZkWatcher());
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

    private class ZkWatcher implements Watcher {
        @Override
        public void process(WatchedEvent watchedEvent) {
            if(KeeperState.SyncConnected == watchedEvent.getState()){
                countDownLatch.countDown();
                logger.info("【Zookeeper】连接成功......");
            }else {
                logger.info("【Zookeeper】正在连接中......");
            }
        }
    }

}
