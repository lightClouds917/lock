package com.java4all.distributed.serviceImpl;

import com.java4all.distributed.dao.ProductStockDao;
import com.java4all.distributed.entity.ProductStock;
import com.java4all.distributed.service.ProductStockService;
import com.java4all.distributed.zklock.ZkDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * description:
 * 测试redis分布式锁
 * @author wangzhongxiang
 * @date 2018/11/28 15:10
 */
@Service
public class ProductStockServiceImpl implements ProductStockService {

  @Autowired
  private ProductStockDao productStockDao;


  @Override
  public ProductStock getById(Integer id) {
    return productStockDao.getById(id);
  }

  /**
   * 测试不加锁的情况
   * @param id
   * @param stock
   */
  @Override
  public void updateStockById(Integer id, Integer stock) {
    ProductStock product = productStockDao.getById(id);
    Integer stock1 = product.getStock();
    stock1 = stock1+stock;
    productStockDao.updateStockById(id,stock1);
  }

  @Override
  public void updateStockById1(Integer id, Integer stock) {

  }

  @Override
  public void updateStockById2(Integer id, Integer stock) {

  }

  /**
   * 测试zookeeper分布式锁
   * @param id
   * @param stock
   */
  @Override
  public void updateStockById3(Integer id, Integer stock) {
    boolean acquire = ZkDistributedLock.acquire();
    if(acquire){
      try {
        ProductStock product = productStockDao.getById(id);
        Thread.sleep(1000);
        Integer stock1 = product.getStock();
        stock1 = stock1+stock;
        productStockDao.updateStockById(id,stock1);
      }catch (Exception ex){
        ex.printStackTrace();
      }finally {
        ZkDistributedLock.release();
      }
    }
  }

}
