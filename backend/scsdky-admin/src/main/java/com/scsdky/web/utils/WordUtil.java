package com.scsdky.web.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.deepoove.poi.XWPFTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Title: WordUtil
 * @Description: Word工具类
 * @date 2020/10/9 9:09
 */

public class WordUtil {

    private static Logger logger = LoggerFactory.getLogger(WordUtil.class);

    public static void main(String[] args) throws IOException {
        Map<String, Object> dates = new HashMap<>();
//        dates.put("year","2023");
//        dates.put("month","09");
//        dates.put("day","26");
//        dates.put("enterpriseName","中国建筑");
//        dates.put("mountNum","172.18");
//        dates.put("num","172");
        String jsonStr = "{\n" +
                "        \"gcgk\": {\n" +
                "            \"route\": \"双柏隧道\",\n" +
                "            \"tunnelName\": \"玉溪到楚雄\",\n" +
                "            \"mileage\": 200,\n" +
                "            \"list\": [\n" +
                "                {\n" +
                "                    \"deviceNum\": \"k147+814\",\n" +
                "                    \"onLineTime\": \"2023-12-27T19:59:51.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:01:57.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:01:59.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:00.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:01.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:03.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:04.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:05.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:06.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceNum\": \"kXXX+XXX\",\n" +
                "                    \"onLineTime\": \"2024-01-04T18:02:08.000+08:00\",\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"mileage\": 100\n" +
                "                }\n" +
                "            ],\n" +
                "            \"deviceTypeList\": null,\n" +
                "            \"loopTypeList\": null\n" +
                "        },\n" +
                "        \"sbgl\": {\n" +
                "            \"route\": \"双柏隧道\",\n" +
                "            \"tunnelName\": \"玉溪到楚雄\",\n" +
                "            \"mileage\": null,\n" +
                "            \"list\": [\n" +
                "                {\n" +
                "                    \"deviceType\": \"雷达型调光控制器（A2）\",\n" +
                "                    \"deviceBreakdown\": 0,\n" +
                "                    \"deviceOffline\": 1,\n" +
                "                    \"deviceOnline\": 0,\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceType\": \"洞外亮度检测器(A6)\",\n" +
                "                    \"deviceBreakdown\": 0,\n" +
                "                    \"deviceOffline\": 0,\n" +
                "                    \"deviceOnline\": 1,\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"deviceType\": \"边缘控制器（A1）\",\n" +
                "                    \"deviceBreakdown\": 0,\n" +
                "                    \"deviceOffline\": 1,\n" +
                "                    \"deviceOnline\": 9,\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"deviceTypeList\": null,\n" +
                "            \"loopTypeList\": null\n" +
                "        },\n" +
                "        \"sjtj\": {\n" +
                "            \"route\": \"双柏隧道\",\n" +
                "            \"tunnelName\": \"玉溪到楚雄\",\n" +
                "            \"mileage\": null,\n" +
                "            \"list\": [\n" +
                "                {\n" +
                "                    \"avgTrafficFlow\": 501.05,\n" +
                "                    \"avgOutside\": 2004.55,\n" +
                "                    \"avgLight\": 12.53,\n" +
                "                    \"theoreticalPowerSavingRate\": 47.88,\n" +
                "                    \"theoreticalCarbonEmissionReduction\": 30.26,\n" +
                "                    \"theoreticalLightUpTimeReduction\": 48.31,\n" +
                "                    \"avgDimmingRadio\": 13,\n" +
                "                    \"theoreticalTotalPowerReduction\": 30.26,\n" +
                "                    \"tunnelName\": \"玉溪到楚雄\",\n" +
                "                    \"avgSpeed\": 101.01\n" +
                "                }\n" +
                "            ],\n" +
                "            \"deviceTypeList\": null,\n" +
                "            \"loopTypeList\": null\n" +
                "        },\n" +
                "        \"ycsj\": {\n" +
                "            \"route\": \"双柏隧道\",\n" +
                "            \"tunnelName\": \"玉溪到楚雄\",\n" +
                "            \"mileage\": null,\n" +
                "            \"list\": [\n" +
                "                {\n" +
                "                    \"入口段1（R1)\": 17756,\n" +
                "                    \"基本段1（R1)\": 338,\n" +
                "                    \"出口段1（R1)\": 294,\n" +
                "                    \"边缘控制器（A1）\": 1,\n" +
                "                    \"雷达型调光控制器（A2）\": 1,\n" +
                "                    \"洞外亮度检测器(A6)\": 0,\n" +
                "                    \"过渡段1（R1)\": 351\n" +
                "                }\n" +
                "            ],\n" +
                "            \"deviceTypeList\": [\n" +
                "                \"雷达型调光控制器（A2）\",\n" +
                "                \"洞外亮度检测器(A6)\",\n" +
                "                \"边缘控制器（A1）\"\n" +
                "            ],\n" +
                "            \"loopTypeList\": [\n" +
                "                \"入口段1（R1)\",\n" +
                "                \"出口段1（R1)\",\n" +
                "                \"过渡段1（R1)\",\n" +
                "                \"基本段1（R1)\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"end\": \"该路段null到null节约电量为1679142kwh、减少成本支出10万元，减少二氧化碳减排量970.819002\\n该路段共计设备12台，灯具156个，健康程度67%，预计下次预计维护时间为2024年01月13日。\"\n" +
                "    }";
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);


