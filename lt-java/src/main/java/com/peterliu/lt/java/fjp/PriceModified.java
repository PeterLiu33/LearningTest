package com.peterliu.lt.java.fjp;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * 《 Java 7 Concurrency Cookbook 》 第五章案例自实现, 更新产品列表的价格
 * <p>
 * Created by liujun on 2018/1/24.
 */
@AllArgsConstructor
public class PriceModified  extends RecursiveAction {

    //父类ForkJoinTask实现了Serializable接口
    private static final long serialVersionUID = 1L;

    private List<Product> products;
    private int first;
    private int last;
    // 存储价格的增长
    private double increment;
    // 任务粒度分割线
    private static final int DIVIDE_AND_CONQUER_SIZE = 10;

    @Override
    protected void compute() {
        if (last - first < DIVIDE_AND_CONQUER_SIZE) {
            updatePrice();
        } else {
            int middle = (last + first) / 2;
            PriceModified left = new PriceModified(products, first, middle + 1, increment);
            PriceModified right = new PriceModified(products, middle + 1, last, increment);
            // 同步调用
            invokeAll(left, right);
        }
    }

    // 更新价格
    private void updatePrice() {
        for (int i = first; i < last; i++) {
            products.get(i).setPrice(products.get(i).getPrice() + increment);
        }
    }


    //:内部类///////////////////////////////////////////////////////

    // 产品类
    @Data
    @AllArgsConstructor
    public static class Product {
        private String name;
        private double price;
    }

    // 产品制造类
    public static class ProductListGenerator {
        public List<Product> generate(int size) {
            List<Product> ret = Lists.newArrayList();
            for (int i = 0; i < size; i++) {
                ret.add(new Product("Product" + i, 10));
            }
            return ret;
        }
    }
}
