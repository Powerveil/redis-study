package com.hmdp.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.constant.SystemConstants;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName VoucherOrderManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/3/23 19:19
 * @Version 1.0
 */
@Component
@Slf4j
public class VoucherOrderManager {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private IVoucherService voucherService;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);

    /**
     * 因为这里处理订单的速度不需要特别快，所以一个线程就行了
     */
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();
    private VoucherOrderManager proxy;

    @PostConstruct
    private void init() {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }

    private class VoucherOrderHandler implements Runnable {

        @Override
        public void run() {
            while (true) {
                // 1..获取队列中的订单信息
                try {
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 2.创建订单
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("处理订单异常", e);
                }
            }
        }

    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        // 获取锁
        // 理论上来讲不用加锁也ok，为什么又要加锁呢？为了兜底，以防万一
        RLock rLock = redissonClient.getLock("order:" + userId);
        // 判断是否获取锁成功
        // 无参是获取失败不等待，锁的释放时间是30s
        boolean isLock = rLock.tryLock();
        if (!isLock) {
            // 获取锁失败，返回错误或重试
            log.error("userId={}, voucherId={}，一人只能一单", userId, voucherId);
            return;
//            throw new RuntimeException("一人只能一单");
        }

        try {
            proxy.createVoucherOrder(voucherOrder);

//            return voucherOrderManager.createVoucherOrder(voucherId);
        } finally {
            // 释放锁
            rLock.unlock();
        }
    }


    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    public Result seckillVoucher(Long voucherId) {
        // 1.执行Lua脚本
        Long userId = UserHolder.getUser().getId();
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                                                   Collections.emptyList(),
                                                    voucherId.toString(), userId.toString());
        // 2.判断是否为0
//        if (SystemConstants.SECKILL_INSUFFICIENT_STOCK.equals(result)) {
//
//        } else if (SystemConstants.SECKILL_PURCHASED.equals(result)) {
//
//        }

        if (!SystemConstants.SECKILL_NO_PURCHASE.equals(result)) {
            // 2.1.不为0，代表没有购买资格
            return Result.fail(SystemConstants.SECKILL_INSUFFICIENT_STOCK.equals(result) ? "库存不足": "不能重复下单");
        }
        // 2.2.为0，有购买资格，把下单信息保存到阻塞队列
        long orderId = redisIdWorker.nextId("order");
        // TODO 保存阻塞队列
        VoucherOrder voucherOrder = new VoucherOrder();
        // 2.3.返回订单id
        voucherOrder.setId(orderId);
        // 2.4.设置用户id
        voucherOrder.setUserId(userId);
        // 2.5.设置优惠卷id
        voucherOrder.setVoucherId(voucherId);
        // 2.6.创建阻塞队列
        orderTasks.add(voucherOrder);
        // 2.7.获取代理对象（事务） 这里不能使用创建是初始化和在init方法中初始化
        proxy = (VoucherOrderManager) AopContext.currentProxy();
        // 3.返回订单id
        return Result.ok(orderId);
    }




    // 第二个版本（没有使用Lua脚本）
//    public Result seckillVoucher(Long voucherId) {
//        // 1.查询优惠卷
//        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
//        // 2.判断秒杀是否开始
//        LocalDateTime now = LocalDateTime.now();
//        // 2.判断秒杀是否开始
//        if (now.isBefore(voucher.getBeginTime())) {
//            return Result.fail("秒杀未开始");
//        }
//        // 3.判断秒杀是否已经结束
//        if (now.isAfter(voucher.getEndTime())) {
//            return Result.fail("秒杀已结束");
//        }
//        // 4.判断库存是否充足
//        if (voucher.getStock() <= 0) {
//            return Result.fail("库存不足");
//        }
//
//        Long userId = UserHolder.getUser().getId();
//        // 获取锁
////        ILock iLock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
//        // 判断是否获取锁成功
////        boolean isLock = iLock.tryLock(15L);
//
//        RLock rLock = redissonClient.getLock("order:" + userId);
//        // 无参是获取失败不等待，锁的释放时间是30s
//        boolean isLock = rLock.tryLock();
//        if (!isLock) {
//            // 获取锁失败，返回错误或重试
//            return Result.fail("一人只能一单");
//        }
//
//        try {
//            VoucherOrderManager voucherOrderManager = (VoucherOrderManager) AopContext.currentProxy();
//            return voucherOrderManager.createVoucherOrder(voucherId);
//        } finally {
//            // 释放锁
////            iLock.unlock();
//            rLock.unlock();
//        }
////        synchronized (userId.toString().intern()) {
////            // 获取代理对象（事务）
////            VoucherOrderManager voucherOrderManager = (VoucherOrderManager) AopContext.currentProxy();
////            return voucherOrderManager.createVoucherOrder(voucherId);
////        }
//    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        // 5.一人一单
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();
        // 5.1 查询订单
        int count = voucherOrderService.count(new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId));
        // 5.2 判断是否存在
        if (count > 0) {
            throw new RuntimeException("一人只能一单");
        }
        // 6.扣减库存
        boolean update = seckillVoucherService.update().setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!update) {
            throw new RuntimeException("库存不足");
        }
        // 7.创建订单
        voucherOrderService.save(voucherOrder);
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        // 5.一人一单
        Long userId = UserHolder.getUser().getId();
        Long voucherOrderId = null;
        // 5.1 查询订单
        int count = voucherOrderService.count(new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId));
        // 5.2 判断是否存在
        if (count > 0) {
            return Result.fail("一人只能一单");
        }
        // 6.扣减库存
        boolean update = seckillVoucherService.update().setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!update) {
            return Result.fail("库存不足");
        }
        // 7.创建订单
        long id = redisIdWorker.nextId("order");
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(id);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);
        voucherOrderService.save(voucherOrder);
        voucherOrderId = voucherOrder.getId();
        // 8.返回订单id
        return Result.ok(voucherOrderId);

    }


//    @Transactional
//    public Long seckillVoucher(Long voucherId) {
//        // 1.获取用户id
//        Long userId = UserHolder.getUser().getId();
//        // 1.1先填充数据再查询优惠卷
//        long id = redisIdWorker.nextId("order");
//        VoucherOrder voucherOrder = new VoucherOrder();
//        voucherOrder.setId(id);
//        voucherOrder.setUserId(userId);
//        voucherOrder.setVoucherId(voucherId);
//        // 2.查询优惠卷是否过期，库存是否大于0
//        LocalDateTime now = LocalDateTime.now();
//        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper
//                .eq(SeckillVoucher::getVoucherId, voucherId)
//                .le(SeckillVoucher::getBeginTime, now)
//                .ge(SeckillVoucher::getEndTime, now)
//                .gt(SeckillVoucher::getStock, 0);
//        SeckillVoucher one = seckillVoucherService.getOne(queryWrapper);
//        if (Objects.isNull(one)) {
//            return -1L;
//        }
//        // 3.天假数据到优惠卷订单表中
//        voucherOrderService.save(voucherOrder);
//        // 4..更新库存表
//        seckillVoucherMapper.deleteStock(voucherId, 1);
//        return voucherOrder.getId();
//    }
}
