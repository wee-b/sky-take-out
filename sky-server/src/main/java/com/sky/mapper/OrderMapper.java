package com.sky.mapper;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Orders order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from sky_take_out.orders where id = #{id}")
    Orders getByOrderId(Long id);

    OrderStatisticsVO statistics();

    @Update("update orders set status = #{status} where id = #{id}")
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    @Update("update orders set status = #{OrderStatus},pay_status = #{OrderPaidStatus},checkout_time = #{check_out_time} where number = #{orderNumber}")
    void updateStatus(Integer OrderPaidStatus,Integer OrderStatus,LocalDateTime check_out_time,String orderNumber);
}
