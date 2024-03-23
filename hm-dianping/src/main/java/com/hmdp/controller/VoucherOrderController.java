package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.manager.VoucherOrderManager;
import com.hmdp.service.IVoucherOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Autowired
    private IVoucherOrderService voucherOrderService;

    @Autowired
    private VoucherOrderManager voucherOrderManager;

    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderManager.seckillVoucher(voucherId);
    }

//    @PostMapping("seckill/{id}")
//    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
//        Long orderId = voucherOrderManager.seckillVoucher(voucherId);
//        return Result.ok(orderId);
//    }
}
