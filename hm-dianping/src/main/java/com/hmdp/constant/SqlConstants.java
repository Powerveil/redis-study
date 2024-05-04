package com.hmdp.constant;

/**
 * @ClassName SqlConstants
 * @Description SQL常量类
 * @Author Powerveil
 * @Date 2024/5/4 15:50
 * @Version 1.0
 */
public class SqlConstants {
    /**
     * 使用in的时候拼接sql使顺序不发生改变
     */
    public static final String LAST_SQL_ORDER_BY_ID = "ORDER BY FIELD(id,%s)";


    public static String splicing(String sql, String param) {
        return String.format(sql, param);
    }
}
