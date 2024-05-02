package com.hmdp.utils;


import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author power
 * @Date 2023/1/7 12:12
 */
public class BeanCopyUtils {

    private BeanCopyUtils() {
    }

    public static <V> V copyBean(Object source, Class<V> clazz) {
        // 创建目标对象
        V result = null;
        try {
            result = clazz.newInstance();
            // 实现属性copy
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 返回结果
        return result;
    }


    public static <T,V> List<V> copyBeanList(List<T> list, Class<V> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }

}