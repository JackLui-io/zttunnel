package com.scsdky.web.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.cell.CellUtil;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelEleMeter;
import com.scsdky.web.domain.vo.InsideAndOutsideVo;
import com.scsdky.web.domain.vo.monitor.TrafficSpeedVo;
import com.scsdky.web.domain.vo.monitor.TrafficVo;
import com.scsdky.web.domain.vo.statistics.StatisticsExcelVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExportExcelUtils {

    /**
     * 总体分析Excel导出
     * @param statisticsExcelVo
     */
    public static void statisticsExportTemp(HttpServletResponse response, StatisticsExcelVo statisticsExcelVo) throws IOException {
        String filePath = "D:/tunnel/file/总体分析"+System.currentTimeMillis()+".xlsx";
        // 通过工具类创建writer
        ExcelWriter writer = ExcelUtil.getWriter(filePath);
        writer.renameSheet("总体分析");
        // 设置高速公路标题
        CellStyle cellStyle = writer.createCellStyle();
        Font font = writer.createFont();
        font.setBold(true);
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short)12);
        cellStyle = setDefaultStyle(cellStyle);
        cellStyle.setFont(font);
        writer.getOrCreateRow(0).setHeightInPoints(29.3f);
        String title = statisticsExcelVo.getTitle();
        writer.merge(0,0,0,7, title,cellStyle);
        // 设置时间段
        CellStyle cellStyle2 = writer.createCellStyle();
        Font font2 = writer.createFont();
        font2.setFontName("Times New Roman");
        font2.setFontHeightInPoints((short)12);
        cellStyle2 = setDefaultStyle(cellStyle2);
        cellStyle2.setFont(font2);
        writer.getOrCreateRow(1).setHeightInPoints(24.75f);
        String dateRange = statisticsExcelVo.getStartDate().replace("-",".")+"-"+statisticsExcelVo.getEndDate().replace("-",".");
        writer.merge(1,1,0,7, dateRange,cellStyle2);
        // 标题
        // 耗电量
        CellStyle cellStyle3 = writer.createCellStyle();
        Font font3 = writer.createFont();
        font3.setBold(true);
        font3.setFontName("宋体");
        font3.setFontHeightInPoints((short)11);
        cellStyle3 = setDefaultStyle(cellStyle3);
        cellStyle3.setFont(font3);
        writer.getOrCreateRow(2).setHeightInPoints(15);
        writer.merge(2,2,0,1, "耗电量/kWh",cellStyle3);
        // 亮灯功率
        writer.merge(2,2,2,3, "亮灯功率/kW",cellStyle3);
        //亮灯时长/h
        writer.merge(2,2,4,5, "亮灯时长/h",cellStyle3);
        //碳排放/tCO2
        writer.merge(2,2,6,7, "亮灯时长/h",cellStyle3);

        CellStyle cellStyle4 = writer.createCellStyle();
        Font font4 = writer.createFont();
        font4.setFontName("宋体");
        font4.setFontHeightInPoints((short)11);
        cellStyle4 = setDefaultStyle(cellStyle4);
        cellStyle4.setFont(font4);

        CellUtil.setCellValue(writer.getOrCreateCell(0,3),"实际耗电量");
        CellUtil.setCellValue(writer.getOrCreateCell(0,4),"原设计耗电量");
        CellUtil.setCellValue(writer.getOrCreateCell(0,5),"理论节电量");
        CellUtil.setCellValue(writer.getOrCreateCell(0,6),"理论节电率");
        CellUtil.setCellValue(writer.getOrCreateCell(1,3),statisticsExcelVo.getActualPowerConsumption());
        CellUtil.setCellValue(writer.getOrCreateCell(1,4),statisticsExcelVo.getOriginalPowerConsumption());
        CellUtil.setCellValue(writer.getOrCreateCell(1,5),statisticsExcelVo.getTheoreticalPowerSaving());
        CellUtil.setCellValue(writer.getOrCreateCell(1,6),statisticsExcelVo.getTheoreticalPowerSavingRate().toString()+"%");

        CellUtil.setCellValue(writer.getOrCreateCell(2,3),"实际运行功率");
        CellUtil.setCellValue(writer.getOrCreateCell(2,4),"设计功率");
        CellUtil.setCellValue(writer.getOrCreateCell(2,5),"功率削减量");
        CellUtil.setCellValue(writer.getOrCreateCell(2,6),"功率削减率");
        CellUtil.setCellValue(writer.getOrCreateCell(3,3),statisticsExcelVo.getActualOperatingPower());
        CellUtil.setCellValue(writer.getOrCreateCell(3,4),statisticsExcelVo.getOriginalOperatingPower());
        CellUtil.setCellValue(writer.getOrCreateCell(3,5),statisticsExcelVo.getTheoreticalOperatingPowerReduction());
        CellUtil.setCellValue(writer.getOrCreateCell(3,6),statisticsExcelVo.getTheoreticalTotalPowerReduction().toString()+"%");

        CellUtil.setCellValue(writer.getOrCreateCell(4,3),"实际时长");
        CellUtil.setCellValue(writer.getOrCreateCell(4,4),"理论时长");
        CellUtil.setCellValue(writer.getOrCreateCell(4,5),"时长削减量");
        CellUtil.setCellValue(writer.getOrCreateCell(4,6),"时长削减率");
        CellUtil.setCellValue(writer.getOrCreateCell(5,3),statisticsExcelVo.getActualLightUpTime());
        CellUtil.setCellValue(writer.getOrCreateCell(5,4),statisticsExcelVo.getOriginalLightUpTime());
        CellUtil.setCellValue(writer.getOrCreateCell(5,5),statisticsExcelVo.getTheoreticalLightUpTimeReduction());
        CellUtil.setCellValue(writer.getOrCreateCell(5,6),statisticsExcelVo.getTheoreticalLightUpTimeReductionRate().toString()+"%");

        CellUtil.setCellValue(writer.getOrCreateCell(6,3),"实际碳排放");
        CellUtil.setCellValue(writer.getOrCreateCell(6,4),"理论碳排放");
        CellUtil.setCellValue(writer.getOrCreateCell(6,5),"碳减排量");
        CellUtil.setCellValue(writer.getOrCreateCell(6,6),"碳减排率");
        CellUtil.setCellValue(writer.getOrCreateCell(7,3),statisticsExcelVo.getActualCarbonEmission());
        CellUtil.setCellValue(writer.getOrCreateCell(7,4),statisticsExcelVo.getOriginalCarbonEmission());
        CellUtil.setCellValue(writer.getOrCreateCell(7,5),statisticsExcelVo.getTheoreticalCarbonEmissionReduction());
        CellUtil.setCellValue(writer.getOrCreateCell(7,6),
                statisticsExcelVo.getTheoreticalCarbonEmissionReductionRate().toString()+"%");

        writer.getOrCreateRow(3).setHeightInPoints(15);
        writer.getOrCreateRow(4).setHeightInPoints(15);
        writer.getOrCreateRow(5).setHeightInPoints(15);
        writer.getOrCreateRow(6).setHeightInPoints(15);

        for (int i = 0; i < 8; i++) {
            writer.getOrCreateCell(i,3).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,4).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,5).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,6).setCellStyle(cellStyle4);
        }

        for(int i=0; i < writer.getRowCount();i++){
            writer.autoSizeColumn(i);
        }
        writer.getWorkbook().write(response.getOutputStream());
        writer.close();
    }

    /**
     * 车流车速导出Excel
     */
    public static void trafficFlowOrSpeedExportTemp(HttpServletResponse response,
                                                    String title,
                                                    String dateRange,
                                                    List<TrafficVo> trafficVoList) throws IOException {

        // 通过工具类创建writer
        ExcelWriter writer = ExcelUtil.getWriter("/tunnel/file/车流车速"+System.currentTimeMillis()+".xlsx");
        writer.renameSheet("车流车速");
        // 设置高速公路标题
        CellStyle cellStyle = writer.createCellStyle();
        Font font = writer.createFont();
        font.setBold(true);
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short)12);
        cellStyle = setDefaultStyle(cellStyle);
        cellStyle.setFont(font);
        writer.getOrCreateRow(0).setHeightInPoints(29.3f);
        writer.merge(0,0,0,1, title,cellStyle);
        // 设置时间段
        CellStyle cellStyle2 = writer.createCellStyle();
        Font font2 = writer.createFont();
        font2.setFontName("Times New Roman");
        font2.setFontHeightInPoints((short)12);
        cellStyle2 = setDefaultStyle(cellStyle2);
        cellStyle2.setFont(font2);
        writer.getOrCreateRow(1).setHeightInPoints(24.75f);
