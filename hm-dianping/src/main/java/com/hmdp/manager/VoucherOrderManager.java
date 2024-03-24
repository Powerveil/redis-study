package com.hmdp.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @ClassName VoucherOrderManager
 * @Description TODO(一句话描述该类的功能)
 * @Author Powerveil
 * @Date 2024/3/23 19:19
 * @Version 1.0
 */
@Component
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

    public Result seckillVoucher(Long voucherId) {
        // 1.查询优惠卷
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        LocalDateTime now = LocalDateTime.now();
        // 2.判断秒杀是否开始
        if (now.isBefore(voucher.getBeginTime())) {
            return Result.fail("秒杀未开始");
        }
        // 3.判断秒杀是否已经结束
        if (now.isAfter(voucher.getEndTime())) {
            return Result.fail("秒杀已结束");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() <= 0) {
            return Result.fail("库存不足");
        }

        Long userId = UserHolder.getUser().getId();
        synchronized (userId.toString().intern()) {
            // 获取代理对象（事务）
            VoucherOrderManager voucherOrderManager = (VoucherOrderManager) AopContext.currentProxy();
            return voucherOrderManager.createVoucherOrder(voucherId);
        }
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
