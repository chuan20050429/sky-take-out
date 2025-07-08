package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {
    /**
     * 处理超时订单
     */
    @Autowired
    private OrderMapper orderMapper;
    @Scheduled(cron = "0 * * * * ? ")//每分钟执行一次
    public void processTimeoutOrder(){
        log.info("处理支付超时订单:{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getBysStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if(ordersList != null && ordersList.size() > 0){
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("支付超时");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理处于待派送状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")//每天凌晨1点执行一次
    public void processDeliveryOrder() {
        log.info("处理处于待派送状态的订单:{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getBysStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
