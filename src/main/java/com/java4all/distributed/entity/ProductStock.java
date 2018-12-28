package com.java4all.distributed.entity;

import lombok.Data;

/**
 * description:
 * 商品库存
 * @author wangzhongxiang
 * @date 2018/11/28 15:04
 */
@Data
public class ProductStock {

  private Integer id;

  private String name;

  private Integer stock;
}
