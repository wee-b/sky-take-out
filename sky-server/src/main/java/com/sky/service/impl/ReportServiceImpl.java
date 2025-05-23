package com.sky.service.impl;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    public TurnoverReportVO getTurnoverReport(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 营业额：状态为"已完成"的订单金额合计
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }
        
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        while (begin.isBefore(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();

        int totalCount = 0;
        for(LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);

            Integer newUser = userMapper.countByMap(map);
            totalCount += newUser;
            totalUserList.add(totalCount);
            newUserList.add(newUser);

        }


        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        LocalDate step = begin;
        while (step.isBefore(end)) {
            dateList.add(step);
            step = step.plusDays(1);
        }

        log.info("getOrderStatistics beginTime:"+begin+",endTime:"+ end);
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        List<Orders> orders = orderMapper.dateRangeQuery(map);
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        int totalCount = 0;
        int validCount = 0;
        int idx = 0;
        for(LocalDate date : dateList) {
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            while (orders.size()>0 && idx<orders.size() && orders.get(idx).getOrderTime().isBefore(dayEnd) ){
                if(orders.get(idx).getStatus().equals(Orders.COMPLETED)){
                    validCount++;
                }
                totalCount++;
                idx++;
            }
            orderCountList.add(totalCount);
            validOrderCountList.add(validCount);
        }

        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalCount)
                .validOrderCount(validCount)
                .orderCompletionRate((double) validCount/totalCount)
                .build();
    }

//    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
//
//
//        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
//        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
//
//        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop(beginTime, endTime);
//
//        List<String> nameList = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
//        List<Integer> numberList = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
//
//
//        return SalesTop10ReportVO
//                .builder()
//                .nameList(StringUtils.join(nameList,","))
//                .numberList(StringUtils.join(numberList,","))
//                .build();
//    }

    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        List<Orders> orders = orderMapper.dateRangeQuery(map);

        HashMap<String,Integer> cnt = new HashMap<>();
        for(Orders order : orders) {
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
            for(OrderDetail orderDetail : orderDetails) {
                String key = orderDetail.getName() +"+"+orderDetail.getOrderId();
                cnt.merge(key,1,Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(cnt.entrySet());
        // 使用 Collections.sort 方法结合自定义比较器进行排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                // 按值从大到小排序
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for(int i = 0;i<Math.min(10,list.size());i++){
            Map.Entry<String, Integer> entry = list.get(i);
            nameList.add(entry.getKey().split("\\+")[0]);
            numberList.add(entry.getValue());
        }

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