//        String dateRange = statisticsExcelVo.getStartDate().replace("-",".")+"-"+statisticsExcelVo.getEndDate().replace("-",".");
        writer.merge(1,1,0,1, dateRange,cellStyle2);
        // 标题
        // 耗电量
        CellStyle cellStyle3 = writer.createCellStyle();
        Font font3 = writer.createFont();
        font3.setBold(true);
        font3.setFontName("宋体");
        font3.setFontHeightInPoints((short)11);
        cellStyle3 = setDefaultStyle(cellStyle3);
        cellStyle3.setFont(font3);
        writer.getOrCreateRow(2).setHeightInPoints(15);
        writer.merge(2,2,0,1, "车流/辆",cellStyle3);
        //writer.merge(2,2,2,3, "车速/km·h⁻¹",cellStyle3);

        CellStyle cellStyle4 = writer.createCellStyle();
        Font font4 = writer.createFont();
        font4.setFontName("宋体");
        font4.setFontHeightInPoints((short)11);
        cellStyle4 = setDefaultStyle(cellStyle4);
        cellStyle4.setFont(font4);

        CellUtil.setCellValue(writer.getOrCreateCell(0,3),"时间");
        writer.getOrCreateCell(0,3).setCellStyle(cellStyle4);
        CellUtil.setCellValue(writer.getOrCreateCell(1,3),"小时车流");
        writer.getOrCreateCell(1,3).setCellStyle(cellStyle4);
    //        CellUtil.setCellValue(writer.getOrCreateCell(2,3),"时间");
    //        writer.getOrCreateCell(2,3).setCellStyle(cellStyle4);
    //        CellUtil.setCellValue(writer.getOrCreateCell(3,3),"速度");
    //        writer.getOrCreateCell(3,3).setCellStyle(cellStyle4);

        // 设置数据
        int startRow = 4;
        for (TrafficVo trafficVo:
             trafficVoList) {
            List<TrafficSpeedVo> trafficSpeedVos = trafficVo.getTrafficSpeedVoList();
            if(trafficSpeedVos != null){
                int speedLen = trafficSpeedVos.size();
                //只有一行数据是不能合并单元格的
                if(speedLen != 1) {
                    writer.merge(startRow,(startRow+speedLen )-1,0,0, trafficVo.getHourUpload()+":00",cellStyle4);
                    writer.merge(startRow,(startRow+speedLen)-1,1,1, trafficVo.getTotalFlow() / 5,cellStyle4);
                }
                //先屏蔽速度
//                for (int i = 0; i < speedLen; i++) {
//                    String uploadTime = DateUtil.format(trafficSpeedVos.get(i).getUploadTime(),"yyyy-MM-dd HH:mm");
//                    CellUtil.setCellValue(writer.getOrCreateCell(2,startRow+i),uploadTime);
//                    CellUtil.setCellValue(writer.getOrCreateCell(3,startRow+i),Math.abs(Integer.parseInt(trafficSpeedVos.get(i).getSpeed())));
//                    writer.getOrCreateCell(2,startRow+i).setCellStyle(cellStyle4);
//                    writer.getOrCreateCell(3,startRow+i).setCellStyle(cellStyle4);
//                    writer.getOrCreateRow(startRow+i).setHeightInPoints(15);
//                }
                startRow+=speedLen;
            }
        }

        writer.getOrCreateRow(3).setHeightInPoints(15);
        writer.getOrCreateRow(4).setHeightInPoints(15);
        writer.getOrCreateRow(5).setHeightInPoints(15);
        writer.getOrCreateRow(6).setHeightInPoints(15);

        for(int i=0; i < writer.getRowCount();i++){
            writer.getSheet().setDefaultColumnWidth(25);
        }

        //创建速度sheet
        Sheet sheet = writer.getWorkbook().createSheet("车速");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        CellUtil.setCellValue(cell,"时间");
        cell.setCellStyle(cellStyle4);
        Cell cell1 = row.createCell(1);
        CellUtil.setCellValue(cell1,"速度");
        cell1.setCellStyle(cellStyle4);
        // 设置数据
        int twoStartRow = 1;
        for (TrafficVo trafficVo: trafficVoList) {
            List<TrafficSpeedVo> trafficSpeedVos = trafficVo.getTrafficSpeedVoList();
            if(trafficSpeedVos != null){
                int speedLen = trafficSpeedVos.size();
                for (int i = 0; i < speedLen; i++) {
                    String uploadTime = DateUtil.format(trafficSpeedVos.get(i).getUploadTime(),"yyyy-MM-dd HH:mm");
                    Row row2 = sheet.createRow(twoStartRow);
                    Cell cell2 = row2.createCell(0);
                    CellUtil.setCellValue(cell2,uploadTime);
                    Cell cell3 = row2.createCell(1);
                    CellUtil.setCellValue(cell3,Math.abs(Integer.parseInt(trafficSpeedVos.get(i).getSpeed())));
                    cell2.setCellStyle(cellStyle4);
                    cell3.setCellStyle(cellStyle4);
                    twoStartRow++;
                }
            }
        }
        //渲染第二个sheet
        sheet.getWorkbook().write(response.getOutputStream());
        //渲染第一个sheet
        writer.getWorkbook().write(response.getOutputStream());
        writer.close();
    }

    /**
     * 洞外亮度导出Excel
     */
    public static void insideAndOutsideExportTemp(HttpServletResponse response,
                                                  String title,
                                                  String dateRange,List<InsideAndOutsideVo> insideAndOutsideVos) throws IOException {

        // 通过工具类创建writer
        ExcelWriter writer = ExcelUtil.getWriter("/tunnel/file/洞外亮度"+System.currentTimeMillis()+".xlsx");
        writer.renameSheet("洞外亮度");
        // 设置高速公路标题
        CellStyle cellStyle = writer.createCellStyle();
        Font font = writer.createFont();
        font.setBold(true);
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short)12);
        cellStyle = setDefaultStyle(cellStyle);
        cellStyle.setFont(font);
        writer.getOrCreateRow(0).setHeightInPoints(29.3f);
        writer.merge(0,0,0,3, title,cellStyle);
        // 设置时间段
        CellStyle cellStyle2 = writer.createCellStyle();
        Font font2 = writer.createFont();
        font2.setFontName("Times New Roman");
        font2.setFontHeightInPoints((short)12);
        cellStyle2 = setDefaultStyle(cellStyle2);
        cellStyle2.setFont(font2);
        writer.getOrCreateRow(1).setHeightInPoints(24.75f);
