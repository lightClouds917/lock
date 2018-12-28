package com.java4all.distributed.controller;

import com.java4all.distributed.entity.ProductStock;
import com.java4all.distributed.service.ProductStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description:
 * 分布式锁测试
 * @author wangzhongxiang
 * @date 2018/11/28 15:17
 */
@RestController
@RequestMapping("productStock")
public class ProductStockController {

  @Autowired
  private ProductStockService productStockService;

  @GetMapping("get/{id}")
  public ProductStock getById(@PathVariable Integer id) {
    ProductStock productStock = productStockService.getById(id);
    System.out.println(productStock.toString());
    return productStock;
  }

  /**测试无锁的情况*/
  @PostMapping("updateStockById")
  public void updateStockById(Integer id,Integer stock){
    productStockService.updateStockById(id,stock);
  }

  /**测试redis分布式锁*/
  @PostMapping("updateStockById1")
  public void updateStockById1(Integer id, Integer stock) {
    productStockService.updateStockById1(id,stock);
  }

  /**测试redisson分布式锁*/
  @PostMapping("updateStockById2")
  public void updateStockById2(Integer id, Integer stock) {
    productStockService.updateStockById2(id,stock);
  }

  /**测试zookeeper分布式锁*/
  @PostMapping("updateStockById3")
  public void updateStockById3(Integer id, Integer stock) {
    productStockService.updateStockById3(id,stock);
  }
}
