import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scsdky.ScsdkyApplication;
import com.scsdky.web.domain.*;
import com.scsdky.web.service.*;
import com.scsdky.web.service.impl.TunnelStatisticsServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

//import static com.scsdky.web.service.impl.TestServiceImpl.randomNum;

/**
 * @author wjs
 * @date 2022/4/1 11:51
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScsdkyApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreateData {

    @Resource
    TunnelStatisticsServiceImpl tunnelStatisticsService;

    @Resource
    TunnelDeviceService tunnelDeviceService;

    @Resource
    TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    @Resource
    TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    private static int getRandom(int min, int max) {
        Random random = new Random();
        int randomNumber = random.nextInt(max) + min;
        return randomNumber;
    }

    @Test
    public void createTunnelStatics() {
        QueryWrapper<TunnelStatistics> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("upload_time", "2023-12-01");
        queryWrapper.le("upload_time", "2023-12-31");
        List<TunnelStatistics> tunnelStatisticsList = tunnelStatisticsService.list(queryWrapper);
        int month = 3;
        while (month <= 11) {
            int count = 0;
            List<TunnelStatistics> saveList = new ArrayList<>();
            for (TunnelStatistics tunnelStatistics :
                    tunnelStatisticsList) {
                TunnelStatistics dstTunnelStatistics = new TunnelStatistics();
                BeanUtil.copyProperties(tunnelStatistics, dstTunnelStatistics);
                dstTunnelStatistics.setId(null);
                Date newDate = DateUtil.offsetMonth(dstTunnelStatistics.getUploadTime(),0-month);
                dstTunnelStatistics.setUploadTime(newDate);
                dstTunnelStatistics.setUpdateTime(new Date());
                //实际耗电量
                dstTunnelStatistics.setActualPowerConsumption(getRandom(1, 150));
                // 实际单位里程耗电量
                //Integer lightUp = randomNum(24);
                Integer lightUp = 2;
                tunnelStatistics.setActualLightUpTime(lightUp);
                tunnelStatistics.setActualPowerConsumption(6 * lightUp);
                // 实际运行功率
                dstTunnelStatistics.setActualOperatingPower(6);
                // 实际碳排放量
                tunnelStatistics.setActualcarbonemission(String.valueOf((6 * lightUp) * 0.573 / 1000));
                dstTunnelStatistics.setActualOperatingPower(getRandom(1, 24));
                saveList.add(dstTunnelStatistics);
                if(saveList.size() >= 200){
                    tunnelStatisticsService.saveBatch(saveList);
                    saveList.clear();
                }
                count++;
                if(count >= 1000){
                    break;
                }
            }
            month++;
            if(saveList.size() >= 0){
                tunnelStatisticsService.saveBatch(saveList);
                saveList.clear();
            }
        }

    }
    @Test
    public void test() {
        System.out.println(DateUtil.offsetMonth(new Date(),0-1));
    }


    /**
     * 模拟构建灯具终端数据
     */
    @Test
    public void tunnelLampsTerminal() {

        /**
         * 获取所有隧道的id
         */
        List<TunnelEdgeComputingTerminal> tunnelEdgeComputingTerminalList = tunnelEdgeComputingTerminalService.list(new LambdaQueryWrapper<TunnelEdgeComputingTerminal>().gt(TunnelEdgeComputingTerminal::getId,170));


        for (TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal : tunnelEdgeComputingTerminalList) {
            //每根隧道构建450条灯具设备
            TunnelDevice tunnelDevice;
            TunnelLampsTerminal tunnelLampsTerminal;
            TunnelLampsEdgeComputing tunnelLampsEdgeComputing;
            for (int i = 0; i < 450; i++) {
                int num = RandomNumberGenerator();
                tunnelLampsTerminal = new TunnelLampsTerminal();
                tunnelLampsTerminal.setDeviceId((long) num);
                tunnelLampsTerminalService.save(tunnelLampsTerminal);
                TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>().eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelEdgeComputingTerminal.getId()));

                tunnelLampsEdgeComputing = new TunnelLampsEdgeComputing();
                tunnelLampsEdgeComputing.setDevicelistId(tunnelDevicelistTunnelinfo.getDevicelistId());
                tunnelLampsEdgeComputing.setUniqueId(tunnelLampsTerminal.getUniqueId());
                tunnelLampsEdgeComputingService.save(tunnelLampsEdgeComputing);

                tunnelDevice = new TunnelDevice();
                tunnelDevice.setTunnelId(tunnelEdgeComputingTerminal.getId());
                tunnelDevice.setTunnelName(tunnelEdgeComputingTerminal.getDirection());
                tunnelDevice.setDeviceId(String.valueOf(num));
                tunnelDevice.setDeviceNum("k3+001");
                tunnelDevice.setDeviceType("灯具控制器");
                tunnelDevice.setDeviceName("灯具控制器" + i);
                tunnelDevice.setLoopNumber("R1-2");
                tunnelDeviceService.save(tunnelDevice);
            }

        }
    }

    static Random random = new Random();
    public static int RandomNumberGenerator(){
        return random.nextInt(9000) + 1000;
    }
}