        String templatePath = "C:/Users/tubo/Desktop/报表.docx";
        String fileDir = "E:/report/template";
        String fileName = "报表";

        String wordPath = WordUtil.createWord(templatePath, fileDir, fileName, dates);
        System.out.println("生成文档路径：" + wordPath);
    }


    /**
     * 根据模板填充内容生成word
     * 调用方法参考下面的main方法，详细文档参考官方文档
     * Poi-tl模板引擎官方文档：http://deepoove.com/poi-tl/
     *
     * @param templatePath word模板文件路径
     * @param fileDir      生成的文件存放地址
     * @param fileName     生成的文件名,不带格式。假如要生成abc.docx，则fileName传入abc即可
     * @param dates        替换的参数集合
     * @return 生成word成功返回生成的文件的路径，失败返回空字符串
     */
    public static String createWord(String templatePath, String fileDir, String fileName, Map<String, Object> dates) throws IOException {
        Assert.notNull(templatePath, "word模板文件路径不能为空");
        Assert.notNull(fileDir, "生成的文件存放地址不能为空");
        Assert.notNull(fileName, "生成的文件名不能为空");

        // 生成的word格式
        String formatSuffix = ".docx";
        // 拼接后的文件名
        fileName = fileName + formatSuffix;

        // 生成的文件的存放路径
        if (!fileDir.endsWith("/")) {
            fileDir = fileDir + File.separator;
        }

        File dir = new File(fileDir);
        if (!dir.exists()) {
            logger.info("生成word数据时存储文件目录{}不存在,为您创建文件夹!", fileDir);
            dir.mkdirs();
        }
        File doc = new File(fileDir + fileName);
        if (!doc.exists()) {
            logger.info("生成word数据时存储文件目录{}不存在,为您创建文件夹!", fileDir);
            doc.createNewFile();
        }

        String filePath = fileDir + fileName;
        // 读取模板templatePath并将dates的内容填充进模板，即编辑模板+渲染数据
        XWPFTemplate template = XWPFTemplate.compile(templatePath).render(dates);
        try {
            // 将填充之后的模板写入filePath
            template.writeToFile(filePath);
            template.close();
        } catch (Exception e) {
            logger.error("生成word异常", e);
            e.printStackTrace();
            return "";
        }
        return filePath;
    }


    /**
     * @param jsonStr  需要的json数据
     * @param document 文档
     */
    public static void setTable(String jsonStr, XWPFDocument document) {
        //保证jsonstr的顺序
        LinkedHashMap linkedHashMap = JSONObject.parseObject(jsonStr, LinkedHashMap.class, Feature.OrderedField);
        JSONObject jsonObject = new JSONObject(true);
        jsonObject.putAll(linkedHashMap);

        List<XWPFTable> tables = document.getTables();

        for (int k = 0; k < tables.size(); k++) {
            XWPFTable tab = tables.get(k);
            CTTblPr tblpro = tab.getCTTbl().getTblPr();
            CTTblBorders borders = tblpro.addNewTblBorders();
            borders.addNewBottom().setVal(STBorder.SINGLE);
            //设置左侧无边框
            borders.addNewLeft().setVal(STBorder.SINGLE);
            borders.addNewRight().setVal(STBorder.SINGLE);
            borders.addNewTop().setVal(STBorder.SINGLE);
            borders.addNewInsideH().setVal(STBorder.SINGLE);
            borders.addNewInsideV().setVal(STBorder.SINGLE);
            if (k == 0) {
                JSONObject gcgk = jsonObject.getJSONObject("gcgk");
                JSONArray tableData = gcgk.getJSONArray("list");

                if (CollectionUtils.isNotEmpty(tableData)) {
                    for (int i = 0; i < tableData.size(); i++) {
                        XWPFTableRow xwpfTableRow = tab.insertNewTableRow(i + 2);
                        Map<String, Object> o = tableData.getJSONObject(i);
                        if(i == 0 ){
                            xwpfTableRow.createCell().setText(String.valueOf(gcgk.get("route")));
                            xwpfTableRow.createCell().setText(String.valueOf(gcgk.get("mileage")));
                        }else{
                            xwpfTableRow.createCell();
                            xwpfTableRow.createCell();
                        }
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("tunnelName")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("deviceNum")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("mileage")));
                        xwpfTableRow.createCell().setText(getTime((Long) o.get("onLineTime")));
                    }
                    mergeCellsVertically(tab, 0, 2, tableData.size() + 1);
                    mergeCellsVertically(tab, 1, 2, tableData.size() + 1);
                }
                logger.info("Word table filled successfully");
            }

            if (k == 1) {
                JSONObject gcgk = jsonObject.getJSONObject("sbgl");
                JSONArray tableData = gcgk.getJSONArray("list");
                if (CollectionUtils.isNotEmpty(tableData)) {
                    for (int i = 0; i < tableData.size(); i++) {
                        XWPFTableRow xwpfTableRow = tab.insertNewTableRow(i + 1);
                        Map<String, Object> o = tableData.getJSONObject(i);
                        xwpfTableRow.createCell().setText(String.valueOf(gcgk.get("route")));
                        xwpfTableRow.createCell().setText(String.valueOf(gcgk.get("tunnelName")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("deviceType")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("deviceOnline")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("deviceBreakdown")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("deviceOffline")));
                    }
                    mergeCellsVertically(tab, 0, 1, tableData.size());
                    mergeCellsVertically(tab, 1, 1, tableData.size());
                    logger.info("Word table filled successfully");
                }
            }

            if (k == 2) {
                JSONObject sjtj = jsonObject.getJSONObject("sjtj");
                JSONArray tableData = sjtj.getJSONArray("list");
                if (CollectionUtils.isNotEmpty(tableData)) {
                    for (int i = 0; i < tableData.size(); i++) {
                        XWPFTableRow xwpfTableRow = tab.insertNewTableRow(i + 2);
                        Map<String, Object> o = tableData.getJSONObject(i);
                        xwpfTableRow.createCell().setText(String.valueOf(sjtj.get("route")));
                        xwpfTableRow.createCell().setText(String.valueOf(sjtj.get("tunnelName")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("theoreticalPowerSavingRate")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("theoreticalCarbonEmissionReduction")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("theoreticalTotalPowerReduction")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("theoreticalLightUpTimeReduction")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("avgTrafficFlow")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("avgSpeed")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("avgOutside")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("avgLight")));
                        xwpfTableRow.createCell().setText(String.valueOf(o.get("avgDimmingRadio")));
                    }
                    mergeCellsVertically(tab, 0, 2, tableData.size());
                    mergeCellsVertically(tab, 1, 2, tableData.size());
                    logger.info("Word table filled successfully");
                }
            }

            if (k == 3) {
                JSONObject ycsj = jsonObject.getJSONObject("ycsj");
                JSONArray tableData = ycsj.getJSONArray("list");

                if (CollectionUtils.isNotEmpty(tableData)) {
                    for (int i = 0; i < tableData.size(); i++) {
                        XWPFTableRow xwpfTableRow = tab.insertNewTableRow(i + 2);
                        Map<String, Object> o = tableData.getJSONObject(i);
                        xwpfTableRow.createCell().setText(String.valueOf(ycsj.get("route")));
                        xwpfTableRow.createCell().setText(String.valueOf(ycsj.get("tunnelName")));
                        List<XWPFTableCell> tableCells = tab.getRow(1).getTableCells();
                        //根据第二行的设备类型来判断
                        for (int i1 = 2; i1 < tableCells.size(); i1++) {
                            Object o1 = o.get(tableCells.get(i1).getText());
                            if(o1 != null ) {
                                xwpfTableRow.createCell().setText(String.valueOf(o1));
                            }else{
                                xwpfTableRow.createCell().setText("0");
                            }
                        }
                    }
                }
                logger.info("Word table filled successfully");
            }

        }
    }

    private static String getTime(long timestamp){
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(date);
        return formattedTime;
    }

    /**
     * word单元格列合并
     *
     * @param table    表格
     * @param col      合并行所在列
     * @param startRow 开始行
     * @param endRow   结束行
     */
    public static void mergeCellsVertically(XWPFTable table, int col, int startRow, int endRow) {
        for (int i = startRow; i <= endRow; i++) {
            XWPFTableCell cell = table.getRow(i).getCell(col);
            if (i == startRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
        //设置垂直居中
        table.getRow(startRow).getCell(col).setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
        //设置水平居中
        table.getRow(startRow).getCell(col).getParagraphArray(0).setAlignment(ParagraphAlignment.CENTER);
    }

    /**
     * word单元格行合并
     *
     * @param table     表格
     * @param row       合并列所在行
     * @param startCell 开始列
     * @param endCell   结束列
     */
    public static void mergeCellsHorizontal(XWPFTable table, int row, int startCell, int endCell) {
        for (int i = startCell; i <= endCell; i++) {
            XWPFTableCell cell = table.getRow(row).getCell(i);
            if (i == startCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }
}
