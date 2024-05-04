package com.hmdp.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Powerveil
 * @Date 2023/12/31 15:18
 * 校验参数
 */
public class ParamCheckUtils {
//    public static void validateParams01(Object... obj) {
//        List<String> validationErrors = new ArrayList<>();
//
//        // 遍历参数
//        for (Object parameter : obj) {
//            // 获取对象的所有字段
//            Field[] fields = parameter.getClass().getDeclaredFields();
//
//            // 遍历字段
//            for (Field field : fields) {
//                // 设置字段可访问
//                field.setAccessible(true);
//
//                try {
//                    // 获取字段的值
//                    Object fieldValue = field.get(parameter);
//
//                    if (fieldValue == null) {
//                        validationErrors.add(field.getName() + " 字段为空");
//                    }
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        // 打印错误消息，如果没有错误则打印"校验通过"
//        if (validationErrors.isEmpty()) {
//            System.out.println("校验通过");
//        } else {
//            System.out.println("参数校验异常：");
//            for (String error : validationErrors) {
//                System.out.println(error);
//            }
//        }
//    }

    /**
     * 判断 必须校验参数 是否正常
     *
     * @param params
     * @return 参数是否正常
     * 可以判断
     * - 普通引用类型
     * - Collection及其实现类或接口
     * - Map及其实现类或接口
     */
    public static boolean validateParams(Object... params) {
        for (Object param : params) {
            // 判断是否为Collection的子类
            if (param instanceof Collection) {
                Collection collection = (Collection) param;
                if (collection.isEmpty()) return false;
            }
            // 判断是否为Map的子类
            if (param instanceof Map) {
                Map map = (Map) param;
                if (map.isEmpty()) return false;
            }
            if (Objects.isNull(param)) return false;
        }
        // 参数全部不为null则为正常
        return true;
    }
}