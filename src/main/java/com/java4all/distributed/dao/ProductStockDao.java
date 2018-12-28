package com.java4all.distributed.dao;

import com.java4all.distributed.entity.ProductStock;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * description:
 *
 * @author wangzhongxiang
 * @date 2018/11/28 15:10
 */
@Repository
public interface ProductStockDao {

  /**根据id查询库存*/
  ProductStock getById(Integer id);

  /**更新库存*/
  void updateStockById(@Param("id") Integer id, @Param("stock") Integer stock);
}
