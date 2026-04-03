package com.scsdky.web.controller.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelNameResultExcel;
import com.scsdky.web.service.TunnelEdgeComputingTerminalService;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.utils.ConvertBit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tubo
 * 设备监听，如果多个实体，就监听多个类，创建相同的类
 * @date 2024/01/25
 */
@Slf4j
public class TunnelNameListener extends AnalysisEventListener<TunnelNameResultExcel> {
    /**
     *单次缓存量为1000
     */
    private final int BATCH_SIZE = 1000;

    /**
     * 临时存储List
     */
    List<TunnelNameResultExcel> cacheData = new ArrayList<>();
    private TunnelNameResultService tunnelNameResultService;
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    public TunnelNameListener(TunnelNameResultService tunnelNameResultService,TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService){
        this.tunnelNameResultService = tunnelNameResultService;
        this.tunnelEdgeComputingTerminalService = tunnelEdgeComputingTerminalService;
    }
    @Override
    public void invoke(TunnelNameResultExcel data, AnalysisContext analysisContext) {
        cacheData.add(data);
        if (cacheData.size() >= BATCH_SIZE){
            saveData();
            //每批存储完成后清空list
            cacheData.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (cacheData.size() > 0){
            saveData();
        }
    }

    /**
     * 加入数据库
     */
    private void saveData(){
        //List<TunnelNameResult> devices = DozerUtils.mapList(dozerMapper,cacheData,TunnelNameResult.class);
        //构建数据
        //处理具体的逻辑
        //1.将数据存到我们自己的库中
        //2.将数据存到中间服务器表中
        //清理map中的内容
        cacheData.forEach(this::buildTunnelName);

    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            int row = excelDataConvertException.getRowIndex()+ 1;
            int column = excelDataConvertException.getColumnIndex()+1;
            log.error(excelDataConvertException.getMessage());
            throw new RuntimeException("第"+row+"行，第"+column+"列解析异常，请正确填写");
        }
    }