//        String dateRange = statisticsExcelVo.getStartDate().replace("-",".")+"-"+statisticsExcelVo.getEndDate().replace("-",".");
        writer.merge(1,1,0,3, dateRange,cellStyle2);
        // 标题
        // 耗电量
        CellStyle cellStyle3 = writer.createCellStyle();
        Font font3 = writer.createFont();
        font3.setBold(true);
        font3.setFontName("宋体");
        font3.setFontHeightInPoints((short)11);
        cellStyle3 = setDefaultStyle(cellStyle3);
        cellStyle3.setFont(font3);
        writer.getOrCreateRow(2).setHeightInPoints(15);
        writer.merge(2,2,0,1, "洞外亮度/cd·m⁻²",cellStyle3);
        writer.merge(2,2,2,3, "调光比例/%",cellStyle3);

        CellStyle cellStyle4 = writer.createCellStyle();
        Font font4 = writer.createFont();
        font4.setFontName("宋体");
        font4.setFontHeightInPoints((short)11);
        cellStyle4 = setDefaultStyle(cellStyle4);
        cellStyle4.setFont(font4);

        CellUtil.setCellValue(writer.getOrCreateCell(0,3),"时间");
        CellUtil.setCellValue(writer.getOrCreateCell(1,3),"亮度值");
        CellUtil.setCellValue(writer.getOrCreateCell(2,3),"时间");
        CellUtil.setCellValue(writer.getOrCreateCell(3,3),"调光比例值");
        int startRow = 4;
        int lightLen = insideAndOutsideVos.size();
        for (int i = 0; i < lightLen; i++) {
            CellUtil.setCellValue(writer.getOrCreateCell(0,startRow+i),insideAndOutsideVos.get(i).getUploadTime());
            CellUtil.setCellValue(writer.getOrCreateCell(1,startRow+i),insideAndOutsideVos.get(i).getOutsideLightValue());
            CellUtil.setCellValue(writer.getOrCreateCell(2,startRow+i),insideAndOutsideVos.get(i).getUploadTime());
            CellUtil.setCellValue(writer.getOrCreateCell(3,startRow+i),insideAndOutsideVos.get(i).getDimmingRadioValue());
            writer.getOrCreateCell(0,startRow+i).setCellStyle(cellStyle4);
            writer.getOrCreateCell(1,startRow+i).setCellStyle(cellStyle4);
            writer.getOrCreateCell(2,startRow+i).setCellStyle(cellStyle4);
            writer.getOrCreateCell(3,startRow+i).setCellStyle(cellStyle4);
        }

        writer.getOrCreateRow(3).setHeightInPoints(15);
        writer.getOrCreateRow(4).setHeightInPoints(15);
        writer.getOrCreateRow(5).setHeightInPoints(15);
        writer.getOrCreateRow(6).setHeightInPoints(15);

        for (int i = 0; i < 4; i++) {
            writer.getOrCreateCell(i,3).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,4).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,5).setCellStyle(cellStyle4);
            writer.getOrCreateCell(i,6).setCellStyle(cellStyle4);
        }

        for(int i=0; i < writer.getRowCount();i++){
            writer.getSheet().setDefaultColumnWidth(25);
        }
        writer.getWorkbook().write(response.getOutputStream());
        writer.close();
    }

    public  static void carbonExportTemp(HttpServletResponse response,
                                         String title,
                                         String dateRange,
            List<String> keys,
            LinkedHashMap<String,  Map<String, TunnelEleMeter>> tunnelEleMeters,
                                         List<TunnelDevice> tunnelDevices){
        // 通过工具类创建writer
        ExcelWriter writer = ExcelUtil.getWriter("/tunnel/file/能碳数据"+System.currentTimeMillis()+".xlsx");
        writer.renameSheet("能碳数据");
        // 设置高速公路标题
        CellStyle cellStyle = writer.createCellStyle();
        Font font = writer.createFont();
        font.setBold(true);
        font.setFontName("微软雅黑");
        font.setFontHeightInPoints((short)12);
        cellStyle = setDefaultStyle(cellStyle);
        cellStyle.setFont(font);
        writer.getOrCreateRow(0).setHeightInPoints(29.3f);
        if(CollectionUtils.isNotEmpty(tunnelDevices)) {
            writer.merge(0,0,0,2*tunnelDevices.size(), title,cellStyle);
            // 设置时间段
            CellStyle cellStyle2 = writer.createCellStyle();
            Font font2 = writer.createFont();
            font2.setFontName("Times New Roman");
            font2.setFontHeightInPoints((short)12);
            cellStyle2 = setDefaultStyle(cellStyle2);
            cellStyle2.setFont(font2);
            writer.getOrCreateRow(1).setHeightInPoints(24.75f);
//        String dateRange = statisticsExcelVo.getStartDate().replace("-",".")+"-"+statisticsExcelVo.getEndDate().replace("-",".");
            writer.merge(1,1,0,2*tunnelDevices.size(), dateRange,cellStyle2);
            // 标题
            // 耗电量
            CellStyle cellStyle3 = writer.createCellStyle();
            Font font3 = writer.createFont();
            font3.setBold(true);
            font3.setFontName("宋体");
            font3.setFontHeightInPoints((short)11);
            cellStyle3 = setDefaultStyle(cellStyle3);
            cellStyle3.setFont(font3);
            writer.merge(2,3,0,0,"时间",cellStyle3);
            int deviceLen = tunnelDevices.size();

            CellStyle cellStyle4 = writer.createCellStyle();
            Font font4 = writer.createFont();
            font4.setFontName("宋体");
            font4.setFontHeightInPoints((short)11);
            cellStyle4 = setDefaultStyle(cellStyle4);
            cellStyle4.setFont(font4);
            int startRow = 4;
            for (int i = 0; i < deviceLen; i++) {
                String deviceNum = tunnelDevices.get(i).getDeviceId();
                writer.merge(2,2,i*2+1,i*2+2,tunnelDevices.get(i).getDeviceType(),cellStyle3);
                CellUtil.setCellValue(writer.getOrCreateCell(i*2+1,3),"耗电量/kWh");
                writer.getOrCreateCell(i*2+1,3).setCellStyle(cellStyle3);
                CellUtil.setCellValue(writer.getOrCreateCell(i*2+2,3),"功率/kW");
                writer.getOrCreateCell(i*2+2,3).setCellStyle(cellStyle3);
                int rowIndex = keys.size();
                for (int j = 0; j < rowIndex; j++) {
                    if(i ==0){
                        CellUtil.setCellValue(writer.getOrCreateCell(0,startRow+j),
                                keys.get(j));
                        writer.getOrCreateCell(0,startRow+j).setCellStyle(cellStyle4);
                    }
                    CellUtil.setCellValue(writer.getOrCreateCell(i*2+1,startRow+j),
                            tunnelEleMeters.get(keys.get(j)).get(deviceNum).getQuantityEle());
                    writer.getOrCreateCell(i*2+1,startRow+j).setCellStyle(cellStyle4);
                    CellUtil.setCellValue(writer.getOrCreateCell(i*2+2,startRow+j),
                            tunnelEleMeters.get(keys.get(j)).get(deviceNum).getPower());
                    writer.getOrCreateCell(i*2+2,startRow+j).setCellStyle(cellStyle4);
                }
            }
        }

        for(int i=0; i < writer.getRowCount();i++){
            writer.getSheet().setDefaultColumnWidth(25);
        }
//        writer.getWorkbook().write(response.getOutputStream());
        writer.close();

    }
    /**
     * 设置单元格默认样式
     * @param cellStyle
     * @return
     */
    public static CellStyle setDefaultStyle(CellStyle cellStyle){
        //下边框
        cellStyle.setBorderBottom(BorderStyle.THIN);
        //左边框
        cellStyle.setBorderLeft(BorderStyle.THIN);
        //上边框
        cellStyle.setBorderTop(BorderStyle.THIN);
        //右边框
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        //垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }
    public static void main(String[] args) {
//        StatisticsExcelVo statisticsExcelVo = new StatisticsExcelVo();
//        statisticsExcelVo.setTitle("玉溪-楚雄高速公路双柏隧道（玉溪-楚雄）");
//        statisticsExcelVo.setStartDate("2023-12-30");
//        statisticsExcelVo.setEndDate("2024-01-05");
//        statisticsExcelVo.setActualPowerConsumption(1926126);
//        statisticsExcelVo.setOriginalPowerConsumption(3697056);
//        statisticsExcelVo.setTheoreticalPowerSaving(1770930);
//        statisticsExcelVo.setTheoreticalPowerSavingRate(new BigDecimal("-697771.74"));
//
//        statisticsExcelVo.setActualOperatingPower("276664");
//        statisticsExcelVo.setOriginalOperatingPower("154044");
//        statisticsExcelVo.setTheoreticalOperatingPowerReduction(122620);
//        statisticsExcelVo.setTheoreticalTotalPowerReduction(new BigDecimal("-2807728.27"));
//
//        statisticsExcelVo.setActualLightUpTime(318499);
//        statisticsExcelVo.setOriginalLightUpTime(616176);
//        statisticsExcelVo.setTheoreticalLightUpTimeReduction(297677);
//        statisticsExcelVo.setTheoreticalLightUpTimeReductionRate(new BigDecimal("48.32"));
//
//        statisticsExcelVo.setActualCarbonEmission(new BigDecimal("1094.999562"));
//        statisticsExcelVo.setOriginalCarbonEmission(new BigDecimal("2118.413088"));
//        statisticsExcelVo.setTheoreticalCarbonEmissionReduction(new BigDecimal("1023.41"));
//        statisticsExcelVo.setTheoreticalCarbonEmissionReductionRate(new BigDecimal("-782043"));
//        statisticsExportTemp(statisticsExcelVo);
//        String title = "玉溪-楚雄高速公路双柏隧道（玉溪-楚雄）";
//        String dateRange = "2023.12.30-2024.1.5";
//        List<TrafficVo> trafficVos = new ArrayList<>();
//        TrafficVo trafficVo = new TrafficVo();
//        trafficVo.setHourUpload("2024-01-01 01");
//        trafficVo.setTotalFlow(10);
//        List<TrafficSpeedVo> trafficSpeedVos = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            TrafficSpeedVo trafficSpeedVo = new TrafficSpeedVo();
//            trafficSpeedVo.setUploadHour("2024-01-01 01");
//            trafficSpeedVo.setSpeed(RandomUtil.randomInt(50,120));
//            String date = "2024-01-01 01:"+RandomUtil.randomInt(10,59);
//            trafficSpeedVo.setUploadTime(DateUtil.parse(date));
//            trafficSpeedVos.add(trafficSpeedVo);
//        }
//        trafficVo.setTrafficSpeedVoList(trafficSpeedVos);
//        trafficVos.add(trafficVo);
//        trafficFlowOrSpeedExportTemp(null,title,dateRange,trafficVos);

//        List<InsideAndOutsideVo> insideAndOutsideVos = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            InsideAndOutsideVo insideAndOutsideVo = new InsideAndOutsideVo();
//            insideAndOutsideVo.setUploadTime("2023-01-01 01:01");
//            insideAndOutsideVo.setOutsideLightValue(3);
//            insideAndOutsideVo.setDimmingRadioValue(70);
//            insideAndOutsideVos.add(insideAndOutsideVo);
//        }
//        String title = "玉溪-楚雄高速公路双柏隧道（玉溪-楚雄）";
//        String dateRange = "2023.12.30-2024.1.5";
//        insideAndOutsideExportTemp(null, title,dateRange,insideAndOutsideVos);
    }
}
