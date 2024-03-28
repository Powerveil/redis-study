package com.hmdp.constant;

public class SystemConstants {
    public static final String IMAGE_UPLOAD_DIR = "D:\\lesson\\nginx-1.18.0\\html\\hmdp\\imgs\\";
    public static final String USER_NICK_NAME_PREFIX = "user_";
    public static final int DEFAULT_PAGE_SIZE = 5;
    public static final int MAX_PAGE_SIZE = 10;


    /**
     * 定义购买产品的常量
     */
    public static final Long SECKILL_NO_PURCHASE = 0L;

    /**
     * 定义库存不足的常量
     */
    public static final Long SECKILL_INSUFFICIENT_STOCK = 1L;

    /**
     * 定义已购买的常量
     */
    public static final Long SECKILL_PURCHASED = 2L;
}
