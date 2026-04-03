# 今日耗电量与 statistics 接口差异说明

## 一、时间范围与单位

| 接口 | 时间范围 | 单位 |
|------|----------|------|
| **statistics** | startTime ~ endTime（carbonV3 在 endTime=今日时会排除今日） | actualPowerConsumption 为 **Wh** |
| **todayPowerSummary** | 仅今日 1 天 | todayConsumptionKwh 为 **kWh** |

## 二、你当前的数据对比

- **statistics**：startTime=2026-03-07，endTime=2026-03-13  
  - carbonV3 会把 endTime 调整为昨日，实际统计 2026-03-07 ~ 2026-03-12（6 天）
  - 隧道 500：159.52（6 天合计）
  - 隧道 501：178.47（6 天合计）
  - 合计：337.99（6 天）
  - 日均：337.99 ÷ 6 ≈ **56.3/天**
  - 单位：actualPowerConsumption 与 originalPowerConsumption(Wh) 同单位，为 **Wh**

- **todayPowerSummary**：今日 2026-03-13  
  - todayConsumptionKwh = 0.27 → **270 Wh**（1 天）

## 三、差异原因

1. **时间范围不同**：337.99 是 6 天合计，0.27 kWh 是 1 天，不能直接比较。
2. **换算后**：270 Wh（今日）vs 56.3 Wh/天（日均），今日约为日均的 4.8 倍，需结合业务判断是否合理。
3. **已做修复**：同一电表同日多条记录时，批量查询改为按电表取 `MAX(power_value)` 再汇总，避免重复累加。
4. **方向过滤**：批量查询未按 direction（左线/右线）过滤，可能包含更多电表，与 statistics 略有差异。

## 四、数值为何会变化？

- **0.27 kWh**：当时对批量查询结果做了 /1000，把 270 当成 Wh 换算成 0.27 kWh（换算错误）
- **270 kWh**：去掉 /1000 后，2026-03-13 当天的实际耗电（当天只过了一半）
- **189.48 kWh**：另一天的实际耗电（todayPowerSummary 始终返回「当天」数据）

不同日期耗电不同，所以会变化，属于正常现象。

## 五、建议验证

运行诊断 SQL（见 `verify_today_power_diagnostic.sql`）核对：

- 今日、昨日 `tunnel_carbon_day_push` 的 `power_value` 分布
- 每个 (devicelist_id, addr) 当日是否有重复记录
- 批量查询的 valid_pairs 与 statistics 使用的电表集合是否一致