    private void buildTunnelName(TunnelNameResultExcel tunnelNameResultExcel) {
        //处理TunnelNameResultExcel 中的线路、隧道、隧道方向，然后拆分成三个对象,处理成父子关系
        //存储线路级别
        //线路名称
        String lineName = tunnelNameResultExcel.getLineName();
        //线路里程
        String lineMileage = tunnelNameResultExcel.getLineMileage();
        //判断当前线路是否存在，存在则不添加
        TunnelNameResult tunnelLineName = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, lineName));
        Long lineId;
        if(tunnelLineName == null ){
            TunnelNameResult tunnelNameResultForLine = new TunnelNameResult();
            tunnelNameResultForLine.setTunnelName(lineName);
            tunnelNameResultForLine.setTunnelMileage(new BigDecimal(lineMileage));
            //TODO 暂时设置为西南运营中心为最顶级，后面要修改最顶级的内容
            tunnelNameResultForLine.setParentId(1L);
            tunnelNameResultForLine.setLevel(2);
            tunnelNameResultService.save(tunnelNameResultForLine);
            lineId = tunnelNameResultForLine.getId();
            tunnelLineName = tunnelNameResultForLine;
        }else{
            lineId = tunnelLineName.getId();
        }
        //存储隧道级
        //隧道里程
        String lineMileageTunnel = tunnelNameResultExcel.getLineMileageTunnel();
        //隧道名称
        String tunnelName = tunnelNameResultExcel.getTunnelName();
        //判断当前隧道是否存在，存在则不添加
        TunnelNameResult tunnelNameExist = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelName));
        //
        Long tunnelId;
        if(tunnelNameExist == null) {
            TunnelNameResult tunnelNameResultForTunnel = new TunnelNameResult();
            tunnelNameResultForTunnel.setTunnelName(tunnelName);
            tunnelNameResultForTunnel.setTunnelMileage(new BigDecimal(lineMileageTunnel));
            tunnelNameResultForTunnel.setParentId(lineId);
            tunnelNameResultForTunnel.setLevel(3);
            tunnelNameResultService.save(tunnelNameResultForTunnel);
            tunnelId = tunnelNameResultForTunnel.getId();
            tunnelNameExist = tunnelNameResultForTunnel;
        }else{
            tunnelId = tunnelNameExist.getId();
        }

        TunnelNameResult tunnelNameDirection = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>()
                .eq(TunnelNameResult::getParentId, tunnelNameExist.getId())
                .eq(TunnelNameResult::getTunnelName, tunnelNameResultExcel.getDirection()));

        if(tunnelNameDirection == null ){
            //存储方向级
            //判断方向级是否存在
            TunnelNameResult tunnelNameResult = TunnelNameResult.builder()
                    .tunnelName(tunnelNameResultExcel.getDirection())
                    .tunnelMileage(new BigDecimal(tunnelNameResultExcel.getTunnelMileage()))
                    .parentId(tunnelId)
//                    .inMileageNum(tunnelNameResultExcel.getInMileageNum())
//                    .outMileageNum(tunnelNameResultExcel.getOutMileageNum())
//                    .zone(tunnelNameResultExcel.getZone())
//                    .zoneNum(tunnelNameResultExcel.getZoneNum())
//                    .loopNumber(tunnelNameResultExcel.getLoopNumber())
//                    .designOperatingPowerR1(tunnelNameResultExcel.getDesignOperatingPowerR1())
//                    .designOperatingPowerTotal(Integer.valueOf(tunnelNameResultExcel.getDesignOperatingPowerTotal()))
//                    .kBrightnessReduction(new BigDecimal(tunnelNameResultExcel.getKBrightnessReduction()))
//                    .coeffL(new BigDecimal(tunnelNameResultExcel.getCoeffL()))
//                    .l20Design(Integer.valueOf(tunnelNameResultExcel.getL20Design()))
//                    .l20DayPreMax(Integer.valueOf(tunnelNameResultExcel.getL20DayPreMax()))
//                    .l20DayPreMin(Integer.valueOf(tunnelNameResultExcel.getL20DayPreMin()))
//                    .vRange(tunnelNameResultExcel.getVRange())
//                    .l20Day(Integer.valueOf(tunnelNameResultExcel.getL20Day()))
//                    .tNightDay(Integer.valueOf(tunnelNameResultExcel.getTNightDay()))
//                    .tDayNight(Integer.valueOf(tunnelNameResultExcel.getTDayNight()))
//                    .lin(Integer.valueOf(tunnelNameResultExcel.getLin()))
//                    .carbonEmissionFactor(new BigDecimal(tunnelNameResultExcel.getCarbonEmissionFactor()))
//                    .equivalentTreeConstant(new BigDecimal(tunnelNameResultExcel.getEquivalentTreeConstant()))
//                    .carbonReductionCoalEquivalentConstant(Integer.valueOf(tunnelNameResultExcel.getCarbonReductionCoalEquivalentConstant()))
//                    .largeTraffic(Integer.valueOf(tunnelNameResultExcel.getLargeTraffic()))
//                    .vMin(Integer.valueOf(tunnelNameResultExcel.getVMin()))
//                    .lightDistance(Integer.valueOf(tunnelNameResultExcel.getLightDistance()))
//                    .delay(Integer.valueOf(tunnelNameResultExcel.getDelay()))
                    .level(4).build();
            tunnelNameResultService.save(tunnelNameResult);

            //保存隧道所需参数
            TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = new TunnelEdgeComputingTerminal();
            BeanUtils.copyProperties(tunnelNameResultExcel,tunnelEdgeComputingTerminal);

            tunnelEdgeComputingTerminal.setInMileageNum(ConvertBit.deviceNumConvertValue(tunnelNameResultExcel.getInMileageNum()));
            tunnelEdgeComputingTerminal.setOutMileageNum(ConvertBit.deviceNumConvertValue(tunnelNameResultExcel.getOutMileageNum()));
            tunnelEdgeComputingTerminal.setBasicStart(ConvertBit.deviceNumConvertValue(tunnelNameResultExcel.getBasicStart()));
            tunnelEdgeComputingTerminal.setBasicEnd(ConvertBit.deviceNumConvertValue(tunnelNameResultExcel.getBasicEnd()));

            tunnelEdgeComputingTerminal.setId(tunnelNameResult.getId());
            tunnelEdgeComputingTerminal.setLineMileage(new BigDecimal(tunnelNameResultExcel.getLineMileage()));
            tunnelEdgeComputingTerminal.setTunnelName(tunnelNameResultExcel.getDirection());
            tunnelEdgeComputingTerminal.setTunnelMileage(new BigDecimal(tunnelNameResultExcel.getTunnelMileage()));
            tunnelEdgeComputingTerminal.setLineId(lineId);
            tunnelEdgeComputingTerminal.setLineName(tunnelLineName.getTunnelName());
            tunnelEdgeComputingTerminal.setLineMileageTunnel(tunnelNameExist.getTunnelMileage());
            tunnelEdgeComputingTerminal.setTunnelId(tunnelId);
            tunnelEdgeComputingTerminal.setDirection(tunnelNameResult.getTunnelName());
            tunnelEdgeComputingTerminalService.saveObject(tunnelEdgeComputingTerminal);
        }
    }
}
