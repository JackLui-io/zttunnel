package com.scsdky.web.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.web.domain.TunnelLongitudeLatitude;
import com.scsdky.web.domain.TunnelName;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.service.TunnelLongitudeLatitudeService;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelNameService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * kml deal with
 * @author tubo
 */
@Component
public class KmlUtil {

    /**
     * 编号名称（迭代器会重置数据，需要使用常量先保存）
     */
    private static String name;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelLongitudeLatitudeService tunnelLongitudeLatitudeService;

    public static void main(String[] args) throws Exception {
        //MultipartFile file 如果上传的文件类型为MultipartFile，使用以下方式转换流
        //InputStream inputStream = file.getInputStream();
        KmlUtil test = new KmlUtil();
        //File file = new File("E:/test.kml");
        File file = new File("C:/Users/tubo/Desktop/工作文档/隧道系统/玉楚高速矢量数据.kml");
        InputStream in = new FileInputStream(file);
        test.parseXmlWithDom4j(in);
    }

    public void parseXmlWithDom4j(InputStream input) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(input);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        //获取kml文件的根结点
        Element root = document.getRootElement();
        listNodes(root);
    }

    /**
     * 遍历当前节点下的全部节点
     * @param node
     */
    @Transactional(rollbackFor = Exception.class)
    public void listNodes(Element node) {

        if ("name".equals(node.getName())) {
            name = node.getTextTrim();
        }
        if ("coordinates".equals(node.getName())) {
            //存储隧道的经纬度
            List<TunnelLongitudeLatitude> tunnelLongitudeLatitudes = new ArrayList<>();
            //通过隧道名称查询
            TunnelNameResult tunnel = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, name));
            if(tunnel != null ) {
                String s = node.getTextTrim();
                String[] s1 = s.split(" ");
                for (String s2 : s1) {
                    TunnelLongitudeLatitude tunnelLongitudeLatitude = new TunnelLongitudeLatitude();
                    String[] str = s2.split(",");
                    //经度
                    BigDecimal d0 = new BigDecimal(str[0]);
                    //纬度
                    BigDecimal d1 = new BigDecimal(str[1]);
                    //海拔
                    Double d2 = new Double(str[2]);

                    double[] douLonLat = LngLatConvertUtil.transform(d0, d1);

                    tunnelLongitudeLatitude.setLongitude(String.valueOf(douLonLat[0]));
                    tunnelLongitudeLatitude.setLatitude(String.valueOf(douLonLat[1]));
                    tunnelLongitudeLatitude.setAltitude(String.valueOf(d2));
                    tunnelLongitudeLatitude.setTunnelId(tunnel.getId());
                    tunnelLongitudeLatitude.setTunnelName(tunnel.getTunnelName());
                    tunnelLongitudeLatitudes.add(tunnelLongitudeLatitude);
                }
                //删除之前的kml文件
                tunnelLongitudeLatitudeService.remove(new LambdaQueryWrapper<TunnelLongitudeLatitude>().eq(TunnelLongitudeLatitude::getTunnelId,tunnel.getId()));
                //保存经纬度数据
                tunnelLongitudeLatitudeService.saveBatch(tunnelLongitudeLatitudes);
            }else{
                throw new BaseException("请跟新建隧道的隧道名称保持一致或者隧道不存在！");
            }
        }
        //同一时候迭代当前节点以下的全部子节点
        //使用递归
        Iterator<Element> iterator = node.elementIterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            listNodes(e);
        }
    }

}
