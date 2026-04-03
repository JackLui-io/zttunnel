package com.scsdky.web.constants;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ElectricMeterCalculator {

    // 模拟电表地址对应的值（实际应用中应从数据库或其他数据源获取）
    private static Map<Integer, Double> meterValues = new HashMap<>();

    static {
        // 初始化模拟数据 - 这里只是示例，实际值需要替换
        for (int i = 1; i <= 240; i++) {
            meterValues.put(i, (double) (i * 10)); // 假设每个电表的值是地址号*10
        }
    }

    public static void calculateRightLineSums() {
        // 右线各段的地址集合
        int[] mainMetersR = {10, 30, 50, 70, 90, 110, 130, 150, 170, 190, 210, 230}; // 回路总表
        int[] rightEntryStrengthenR = {1, 21, 41, 61, 81, 101, 121, 141, 161, 181, 201, 221}; // 右侧入口加强
        int[] leftEntryStrengthenR = {2, 22, 42, 62, 82, 102, 122, 142, 162, 182, 202, 222}; // 左侧入口加强
        int[] rightTransitionStrengthenR = {3, 23, 43, 63, 83, 103, 123, 143, 163, 183, 203, 223}; // 右侧过渡加强
        int[] leftTransitionStrengthenR = {4, 24, 44, 64, 84, 104, 124, 144, 164, 184, 204, 224}; // 左侧过渡加强
        int[] basic1R = {5, 25, 45, 65, 85, 105, 125, 145, 165, 185, 205, 225}; // 基本1
        int[] basic2R = {6, 26, 46, 66, 86, 106, 126, 146, 166, 186, 206, 226}; // 基本2
        int[] exitR = {7, 27, 47, 67, 87, 107, 127, 147, 167, 187, 207, 227}; // 出口
        int[] emergencyR = {8, 28, 48, 68, 88, 108, 128, 148, 168, 188, 208, 228}; // 应急


        // 左线各段的地址集合
        int[] mainMetersL = {20, 40, 60, 80, 100, 120, 140, 160, 180, 200, 220, 240}; // 回路总表
        int[] rightEntryStrengthenL = {11, 31, 51, 71, 91, 111, 131, 151, 171, 191, 211, 231}; // 右侧入口加强
        int[] leftEntryStrengthenL = {12, 32, 52, 72, 92, 112, 132, 152, 172, 192, 212, 232}; // 左侧入口加强
        int[] rightTransitionStrengthenL = {13, 33, 53, 73, 93, 113, 133, 153, 173, 193, 213, 233}; // 右侧过渡加强
        int[] leftTransitionStrengthenL = {14, 34, 54, 74, 94, 114, 134, 154, 174, 194, 214, 234}; // 左侧过渡加强
        int[] basic1L = {15, 35, 55, 75, 95, 115, 135, 155, 175, 195, 215, 235}; // 基本1
        int[] basic2L = {16, 36, 56, 76, 96, 116, 136, 156, 176, 196, 216, 236}; // 基本2
        int[] exitL = {17, 37, 57, 77, 97, 117, 137, 157, 177, 197, 217, 237}; // 出口
        int[] emergencyL = {18,38, 58, 78, 98, 118, 138, 158, 178, 198, 218, 238}; // 应急

        // 计算右线各段总和
        double mainSum = calculateSum(mainMetersR);
        double rightEntrySum = calculateSum(rightEntryStrengthenR);
        double leftEntrySum = calculateSum(leftEntryStrengthenR);
        double rightTransitionSum = calculateSum(rightTransitionStrengthenR);
        double leftTransitionSum = calculateSum(leftTransitionStrengthenR);
        double basic1Sum = calculateSum(basic1R);
        double basic2Sum = calculateSum(basic2R);
        double exitSum = calculateSum(exitR);
        double emergencySum = calculateSum(emergencyR);


        // 计算左线线各段总和
        double mainSumL = calculateSum(mainMetersL);
        double rightEntrySumL = calculateSum(rightEntryStrengthenL);
        double leftEntrySumL = calculateSum(leftEntryStrengthenL);
        double rightTransitionSumL = calculateSum(rightTransitionStrengthenL);
        double leftTransitionSumL = calculateSum(leftTransitionStrengthenL);
        double basic1SumL = calculateSum(basic1L);
        double basic2SumL = calculateSum(basic2L);
        double exitSumL = calculateSum(exitL);
        double emergencySumL = calculateSum(emergencyL);

        // 输出结果
        System.out.println("右线各段电表数据总和：");
        System.out.println("回路总表总和: " + mainSum);
        System.out.println("右侧入口加强总和: " + rightEntrySum);
        System.out.println("左侧入口加强总和: " + leftEntrySum);
        System.out.println("右侧过渡加强总和: " + rightTransitionSum);
        System.out.println("左侧过渡加强总和: " + leftTransitionSum);
        System.out.println("基本1总和: " + basic1Sum);
        System.out.println("基本2总和: " + basic2Sum);
        System.out.println("出口总和: " + exitSum);
        System.out.println("应急总和: " + emergencySum);

        // 计算右线所有电表总和
        double totalSum = mainSum + rightEntrySum + leftEntrySum + rightTransitionSum
                + leftTransitionSum + basic1Sum + basic2Sum + exitSum + emergencySum;
        System.out.println("\n右线所有电表总和: " + totalSum);
    }


    // 计算给定地址数组的总和
    private static double calculateSum(int[] meterAddresses) {
        double sum = 0;
        for (int address : meterAddresses) {
            Double value = meterValues.get(address);
            if (value != null) {
                sum += value;
            } else {
                System.out.println("警告: 地址 " + address + " 没有对应的值");
            }
        }
        return sum;
    }
}
