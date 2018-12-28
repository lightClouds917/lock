package com.java4all.distributed.service;


import com.java4all.distributed.entity.ProductStock;

/**
 * description:
 *
 * @author wangzhongxiang
 * @date 2018/11/28 15:10
 */
public interface ProductStockService {

  ProductStock getById(Integer id);

  void updateStockById(Integer id, Integer stock);

  void updateStockById1(Integer id, Integer stock);

  void updateStockById2(Integer id, Integer stock);

  void updateStockById3(Integer id, Integer stock);
}
