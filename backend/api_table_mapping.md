# 首页与数据大屏接口-数据库表映射清单

> 范围：`scsdky-admin` 模块中 `TunnelAnalyzeController` 的分析类接口。
> 说明：以下为当前代码实现口径（按 `Controller -> Service -> Mapper/Entity` 追踪）。

## 1. 首页接口

| 接口 | HTTP | Service方法 | 主要数据库表 | 备注 |
|---|---|---|---|---|
| `/analyze/lightByMonth` | POST | `TunnelStatisticsServiceImpl.lightByMonth` | `tunnel_power_data`, `tunnel_devicelist_tunnelinfo`, `tunnel_edge_computing_terminal`, `tunnel_power_edge_computing`, `t_user_tunnel` | 单隧道/用户可见隧道月度用电与节电；当前实现为电表数据口径 |
| `/analyze/lightByMonthAll` | GET | `TunnelStatisticsServiceImpl.lightByMonthAll` | `tunnel_carbon_day_push`, `tunnel_edge_computing_terminal` | 全隧道月度汇总；用 `tunnel_carbon_day_push` 聚合后在服务层补齐12个月 |

## 2. 数据大屏接口

| 接口 | HTTP | Service方法 | 主要数据库表 | 备注 |
|---|---|---|---|---|
| `/analyze/statistics` | POST | `TunnelStatisticsServiceImpl.statistics` | `t_user_tunnel`, `t_tunnel_name_result`, `tunnel_devicelist_tunnelinfo`, `tunnel_edge_computing_terminal`, `tunnel_carbon_day_push`, `tunnel_power_edge_computing`, `tunnel_inside_outside_day`, `tunnel_edge_compute_data` | 统计概览，汇总用电/碳排/亮灯等 |
| `/analyze/trafficFlowOrSpeed` | POST | `TunnelTrafficFlowServiceImpl.trafficFlowOrSpeedV2` | `tunnel_traffic_flow_day` | 日维度车流/车速（V2） |
| `/analyze/trafficFlowOrSpeedByHour` | POST | `TunnelTrafficFlowServiceImpl.trafficFlowOrSpeed` | `tunnel_edge_compute_data`, `tunnel_edge_computing_terminal`, `tunnel_devicelist_tunnelinfo` | 小时维度车流/车速 |
| `/analyze/insideAndOutside` | POST | `TunnelLightOutsideServiceImpl.insideAndOutsideV2` | `tunnel_inside_outside_day` | 日维度洞内外照度（V2） |
| `/analyze/insideAndOutsideByHour` | POST | `TunnelLightOutsideServiceImpl.insideAndOutside` | `tunnel_edge_compute_data` | 小时维度洞内外照度 |
| `/analyze/carbon` | POST | `TunnelStatisticsServiceImpl.carbonV4` | `tunnel_carbon_day_push`, `tunnel_power_edge_computing`, `tunnel_devicelist_tunnelinfo`, `tunnel_edge_computing_terminal` | 能碳数据（当前走 V4） |
| `/analyze/carbonByHour` | POST | `TunnelEnergyCarbonServiceImpl.carbon` | `t_tunnel_energy_carbon` | 小时能碳数据 |
| `/analyze/getCarbonByStatistics` | POST | `TunnelStatisticsServiceImpl.getCarbonByStatistics` | `tunnel_carbon_day_push`, `tunnel_power_edge_computing`, `tunnel_inside_outside_day`, `tunnel_edge_computing_terminal` | 统计模块中的碳排放量 |
| `/analyze/zdByHouse` | POST | `TunnelLightOutsideServiceImpl.zdByHouse` | `tunnel_edge_compute_data`, `tunnel_devicelist_tunnelinfo` | 照度对比小时统计 |
| `/analyze/clByHouse` | POST | `TunnelTrafficFlowServiceImpl.clByHouse` | `tunnel_edge_compute_data`, `tunnel_devicelist_tunnelinfo`, `tunnel_edge_computing_terminal` | 车流量小时统计 |
| `/analyze/csByHouse` | POST | `TunnelTrafficFlowServiceImpl.csByHouse` | `tunnel_edge_compute_data`, `tunnel_devicelist_tunnelinfo`, `tunnel_edge_computing_terminal` | 车速小时统计 |
| `/analyze/dnByHouse` | POST | `TunnelStatisticsServiceImpl.dnByHouse` | `tunnel_power_data`, `tunnel_devicelist_tunnelinfo`, `tunnel_power_edge_computing`, `tunnel_edge_computing_terminal` | 电能参数小时统计 |

## 3. 口径说明（交接建议重点）

- `lightByMonth` 与 `lightByMonthAll` 都是月度用电/节电接口，但统计范围和底层数据来源不同。
- `lightByMonth` 当前主路径不是 `t_tunnel_statistics`，而是电表数据口径（`tunnel_power_data`）。
- `lightByMonthAll` 当前主路径来自 `tunnel_carbon_day_push`，并在服务层按设计功率计算节电与碳减排。
- 数据大屏中的多项接口已经切到 `*_day` 日汇总表（如 `tunnel_traffic_flow_day`, `tunnel_inside_outside_day`），而小时接口仍大量使用 `tunnel_edge_compute_data`。

## 4. 代码定位

- `scsdky-admin/src/main/java/com/scsdky/web/controller/tunnel/TunnelAnalyzeController.java`
- `scsdky-admin/src/main/java/com/scsdky/web/service/impl/TunnelStatisticsServiceImpl.java`
- `scsdky-admin/src/main/java/com/scsdky/web/service/impl/TunnelTrafficFlowServiceImpl.java`
- `scsdky-admin/src/main/java/com/scsdky/web/service/impl/TunnelLightOutsideServiceImpl.java`
- `scsdky-admin/src/main/java/com/scsdky/web/service/impl/TunnelEnergyCarbonServiceImpl.java`
- `scsdky-admin/src/main/resources/mapper/TunnelPowerDataMapper.xml`
- `scsdky-admin/src/main/resources/mapper/TunnelTrafficFlowMapper.xml`
- `scsdky-admin/src/main/resources/mapper/TunnelLightOutsideMapper.xml`
- `scsdky-admin/src/main/resources/mapper/TunnelCarbonDayPushMapper.xml`
- `scsdky-admin/src/main/resources/mapper/TunnelEnergyCarbonMapper.xml`

