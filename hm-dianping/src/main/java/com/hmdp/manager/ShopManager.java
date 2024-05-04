package com.hmdp.manager;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.constant.RedisConstants;
import com.hmdp.constant.SqlConstants;
import com.hmdp.constant.SystemConstants;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.ParamCheckUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/5/4 17:01
 * @Version 1.0
 */
@Component
public class ShopManager {

    @Autowired
    public IShopService shopService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Result queryById(Long id) {
        return shopService.queryById(id);
    }

    public Result saveShop(Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    public Result updateShop(Shop shop) {
        return shopService.updateShop(shop);
    }

    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 1.判断是否需要跟觉坐标查询
        // x和y只要一个为空就不需要跟觉坐标查询
        if (!ParamCheckUtils.validateParams(x, y)) {
            // 根据类型分页查询
            Page<Shop> page = shopService.query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            return Result.ok(page.getRecords());
        }
        // 2.计算分页参数
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        // 3.查询redis，跟觉距离排序、分页。结果：shopId，distance
        String key = RedisConstants.SHOP_GEO_KEY + typeId;
        // 4.解析出id
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo() // GEOSEARCH key BYLONLAT x y BYRANDIUS 10 WITHDISTANCE
                .search(key,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000), // 默认单位m 根据需求去选择
                        // 这里设置的是截止的位置，不能设置初始位置
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        if (Objects.isNull(results)) {
            return Result.ok(Collections.emptyList());
        }
        // 4.1.截取 from - end的部分
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(item -> {
            // 4.2.获取店铺id
            String shopIdStr = item.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            // 4.3.获取距离
            Distance distance = item.getDistance();
            distanceMap.put(shopIdStr, distance);
        });
        // 5.根据id查询Shop
        String join = StrUtil.join(",", ids);
        String lastSql = SqlConstants.splicing(SqlConstants.LAST_SQL_ORDER_BY_ID, join);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Shop::getId, ids)
                .last(lastSql);
        List<Shop> list1 = shopService.list(queryWrapper);

        list1.stream().forEach(item -> {
            item.setDistance(distanceMap.get(item.getId().toString()).getValue());
        });
        // 6.返回
        return Result.ok(list1);
    }

    public Result queryShopByName(String name, Integer current) {
        // 根据类型分页查询
        Page<Shop> page = shopService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }
}
