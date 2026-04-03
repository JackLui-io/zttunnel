package com.scsdky.web.utils;

import com.alibaba.excel.util.StringUtils;

/**
 * @author tubo
 * @date 2024/08/23
 */
public class ConvertBit {

    /**
     * 根据具体值和位计算响应结果
     * @param num 需要计算的数值
     * @param bit 位运算---移动几位
     * @return
     */
    public static int computerBit(int num,int bit) {
        return num & (1 << bit);
    }

    /**
     * 十进制转16进制
     */
    public static String decToHex(int decimalValue){
        // 假设这是从Excel单元格G2中获取的值
        // 将十进制数转换为十六进制字符串
        String hexString = Integer.toHexString(decimalValue).toUpperCase();
        System.out.println("十进制数 " + decimalValue + " 转换为十六进制数是: " + hexString);
        return hexString;
    }

    /**
     * 16进制转10进制
     * @param hexString
     * @return
     */
    public static int hexToDec(String hexString){
        // 将拼接后的十六进制字符串转换为十进制数
        return Integer.parseInt(hexString, 16);
    }


    public static String bigAndSmall(int deviceNum){

        String big;
        String small;
        int bigNum = 0;
        int smallNum = 0;

        // 1. 十进制 → 十六进制（大写，无前缀）
        //16进制 E803B6
        String hexString = ConvertBit.decToHex(deviceNum);

        // 2. 去掉前导的 '1'（如果存在）
        if(hexString.startsWith("1") && (hexString.length() >2 && hexString.length() <=4) ) {
            bigNum = 1;
            String remainingHex = hexString.startsWith("1") ? hexString.substring(1) : hexString;

            if(StringUtils.isNotBlank(remainingHex)) {
                // 3. 剩余部分 → 十进制
                smallNum = Integer.parseInt(remainingHex, 16);
            }
            //十进制转换为十六进制如果是008+008这种，肯定是三位，如果16进制是三位，则第一位为一组，后两位为一组；//如果16进制是四位，则每两位为一组
        }else if(hexString.length() == 3 || hexString.length() == 4) {
            String  result = hexString.substring(hexString.length() - 2);
            big = hexString.substring(0, hexString.indexOf(result));
            small = result;
            //10进制
            bigNum = ConvertBit.hexToDec(big);
            smallNum = ConvertBit.hexToDec(small);
            //如果16进制长度等于五位，则取后三位为一组，其余的为一组
        }else if (hexString.length() >= 5 ) {
            String  result = hexString.substring(hexString.length() - 4);

            big = hexString.substring(0, hexString.indexOf(result));
            small = result;
            //10进制
            bigNum = ConvertBit.hexToDec(big);
            smallNum = ConvertBit.hexToDec(small);
        }else{
            smallNum = ConvertBit.hexToDec(hexString);
        }

        return bigNum + "+" + smallNum;
    }


    /**
     * 将14+28转为10进制
     * @param deviceNum
     * @return
     */
    public static int deviceNumConvertValue(String deviceNum){
        String[] bigNumAndSmallNum = deviceNum.split("\\+");

        String bigNum = bigNumAndSmallNum[0];
        String smallNum = bigNumAndSmallNum[1];

        String hexString = ConvertBit.decToHex(Integer.parseInt(bigNum));
        String hexString2 = ConvertBit.decToHex(Integer.parseInt(smallNum));

        String num1 = String.format("%04X", Integer.parseInt(hexString, 16));
        String num2 = String.format("%04X", Integer.parseInt(hexString2, 16));
        return ConvertBit.hexToDec(num1 + num2);
    }


    public static String valueConvertToDeviceNum(int value) {
        // 1. 将值转换为8位十六进制字符串
        String hex = Integer.toHexString(value).toUpperCase();
        hex = String.format("%8s", hex).replace(' ', '0');

        // 2. 分割为前4位和后4位（逆向原始方法的拼接）
        String part1 = hex.substring(0, 4); // 对应原始bigNum
        String part2 = hex.substring(4);    // 对应原始smallNum

        // 3. 转换为十进制
        int bigNum = Integer.parseInt(part1, 16);
        int smallNum = Integer.parseInt(part2, 16);

        // 4. 格式化输出
        return bigNum + "+" + String.format("%03d", smallNum);
    }
    public static void main(String[] args) {
        System.out.println(bigAndSmall(22));
        //System.out.println(deviceNumConvertValue("0+22"));

    }
}
