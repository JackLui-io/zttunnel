package com.scsdky.web.utils;

import java.math.BigDecimal;

/**
 * @author tubo
 * 地球坐标系(WGS - 84)转火星坐标系(GCJ)
 * @date 2023/10/08
 */
public class LngLatConvertUtil {

    private static final double PI = 3.14159265358979324;

    private static final double a = 6378245.0;

    private static final double EE = 0.00669342162296594323;


    /**
     * 地球坐标系(WGS-84)转火星坐标系(GCJ)
     *
     * @param wgLon 地球坐标系(WGS-84) 经度
     * @param wgLat 地球坐标系(WGS-84) 纬度
     * @return 火星坐标系(GCJ) 数组 0 经度；1 纬度
     */
    public static double[] transform(BigDecimal wgLon, BigDecimal wgLat) {
        return transform(wgLon.doubleValue(), wgLat.doubleValue());
    }

    /**
     * 地球坐标系(WGS-84)转火星坐标系(GCJ)
     *
     * @param wgLon 地球坐标系(WGS-84) 经度
     * @param wgLat 地球坐标系(WGS-84) 纬度
     * @return 火星坐标系(GCJ) 数组 0 经度；1 纬度
     */
    public static double[] transform(double wgLon, double wgLat) {
        double[] result = new double[2];

        if (outOfChina(wgLon, wgLat)) {
            result[0] = wgLon;
            result[1] = wgLat;
            return result;
        }
        double dLat = transformLat(wgLon - 105.0, wgLat - 35.0);
        double dLon = transformLon(wgLon - 105.0, wgLat - 35.0);
        double radLat = wgLat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - EE)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        result[0] = wgLon + dLon;
        result[1] = wgLat + dLat;
        return result;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 在中国范围内
     *
     * @param lon 经度
     * @param lat 纬度
     * @return true 出中国
     */
    private static boolean outOfChina(double lon, double lat) {
        if ((lon < 72.004 || lon > 137.8347) && (lat < 0.8293 || lat > 55.8271)) {
            return true;
        } else {
            return false;
        }
    }

}
