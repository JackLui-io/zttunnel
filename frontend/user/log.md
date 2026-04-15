# 修改日志

## 2026-03-17

### 统计分析页面卡片底部阴影

**修改**：月度用电/节电对比及三个分析表格的 `.card-sa-body` 将 `overflow: hidden` 改为 `overflow: visible`。`StatisticalAnalysis.vue` 增加 `padding-bottom: 12px` 为底部阴影留出空间。

### 实时监控页面底部卡片阴影

**修改**：每小时电能参数（`CardPower`）、平均车速（`CardSpeed`）的 `.card-rt-body`：`overflow: hidden` 改为 `overflow: visible`，`box-shadow` 增强为 `0 4px 8px rgba(85, 160, 92, 0.6)`。`SupervisoryControl.vue` 增加 `padding-bottom: 12px` 为底部阴影留出空间。

### 统计分析页面分析表格滚动条样式

**修改**：`CardSaTrafficTable`、`CardSaBrightnessTable`、`CardSaCarbonTable` 中 `.sa-table-wrap` 的滚动条：
- 宽度 8px（横竖一致）
- 轨道背景透明
- 滑块颜色 #91CAA7
- 支持 Webkit（Chrome/Safari）和 Firefox

### 登录页样式调整

**修改**：
1. 输入框文字：`InputField.vue` 中 `.input` 及 `::placeholder` 字号由 14px 改为 16px，因登录页有 scale 缩放，14px 视觉偏小
2. 标题字体：`style.css` 新增 `@font-face` 引入 `QingNiaoHuaGuangJianDaBiaoSong-2.ttf`，`Login.vue` 中 `.title-cn`（隧道低碳智控平台）采用该字体。需将字体文件放入 `public/fonts/` 目录
3. 英文副标题：`.title-en` 的 `margin-top` 由 `-4px` 改为 `5px`，与主标题拉开间距

### 数据大屏智控平台按钮下移

**修改**：`DataScreenHeader.vue` 中智控平台按钮增加 `margin-top: 6px`，使其往下移动 6px。

### 隧道选择卡片框高度修正

**问题**：卡片框（node-3）原先包含标题区域，标题图片覆盖在卡片框上方，导致圆角处露出卡片底色。

**修改**：`MainLayout.vue` 中 `.node-3` 的 `top` 由 `90px` 改为 `139px`（90 + 49），使卡片框仅包含隧道列表内容，不包含标题。标题区（49px）仅显示标题图片和文字，无卡片框背景。平板断点同理：`top` 由 `70px` 改为 `119px`。

### 登录页添加备案信息

**修改**：参考原前端 `zt_tunnel_web/App.vue` 的 `icp_footer` 结构，在 `Login.vue` 登录页添加备案信息：
- 内容：Copyright © 2026 中铁成都科学技术研究院有限公司. + 蜀ICP备20003395号（链接至 https://beian.miit.gov.cn）
- 位置：`position: fixed; bottom: 0` 固定在页面最底部
- 年份：使用 `new Date().getFullYear()` 动态获取当前年份
- 样式：半透明黑底、毛玻璃效果，12px 字号，ICP 号可点击跳转工信部备案查询

### 全局备案信息（所有页面）

**修改**：将备案信息移至 `App.vue` 作为全局组件，覆盖所有页面；底色改为透明，字体颜色 #5F646B：
- 登录页、Dashboard、实时监控、统计分析、设备管理、报表生成、数据大屏 均显示底部备案信息
- 登录页使用 `icp-footer-login` 提高 z-index 确保显示
- 从 `Login.vue` 移除重复的备案 footer

---

## 2026-03-13

### Dashboard Card3/4/5/6 参考年度数据概览（Card1）统一样式

**修改**：Card3Tunnel、Card4Device、Card5Efficiency、Card6Trend 参考 Card1Overview 效果调整，使第二、三行卡片与年度数据概览风格一致。

1. **Card3Tunnel（隧道概况）**：`overflow: visible`；inner `padding: 10px 24px 4px`（底部留白缩小）；行 `padding: 6px 0`；图标 40×40、徽章 40×24、字号略缩，确保 3 行内容完整显示；Dashboard 第二行高度 235px→250px。
2. **Card4Device（设备状态分布）**：`overflow: visible`；inner `padding: 19px 24px 20px`；图例 gap 8px、字号 14px、色块 14×14px；legend `margin-top: 20px`。
3. **Card5Efficiency（总体月度节能效率指标）**：`overflow: visible`；inner `padding: 19px 24px 24px`、`gap: 24px`；数值字号 24px、颜色 #0f7f62 / #618430；进度条高度 14px。
4. **Card6Trend（总体月度用电/节电趋势）**：`overflow: visible`；inner `padding: 19px 24px 20px`、`gap: 16px 18px`；月度小矩形内容区 padding 与 gap 略增，使布局更宽松。

### 大屏数据请求：独立处理每个接口，失败/超时不影响其它卡片

**问题**：大屏 6 个接口并发请求，`userPowerOverview` 慢或超时（15s）时，原先用 `Promise.allSettled` 会等所有请求结束才更新，导致快速返回的 `deviceStatusDistribution` 等数据也要等 15 秒才能展示；且若存在竞态，成功数据可能被覆盖。

**修改**：`DataScreenLayout.vue` 中 `fetchData` 改为对每个接口单独 `.then()/.catch()`，每个接口返回后立即更新对应 ref，互不影响。`Promise.allSettled(...).finally()` 仅用于在全部结束后关闭 loading。

**效果**：`deviceStatusDistribution` 约 180ms 返回后即可展示设备状态，不再等待慢接口。

**Dashboard 页面**：已检查，Dashboard 使用独立的 try-catch 分别请求，无此问题。

### 数据大屏卡片：底色与屏幕一致

**修改**：`dataScreenCards.css` 将 `.screen-card` 背景由浅色渐变改为 `#edf2ea`，与屏幕底色一致，消除左上角白色小三角。`.screen-card-header` 增加 `background-color: #edf2ea`，使 title.png 透明区域透出正确底色。`CardRadar`、`CardStats` 的 echarts 背景改为 `transparent`。

---

### Dashboard 第二行卡片：等高且底部对齐

**修改**：隧道概况、设备状态分布、总体月度节能效率指标三张卡片统一为等高且底部水平线对齐。Card3/Card4/Card5 根元素增加 `flex: 1`、`min-height: 0`、`overflow: hidden`；各 card-body 增加 `min-height: 0`；Card3-inner 增加 `height: 100%`；Card4/Card5-inner 增加 `min-height: 0`。Dashboard 中移除冗余的 `height: 100%`、`min-height: 0`，保留 `align-self: stretch` 使三卡等高。

---

### Dashboard 卡片布局：第二行高度与月度趋势适配

**修改**：
1. **第二行卡片**（隧道概况、设备状态、节能效率）：`grid-template-rows` 第三行由 250px 改为 235px（减 15px）。Card3/Card4/Card5 内容适配：缩小 padding、图标、字体、图表高度、进度条等。
2. **总体月度用电/节电趋势**（Card6Trend）：获得多出的 15px 空间；`margin-top` 由 25px 改为 10px；月度小矩形 `min-height` 由 140px 改为 155px；进度条高度由 `clamp(10px, 1.2vw, 16px)` 改为 `clamp(12px, 1.4vw, 18px)`；`card-6-inner` 底部 padding 由 20px 改为 12px，缩短第二行矩形与卡片底部的距离。

---

### 数据大屏顶栏：地区与天气实时获取

**修改**：`DataScreenHeader.vue` 使用 IP 定位 + 天气 API 获取实时信息：
- **地区**：`ipapi.co`（HTTPS，免费）根据 IP 返回城市
- **天气**：`Open-Meteo`（HTTPS，免费，无需 key）根据经纬度返回当前天气，天气码映射为中文

请求失败时显示 `--`。使用 axios 发起请求，超时 5 秒。

---

## 2026-03-xx

### 车流/车速 统计分析

**API**（`src/api/analyze.ts`）：
- `getTrafficFlowOrSpeed(form)`：POST `/analyze/trafficFlowOrSpeed`，参数 startTime、endTime、tunnelId
- `trafficFlowOrSpeedExport(form, fileName)`：车流车速导出
- `TrafficFlowOrSpeedVo`：uploadTime、trafficFlow、maxTrafficFlow、minTrafficFlow、avgTrafficFlow、maxSpeed、minSpeed、avgSpeed

**卡片**：
- `CardSaTrafficFlow`：车流量折线+面积图，x 轴 uploadTime，y 轴 trafficFlow（辆/d），表头 wait.png
- `CardSaAvgSpeed`：平均速度折线+面积图，x 轴 uploadTime，y 轴 avgSpeed（km/h），表头 wait.png
- `CardSaTrafficTable`：分析表格，8 列（日期、每日车流量、最大/最小/平均车流、最大/最小/平均车速），表头 Group70.png

**StatisticalAnalysis.vue**：
- activeFunc === 'traffic' 时显示上述 3 个卡片，布局：第 2 行 2 列（车流量、平均速度），第 3 行全宽（分析表格）
- 导出时调用 trafficFlowOrSpeedExport

**说明**：表头图片 wait.png 需放在 public/page1/ 下，若不存在可暂用 Group70.png 替代。

**布局调整**：车流量与平均速度卡片并排放置、占满一行，grid-column 改为 1/3 与 3/5，各占 50% 宽度。

---

## 2026-02-26

### 4 个 echarts 图表对齐原项目 zt_tunnel_web

**修改**：将 CardBrightness、CardTraffic、CardPower、CardSpeed 与原项目 echarts1~4 对齐。

1. **统一 ensureChartAndSetOption 模式**：数据到达后再 init/绘制，避免 replaceMerge 丢失配置
2. **CardTraffic（车流量）**：boundaryGap: false，bar+line 双 Y 轴，bar 用 barWidth: 'auto'、barMaxWidth: 12，配色对齐原 echarts2
3. **CardPower（电能）**：boundaryGap: true，stack: 'Total' 堆叠柱状图，耗电量/理论节电量使用 decal 纹理，barMaxWidth: 6/12
4. **CardSpeed（车速）**：boundaryGap: false，单折线+面积，series 名称「实时车速」，hour 补零解析，配色对齐原 echarts3
5. **布局**：各卡片 chart-container 使用 flex: 1，保证图表高度

### 移除 mock 数据

**修改**：清除所有图表卡片的 mock 数据，无数据时使用空数组。
- `src/data/supervisoryControl.ts`：删除 brightnessMock、trafficMock、powerMock、speedMock
- `CardBrightness`、`CardTraffic`、`CardPower`、`CardSpeed`：使用 `emptyChartData()` 返回空数组作为默认/失败时的占位

### 亮度对比卡片：对齐原项目绘制流程

**原项目逻辑**（echarts1.vue）：
- 响应：request 拦截器返回 `res.data`（后端 body），组件用 `res.data.forEach` 取数组
- 横轴：`hour` 数组（如 "08:05"），来自 `item.hour` 补零
- 纵轴：左轴 `dimmingRadio`（调光比例），右轴 `outside`（洞外亮度）
- 绘制：数据到达后调用 `showEcharts()`，`getInstanceByDom` 或 `init`，再 `setOption(完整 option)`

**修改**：
- 改为“数据到达后再绘制”：`ensureChartAndSetOption` 在数据就绪后 init（若未 init）并执行完整 `setOption`
- `updateChart` 统一调用 `ensureChartAndSetOption`，避免 `replaceMerge` 导致 xAxis 配置丢失
- 后端返回 288 条（24×12，5 分钟间隔），hour 格式如 "08:5" 需补零为 "08:05"

### 亮度对比卡片：API 一致性与曲线图绘制

**分析**：
- **API 一致性**：原项目与新项目均调用 POST `/analyze/zdByHouse`，参数 `{ tunnelId, time }`，time 格式 `YYYY-MM-DD`，完全一致。后端返回 `dimmingRadio`、`outside`、`hour`。
- **图表不显示原因**：1) 图表容器在 flex 布局下可能无有效高度；2) 初始化时序问题（watch immediate 早于 onMounted）；3) extractData 需兼容 `code: 0`。

**修改**：
- `src/api/analyze.ts`：extractData 增加对 `code === 0` 的成功判断。
- `CardBrightness.vue`：
  - 使用 `nextTick` 确保 DOM 就绪后再初始化 echarts，初始化后调用 `chart.resize()`
  - 数据解析：补全 hour 格式（如 `8:5` → `08:05`），兼容 BigDecimal 序列化为字符串
  - 图表容器：`.card-rt-body` 增加 `display: flex`，`.chart-container` 使用 `flex: 1` 保证高度
  - 双 Y 轴：调光比例(%)、洞外亮度(cd/m²)，与原项目一致；移除固定 max 以支持自动缩放
  - `updateChart` 后调用 `chart.resize()` 确保尺寸正确

### 小车图标：常规隐藏，有车经过时显示

**需求**：参考原项目隧道有车经过时的延迟亮灯逻辑，新前端改为常规状态下不显示隧道中的小车图标，若有车经过则显示小车图标。

**原项目逻辑**（`zt_tunnel_web/tunnelDh.vue`）：
- WebSocket 连接 `/websocket/3`，收到 `{ tunnelId, flag }` 表示该隧道有车流（flag 为车流量）
- 根据 flag 动态添加小车并做移动动画，小车经过的灯段会亮（延迟亮灯）

**新前端实现**：
- 新增 `src/composables/useTunnelWebSocket.ts`：连接 WebSocket，当收到当前隧道 `tunnelId` 且 `flag > 0` 时，`carVisible = true`
- 小车显示后 10 秒自动隐藏（模拟车通过隧道）
- `CardEntrance.vue`：小车图标使用 `v-show="carVisible"`，默认不显示，有车经过时显示

### 修复入口里程-出口里程卡片不显示数据

**问题**：点击隧道后 `/analyze/get/current/model?tunnelId=501` 接口成功返回数据（`code: 0`），但入口里程-出口里程卡片仍显示 `--`。

**原因**：后端使用 `code: 0` 表示成功，而 `src/api/request.ts` 的响应拦截器只接受 `code === 200`，将 `code: 0` 的成功响应当作失败并 reject，导致卡片无法拿到数据。

**修改**：
- `src/api/request.ts`：在响应拦截器中增加对 `code === 0` 的成功判断，与 `code === 200` 并列；同时兼容错误信息字段 `message`（后端使用）和 `msg`。

### 按段亮灯 + 小车动画

**需求**：实现原版 zt_tunnel_web 的「按段亮灯 + 小车动画」，灯亮时显示 car.png，无灯亮时隐藏小车。

**修改**：
1. **CardEntrance.vue**：
   - 灯具按段亮灯 `isLampOn(ratio, i)`，亮灯样式 `background: #FFFFFF`（Rectangle 273）
   - 小车使用 car.png，`v-show="carVisible"`：有灯亮时显示、无灯亮时隐藏；位置跟随第一辆车 `carPosition`
2. **useTunnelWebSocket.ts**：移除演示小车逻辑，仅依赖 WebSocket 车流数据

### 亮灯判断：接入 mode（照明模式）

**问题**：新前端所有隧道都不亮灯，原版部分隧道有亮灯。

**原因**：原版 `tunnelDh.vue` 的 `isShowLampbulingbuling` 逻辑：`if (props.model != 1) str = true`。即 mode 0（无极调光）、mode 2（固定功率）时**全亮**；mode 1（智慧调光）时**按段亮灯**。新前端未接入 mode。

**接口**：`getCurrentModel` 返回 `res.data`，含 `mode` 字段。无单独“灯具状态”接口，亮灯由 mode + WebSocket 车流综合判断。

**修改**：`CardEntrance.vue` 从 `getCurrentModel` 获取 `mode`，存入 `lampMode`；`isLampOn` 中当 `mode != 1` 时返回 true（全亮），否则按段判断；`carVisible` 当 `mode != 1` 或有车时显示小车。

### 小车位置调整

**修改**：小车由 `top: 51%` 改为 `bottom: 4%`，并增加 `object-position: bottom`，使车轮贴紧隧道最下方。

### 图表颜色：对齐 supervisoryControl.html 浅绿+浅橙渐变

**参考**：`html/page1/supervisoryControl.html` 中 echarts 配色。

**修改**：
1. **CardTraffic**：柱状图改为浅绿渐变 `#91CAA7`→`#BBDF9F`→`#DEF0D6`，折线改为浅橙 `#FEB603`
2. **CardPower**：耗电量改为浅橙→浅绿渐变 `#FCC432`→`#DEF0D6`，理论节电量改为浅绿渐变 `#91CAA7`→`#BBDF9F`→`#DEF0D6`（移除 decal 纹理）
3. **CardSpeed**：面积图改为浅绿渐变 `rgba(145,202,167,0.8)`→`rgba(187,223,159,0.8)`→`rgba(222,240,214,0.8)`，线条 `#91CAA7`
4. **CardBrightness**：已使用 `#97C89F`、`#FEB603`，与参考一致

---

## 2026-03-03

### 统计分析页面（statisticalAnalysis）原前端代码分析总结

**目标**：为 zttunnel-frontend 新前端开发 `/statisticalAnalysis` 页面，需保持与原前端 zt_tunnel_web 数据展示逻辑完全一致。

#### 一、页面结构概览

原前端 `zt_tunnel_web/src/views/statisticalAnalysis/index.vue` 结构：

1. **左侧**：隧道树形选择器 `tree.vue`（公司 → 高速 → 隧道 → 左线/右线）
2. **右侧**：
   - 顶部：当前隧道路径、日期范围选择、快捷日期（近七天/近一月/近三月）、四个分析标签（总体分析/车流车速/洞内外亮度/能碳数据）、数据导出
   - 内容区：根据标签切换展示 table1~table4

#### 二、核心查询参数 form

```js
form = {
  startTime: "YYYY-MM-DD",  // 开始日期
  endTime: "YYYY-MM-DD",    // 结束日期
  tunnelId: Long            // 隧道ID（来自左侧选中的 direction.id，即左线/右线节点）
}
```

- 隧道选择：`changeMsg` 回调中 `form.tunnelId = val.direction.id`
- 日期：`parseTime` 格式化为 `{y}-{m}-{d}`，快捷按钮计算对应天数
- 数据请求：各 table 组件 watch `form` 变化，`tunnelId` 为 null 时不请求

#### 三、接口清单与数据结构

| 接口 | 方法 | 用途 | 请求体 | 响应 |
|------|------|------|--------|------|
| `/tunnel/tree/list` | GET | 隧道树结构 | - | 嵌套树：operation→expressway→tunnel→direction |
| `/analyze/statistics` | POST | 总体分析 | form | `StatisticsVo[]`，取 `[0]` |
| `/analyze/trafficFlowOrSpeed` | POST | 车流/车速 | form | `TrafficFlowOrSpeedVo[]` |
| `/analyze/insideAndOutside` | POST | 洞内外亮度 | form | `InsideAndOutsideVo[]` |
| `/analyze/carbon` | POST | 能碳数据 | form | `EnergyCarbonVo[]` |
| `/analyze/getCarbonByStatistics` | POST | 碳排放图表 | form | `CarbonVo[]` |
| `/analyze/statisticsExport` | POST | 总体分析导出 | form | Excel 文件流 |
| `/analyze/trafficFlowOrSpeedExport` | POST | 车流车速导出 | form | Excel 文件流 |
| `/analyze/trafficFlowOrSpeedListExport` | POST | 车流车速列表导出 | form | Excel 文件流 |
| `/analyze/insideAndOutsideExport` | POST | 洞内外照度导出 | form | Excel 文件流 |
| `/analyze/insideAndOutsideListExport` | POST | 洞内外照度列表导出 | form | Excel 文件流 |
| `/analyze/carbonExport` | POST | 能碳数据导出 | form | Excel 文件流 |

**说明**：原项目 table2、table3 中有 `{ tunnelId: 3, ...val }` 硬编码，新项目应去掉，统一使用 `form.tunnelId`。

#### 四、各子页面数据展示逻辑

**1. 总体分析（table1）**

- 接口：`getAnalyzeStatistics(form)` → `res.data[0]`
- 四个 KPI 卡片，字段与计算：

| 卡片 | 主指标 | 字段 | 计算/说明 |
|------|--------|------|-----------|
| 理论节电率 | `theoreticalPowerSavingRate`% | actualPowerConsumption, originalPowerConsumption | 理论节电 = original - actual，进度条宽度 = rate * 220 * 0.01 |
| 理论总功率削减 | `theoreticalTotalPowerReduction`% | actualOperatingPower, originalOperatingPower, theoreticalOperatingPowerReduction | 进度条同上 |
| 理论碳减排率 | `theoreticalCarbonEmissionReduction`% | actualCarbonEmission, originalCarbonEmission | 理论碳减排 = original - actual，进度条同上 |
| 理论亮灯时间削减 | `theoreticalLightUpTimeReduction`% | actualLightUpTime, originalLightUpTime | 理论削减 = original - actual，进度条同上 |

**2. 车流/车速（table2）**

- 接口：`getAnalyzeTrafficFlowOrSpeed(form)`
- 图表：echarts2 车流量折线（uploadTime, trafficFlow）、echarts3 平均速度折线（uploadTime, avgSpeed）
- 表格列：uploadTime, trafficFlow, maxTrafficFlow, minTrafficFlow, avgTrafficFlow, maxSpeed, minSpeed, avgSpeed

**3. 洞内外亮度（table3）**

- 接口：`getAnalyzeInsideAndOutside(form)`
- 图表：echarts4 双轴（avgOutside 洞外亮度、avgDimmingRadio 调光比例），echarts5 柱状（lightUp 亮灯、lightDown 暗灯）
- 表格列：uploadTime, maxOutside, minOutside, avgOutside, maxDimmingRadio, minDimmingRadio, avgDimmingRadio, lightRadio

**4. 能碳数据（table4）**

- 接口：`getAnalyzeCarbon(form)` + `getCarbonByStatistics(form)`
- 图表：echarts6 耗电量与节电率（dailyPowerConsumption, theoreticalPowerSavings, theoreticalPowerSavingRate），echarts7 碳排放（actualCarbonEmission, originalCarbonEmission，数据来自 getCarbonByStatistics）
- 表格：固定列 uploadTime, dailyPowerConsumption, cumulativePowerConsumption；动态列来自 `meterReadingVos`，每项 `loopName + '(kWh)'`，值 `meterReading0`、`meterReading1` 等

**EnergyCarbonVo**：`uploadTime` 可能为 LocalDate 字符串；`meterReadingVos`: `[{ loopName, dataValue }]`。

#### 五、隧道树与标题

- 树接口：`getTunnelTree()`，返回嵌套结构
- 标题：`expressway.tunnelName — tunnel.tunnelName`，方向 `direction.tunnelName` 用 `getName(val)` 按「到」拆分显示（如「凤翔→旬邑」）
- 选中逻辑：最终数据以 `direction.id` 作为 `tunnelId` 查询

#### 六、数据导出

- 使用 `AutoeeUtil.download(url, form, fileName)`，POST 传 form，responseType blob，保存为 Excel
- 各标签对应不同导出 URL 和文件名（见上表）

#### 七、新前端开发要点

1. 复用或对齐 `form` 参数与请求时机（tunnelId 非空、form 变化时请求）
2. 总体分析取 `res.data[0]`，注意后端可能返回 `code: 0` 表示成功
3. 能碳表格动态列：根据 `meterReadingVos` 生成表头和 `meterReading0` 等字段
4. echarts 数据顺序：原项目用 `unshift` 倒序，需确认与后端排序一致
5. 日期快捷：近 7 天 = 6 天前至今，近一月/近三月按自然月天数计算

### 统计分析页面重构方案（仅中央区域卡片数据）

**参考**：SupervisoryControl 页面结构（MainLayout + 中央 grid 卡片 + tunnelStore）

**方案文档**：`docs/statistical-analysis-refactor-plan.md`

**要点**：
1. 数据流：`queryForm = { startTime, endTime, tunnelId }`，tunnelId 来自 tunnelStore，日期由 CardSaFilter 提供
2. 卡片接入：CardSaMetric × 4 调用 `getAnalyzeStatistics`，CardSaMonthly 待确认数据源
3. 当前阶段仅实现「总体分析」，车流/车速等子标签后续迭代
4. StatisticsVo 到 4 个 KPI 卡片的字段映射已整理

### 统计分析页面实施完成（数据与计算与原前端一致）

**修改**：

1. **API**（`src/api/analyze.ts`）：
   - 新增 `SaQueryForm`、`StatisticsVo` 类型
   - `getAnalyzeStatistics(form)`：POST `/analyze/statistics`，取 `res.data[0]`
   - `statisticsExport(form, fileName)`：POST form-urlencoded 导出 blob

2. **日期工具**（`src/utils/saDate.ts`）：
   - `getRangeNear7Days()`：近 7 天 = 6 天前至今（与原 `timeInterval(6)` 一致）
   - `getRangeNear1Month()`：`getOldMonthDays(1)` 往前天数
   - `getRangeNear3Months()`：`getOldMonthDays(3)` 往前天数

3. **CardSaFilter**：
   - `v-model:model-value` 日期范围，`v-model:active-func` 分析类型
   - 快捷按钮调用 `saDate` 工具，与原前端 `timeClick` 一致
   - `@export` 触发父组件导出

4. **StatisticalAnalysis**：
   - `queryForm` 由 `dateRange` + `tunnelStore.selectedTunnelId` 组成
   - `watch` 变化时调用 `getAnalyzeStatistics`
   - 仅实现「总体分析」，导出调用 `statisticsExport`

5. **saMetrics**（`src/utils/saMetrics.ts`）：
   - `statisticsVoToMetrics(vo)`：将 `StatisticsVo` 映射为 4 个 KPI 卡片
   - 理论节电/碳减排/亮灯削减 = 原设计 - 实际，`.toFixed(2)` 与原 table1 一致
   - 无数据时显示 `--` 占位

6. **CardSaMonthly**：暂保留 mock 数据，原总体分析无月度图，后续可接 `lightByMonth` 等

### CardSaMonthly 接入 lightByMonth 接口

**修改**：

1. **API**（`src/api/analyze.ts`）：
   - 新增 `PowerLightVo` 类型：`month`、`totalLight`、`totalEconomyLight`
   - `getLightByMonth(tunnelId, year)`：POST `/analyze/lightByMonth`，参数 `{ tunnelId, year }`

2. **CardSaMonthly.vue**：
   - 新增 props：`tunnelId`（number | null）、`year`（默认当前年）
   - 调用 `getLightByMonth` 获取数据，`padTo12Months` 将接口返回按 1-12 月补齐，缺失月份用 0
   - 映射：`totalLight` → 实际用电量，`totalEconomyLight` → 节电量
   - 移除 `monthlyPowerMock`，改为接口数据；watch tunnelId/year 变化时重新请求

3. **StatisticalAnalysis.vue**：
   - 传递 `tunnel-id`、`year` 给 `CardSaMonthly`

### 月度用电图表：请求参数对齐原前端

**问题**：2026 年 1 月数据库有数据，但新前端月度图表显示 6–12 月。需检查传参是否正确。

**修改**：
1. **API**：`getLightByMonth(tunnelId, year)` 的 `year` 改为 **number**（如 2026），与原前端 `{ tunnelId, year }` 一致
2. **CardSaMonthly**：移除 year prop，组件内部使用 `new Date().getFullYear()` 作为当前年
3. **StatisticalAnalysis**：仅传 `tunnel-id`，不再传 year
4. **调试**：开发环境下 `console.debug` 输出请求参数，便于在 Network/Console 中验证

**说明**：总体分析（4 个 KPI 卡片）来自 `t_tunnel_statistics`，月度用电来自 `tunnel_power_data`，两者数据源不同。若传参正确仍显示异常，需后端确认 `selectImepGroupByMonth` 的查询逻辑。

### 日期选择器可手动选择 + lightByMonth 后端 SQL 修复

**1. 日期选择器**（`CardSaFilter.vue`）：
- 参考原前端，使用 Element Plus `el-date-picker` type="daterange"，支持日历弹窗、双月并排、范围选择
- 新增 element-plus 依赖，main.ts 全局注册
- 保留「近7天」「近一月」「近三月」快捷按钮
- 移除 Vector198.svg 图标；日期选择器样式适配绿色主题（文字/分隔符/图标 #0f7f62，透明背景融入 #def0d6 容器）
- 日期选择器输入框背景改为 #def0d6，与按钮一致；数据导出改为 sa-func-btn 样式，与总体分析等按钮同尺寸

**2. lightByMonth 数据混入 2025 年 7–12 月问题**：
- **说明**：不改动后端（`selectImepGroupByMonth` 可能被多处使用），仅在前端处理
- **前端修复**（`CardSaMonthly.vue`）：`padTo12Months` 增加 year 参数，当年时仅保留 1 月至当前月，未来月份置 0，避免显示后端可能混入的往年数据

---

## 2026-03-04

### TopBar 顶栏用户名改为当前登录用户

**修改**：
1. **TopBar.vue**：将硬编码的「超级管理员」改为从 auth store 的 `user` 获取，使用 `computed` 显示 `userName ?? nickName ?? '超级管理员'` 作为兜底
2. **MainLayout.vue**：在 `onMounted` 中调用 `authStore.fetchUserInfo()`，确保进入主布局时拉取当前用户信息

### 数据大屏底部装饰

**修改**（`DataScreen.vue`）：
- 页面底部中央新增装饰图层：底层 `shape502.png`，其上居中叠放 `shape9.png`
- 使用 `position: relative` 容器 + `position: absolute` 实现图层叠加
- 需将 `shape502.png`、`shape9.png` 放入 `public/page1/` 目录

### 数据大屏背景与 Dashboard 一致

**修改**（`DataScreen.vue`）：移除 `527.svg` 背景图，改用与 MainLayout/Dashboard 相同的背景色 `#edf2ea`

### test_dev 用户年度数据概览无值问题修复（后端，方案 1）

**问题**：Dashboard 年度数据概览在 test_dev 等用户下显示 `---`。

**根因**：前端传 `tunnelId: null` 表示汇总，后端 `@NotNull` 校验失败且 `statistics()` 未正确处理 null。

**修改**（zt_project_tunnel 后端）：
1. **AnalyzeDto**：移除 `tunnelId` 的 `@NotNull`
2. **TunnelStatisticsServiceImpl.statistics()**：`tunnelId == null` 时遍历用户可见隧道，逐隧道统计后聚合；`tunnelId != null` 时保持原单隧道逻辑
3. 新增 `statisticsForOneTunnel()`、`aggregateStatistics()`

**影响范围**：仅 Dashboard 传 null；统计分析、报表、导出均传具体 tunnelId，行为不变。详见 `docs/statistics-api-impact.md`。

### 登录 getInfo 返回 500（user is null）修复

**问题**：登录后 `getInfo` 返回 500，`"Cannot invoke SysUser.getUserId() because user is null"`。

**原因**：从 Redis 反序列化的 `LoginUser` 中 `user` 字段可能为 null（序列化/反序列化异常），`getInfo` 直接使用 `getUser()` 导致 NPE。

**修改**（`SysLoginController.getInfo()`）：
- 当 `loginUser.getUser()` 为 null 且 `loginUser.getUserId()` 非空时，用 `userService.selectUserById()` 从数据库回填
- 若仍无法获取 user，返回 401「登录已过期，请重新登录」

### 修复 test_dev/test_dev2 登录后右上角误显示「超级管理员」

**问题**：test_dev、test_dev2 登录后，页面右上角偶尔显示「超级管理员」，刷新后恢复为正确用户名。

**原因**：`TopBar.vue` 中 `username` 的 fallback 为 `'超级管理员'`。当 `authStore.user` 为 null 或 `userName`、`nickName` 均为空时（如 getInfo 未完成、返回异常、Redis 反序列化导致 user 为空），会显示该 fallback，造成非 admin 用户误显示「超级管理员」。

**修改**（`TopBar.vue`）：
- 将 fallback 从 `'超级管理员'` 改为 `'用户'`，避免在用户信息未加载或异常时误导性显示

### Dashboard 隧道概况与设备状态分布对接（2026-03-04）

**修改**：
1. **隧道概况（Card3Tunnel）**：改为调用 `getTunnelOverview()` API，不再从 tunnelData 前端聚合
2. **设备状态分布（Card4Device）**：接收 `deviceStatus` 属性，调用 `getDeviceStatusDistribution()` API，使用 echarts 饼图展示在线/离线/故障分布，图例显示数量与占比
3. **API**（`src/api/analyze.ts`）：新增 `getTunnelOverview`、`getDeviceStatusDistribution` 及对应类型

### 总体月度节能效率指标卡片去掉右上角年份（2026-03-04）

**修改**（`Card5Efficiency.vue`）：移除标题栏右侧的年份显示（如「2026年」）及对应样式、year 属性

### 入口里程卡片小车随 WebSocket 车流移动（2026-03-31）

**修改**（`CardEntrance.vue`）：
- 小车图层移入 `entrance-tunnel-inner`，与灯具共用 0–100% 洞轴坐标
- `useTunnelWebSocket` 返回的 `cars[].x` 绑定为 `left: x * 100%` + `translateX(-50%)`，多车 `v-for`，`transition: left 0.1s linear` 与 100ms  tick 对齐
- 非智慧调光且无模拟车时仍显示原 `left: 24%` 装饰车

### 修复入口里程随车亮灯不生效（2026-04-01）

**原因概要**：
- WebSocket URL 仅用 `hostname` 丢掉 API 端口（如 `:8026`），难以连上真后端
- 开发环境 Vite 未代理 `/websocket`，`ws://localhost:5173/websocket/3` 无法到达 8026
- `tunnelId ===` 严格相等易因 number/string 不匹配丢消息
- `setInterval` 缺少第二参数时行为不确定；已固定为 100ms
- `mode !== 1` 时 `isLampOn` 恒为 true，有车流时灯仍全亮、看不出随车效果

**修改**：
- `useTunnelWebSocket.ts`：`getWebSocketUrl` 使用 `URL.host`（含端口）；无完整 base 时用 `window.location.host`；消息用 `coerceTrafficPayload` 解析，`Number(tunnelId)` 比对；`setInterval(..., 100)`
- `vite.config.ts`：增加 `/websocket` 的 `ws: true` 代理到 `localhost:8026`
- `CardEntrance.vue`：有 `running` 车时一律按段亮灯；无车且非智慧调光仍全亮；无车且智慧调光仍全暗

### 车流 WS 与选中隧道 id 对齐（2026-04-01）

**后端**：`PushController` 车流推送改为 `HashMap`，确保 JSON 含 `tunnelId`、`flag`（见 `zt_project_tunnel` log）。

**前端**：`useTunnelWebSocket` 增加可选 `parentTunnelId` 参数，与当前选中 level-4 或 level-3 匹配均可；开发环境下 tunnelId 不匹配时在控制台 `console.debug` 提示。

### 开发环境车流 WebSocket 直连后端端口（2026-04-01）

**问题**：`.env.development` 中 `VITE_API_BASE_URL` 为空时，原逻辑使用 `ws://{页面 host}/websocket/3`（如 `5173`），连到 Vite 而非 Java；Network → WS → Messages 中只有 `type:connected`、vite-plugin-inspect 等 HMR 流量，无 `tunnelId`。

**修改**：`getWebSocketUrl` 在 `import.meta.env.DEV` 且无绝对 API 地址时，改为 `ws(s)://{hostname}:{VITE_DEV_BACKEND_PORT||8026}/websocket/3`；支持可选 `VITE_WS_URL` 覆盖。`.env.development` 增加说明注释。

### 入口里程卡片车流逻辑（2026-04-07）

- 去掉静态装饰车、切换隧道即 `stopRun`、`carsPush` 单帧上限放宽等已保留。
- **不再**在点开隧道时根据 `clByHouse` 小时流量自动 seed 小车：统计是「小时里的车」，不是「此刻是否过车」，一点隧道就动易与现场不符。**入口小车与随车亮灯仅由 WebSocket（tunnelId + flag）驱动**；「车流量统计」图表仍用 clByHouse，与动画解耦。说明见 `useTunnelWebSocket.ts` 顶部注释与 `CardEntrance` 中 `isLampOn` 注释。

### 生产环境 WS 空白与相对路径 API（2026-03-31）

**原因说明**：`VITE_API_BASE_URL=/api` 为相对路径时，显式走 `wss://{页面 host}/websocket/3`，须在网关反代 `/websocket/` 并 Upgrade；前后端不同域时用 `VITE_WS_URL`。`VITE_TUNNEL_WS_DEBUG=1` 时在控制台输出 WS open/message 及 tunnelId 不匹配提示（生产可临时写入 `.env.production` 再构建）。

**修改**：`getWebSocketUrl` 增加以 `/` 开头的 `base` 分支与注释；`useTunnelWebSocket` 增加 `isTunnelWsDebug` 及对 `onopen`/`onerror`/`onclose` 日志。`.env.production` 增加 `VITE_WS_URL`、`VITE_TUNNEL_WS_DEBUG` 说明。

### HTTPS 站点与 wss（2026-03-31）

**行为**：`getWebSocketUrl` 已用 `window.location.protocol === 'https:'` 选择 `wss:`，同源默认地址为 `wss://{host}/websocket/3`。

**防呆**：若 `VITE_WS_URL` 误写为 `ws://`，在 HTTPS 页面下自动改为 `wss://`，避免混合内容被拦。`.env.production` 注释强调生产应用 `wss://`。

### 生产 Nginx 完整示例（2026-03-31）

**新增**：`deploy/nginx.zttunnel.com.conf` — 含 HTTP→HTTPS、`443` 与 `wss` 所需 `location /websocket/`（`Upgrade`、`map $connection_upgrade`）、`/api/`、`/apsLogin/`、静态目录与缓存。证书路径、`root`、`upstream` 端口需按服务器实际修改。

### 线上小车不动与同机 Nginx 自检（2026-04-07）

**现象**：本地有小车动画，部署到服务器后无。

**建议排查**：① 浏览器 F12 → Network → WS：是否 101、Messages 是否有含 `tunnelId`/`flag` 的帧；② 服务器 Java 是否已部署「去掉全局 id 去重、onOpen 用 HashMap」等修复；③ 推送里 `tunnelId` 是否与当前选中**左右线 id**（或 `parentTunnelId`）一致，可临时 `VITE_TUNNEL_WS_DEBUG=1` 重构建看控制台；④ `GET /push/test/traffic-ws?tunnelId=...` 验证下行。

**Nginx**：仅 `listen 443` 而无 `ssl` 与证书时，**不能**作为标准 HTTPS 站点；访客用 `https://` 需 `ssl_certificate` 等或由前置机终结 TLS。`/websocket/` 的 `proxy_pass` + `Upgrade` 写法可用；`root` 务必与实际上传的 dist 一致。

**CDN 挂证书**：TLS 在 CDN 终结、回源 HTTP 时，源站可不挂证书，但回源端口上仍需 `/websocket/` 反代；**须在 CDN 开启 WebSocket**。见 `deploy/nginx.zttunnel.com.conf` 头部注释。

**排查结论（2026-04-08）**：源站执行 `curl` 对 `http://127.0.0.1/websocket/3` 返回 **HTTP/1.1 101** 且收到 `{"msg":"connected","code":200}` 及多路 `tunnelId`/`flag`，说明 **Nginx→Java WebSocket 正常**；公网浏览器 `wss://www...` 仍 `failed` 时，问题在 **CDN 未正确转发 WebSocket**，需在 CDN 开启 WS。前端对 **`flag<=0`** 不追加小车，选中有 **`flag>0`** 的 `tunnelId`（如 507 且 flag=15）后才有动画。

**取消 CDN、Nginx 全站 80→443 后 curl 301（2026-04-08）**：再对 `http://127.0.0.1/websocket/3` 测会得到 **301 到 https://127.0.0.1/...**，因 80 仅做重定向；应用 **https** + 适配证书的主机名（`Host: www.zttunnel.com` 或公网域名）验证 WebSocket 101。说明见 `deploy/nginx.zttunnel.com.conf` 注释。

### 入口随车亮灯改为「当前灯 + 前后各一盏」（2026-04-08）

**原因**：原先用相邻灯中点划分区间，单车只落在一段内只亮一盏；与现场「车头灯带」预期（车身所在灯 + 前、后各一盏，共三盏）不符。

**修改**：`CardEntrance.vue` 用 `litLampIndicesWithCars`：对每辆车取与 `x` **距离最小**的灯下标 `i`，点亮 `i-1、i、i+1`（越界则少于 3 盏），多车取并集。

### 多车同时显示与 zt_tunnel_web 对齐（2026-04-08）

**原因**：原 `tunnelDh.vue` 中 `carsPush(flag)` 用 `for (i < val)` 把 **flag 当作本批发车数量**；新项目曾用 `floor(flag/80)`，小 flag 每帧只加 1 辆，难以同时多车。

**修改**：`useTunnelWebSocket.ts` 中 `carsPush` 改为 `n = min(max(1,floor(flag)), 24)`，首辆立即 `carsAdd`，其余用短随机 `setTimeout` 错峰；`stopRun` 清除待发定时器；动画仅在 `cars` 与待发队列皆空时停止。

### 随车三灯与多车叠影对齐原 tunnelDh（2026-04-08）

**差异**：原 `tunnelDh` 灯具按桩号落在 **50 格排序** 后算 `left/right`（px），`running` 中车 `left` 落在 `val.left < item < val.right` 则亮；新车 `carsAdd` 随机 **top 双车道**。新项目 `lampPositions` 曾 **未按里程排序**，用下标 ±1 不是空间前中后；多车同 `x`、同速易 **叠影**。

**修改**：`fetchLampPositions` 对比例 **升序排序**；`centerLampIndexForCar` 用相邻灯 **中点区间** 定中灯再 ±1；`TunnelCar` 增加 `lane`，`carsPush` 为每辆设 **初始 x 间距**（`WS_SPAWN_X0/GAP`）与 **lane 交替**；`carMovingStyle` 用 `translate` 纵向错车道。

### 小车步长按 tunnelDh 隧道长 + 设计车速（2026-04-08）

**结论**：原 `tunnelDh.vue` **未**对接实时车速接口；`carsRun` 中 `V = (1300 / (tunnelLong / 22.22)) * 0.1` 像素/100ms，`tunnelLong` 来自 `getCurrentModel` 里程差，`22.22` 为写死的 m/s（约 80 km/h）。

**修改**：`CardEntrance` 维护 `tunnelAxisLengthMeters`（与 `tunnelLong` 同源）传入 `useTunnelWebSocket`；`tunnelCarNormalizedStepPerTick` 将同式换算为 0~1 轴步长；无有效长度时用 `FALLBACK_NORMALIZED_STEP`。

### 左线无灯：里程方向 out < in（2026-04-08）

**原因**：`fetchLampPositions` 曾用 `tunnelLong = outM - inM`；左线若桩号录入为「大→小」，差值为负被判无效并清空 `lampPositions`。

**修改**：`startM = min(in,out)`，`endM = max(in,out)`，`tunnelLong = endM - startM`；灯位比例 `(deviceNum - startM) / tunnelLong`。

**文件**：`deploy/nginx.zttunnel.com.conf` 顶部增加常见坑注释。

### 数据大屏中央地图省份热点（2026-04-08）

**需求**：在 `cmap.png` 上对陕西、广西、湖南、吉林 四省放置可点击热点；点击展示管理单位名称、线路条数（level=2）、隧道条数（level=3）。对应 `t_tunnel_name_result` 的 level=1 节点：`1` 陕西旬凤韩黄、`542` 广西南横、`552` 炉慈桑龙、`600` 桓集高速。

**数据**：不单独增接口；大屏与其它页一致调用 **`GET /tunnel/tree/list`**（`getTunnelTree`），在前端 **`mapHotspotsFromTree.ts`** 内按 id 找到 level=1 节点，对子树 DFS 统计 level=2 线路数、level=3 隧道数（与 `tunnelOverview` 口径一致）；单位名取节点 `name`，无节点时用预设兜底。

**前端**：`DataScreenLayout` 去掉整页底图上的 `cmap.png`（避免与热点错位），改由 `DataScreenCenter` 内 `img` + `HOTSPOT_LAYOUT` 百分比热点；点击浮层外关闭气泡。

**文件**：`DataScreenCenter.vue`、`DataScreenLayout.vue`、`mapHotspotsFromTree.ts`、`api/tunnel.ts`。

### 大屏地图与提示框视觉（2026-04-08）

**修改**：`DataScreenCenter.vue` 中 `cmap.png` 显示尺寸相对原 `max-height`（52vh / 520px）放大 **20%**（62.4vh / 624px；移动端 40vh→48vh）。地图热点信息框改为设计稿样式：**白边 + 圆角 12px**，**顶栏**左→右绿渐变（`#76b891`–`#c8e6b2`）白字标题（省份），**正文**浅薄荷底（`#eaf6ee`）三行：标签 `#4a6354`，数值 `#43a074`，行内为管理单位 / 线路 / 隧道。

**调整（同日）**：去掉提示框渐变顶栏（不再在顶栏显示省份白字）；正文仍为管理单位 / 线路 / 隧道三行；地图在上次尺寸上再放大 **20%**（`74.88vh` / `749px`，小屏 `57.6vh`）。

### 大屏地图四省热点坐标校正（2026-04-08）

**问题**：`HOTSPOT_LAYOUT` 原百分比使热点偏向黑龙江、宁夏、贵州与南海一侧。**修改**：`DataScreenCenter.vue` 中调整为更接近吉林 / 陕西 / 湖南 / 广西省区（`600` `70%/28%`、`1` `52%/48%`、`552` `65%/56%`、`542` `48%/64%`）；换底图后仍可继续微调同一常量。

### 大屏热力点与信息框层级（2026-04-08）

**修改**：热力点为红色 `#e3252b`，白环 + 双层红色 `box-shadow` 光晕，扩散层用径向渐变脉冲。信息框恢复绿色渐变顶栏（`#76b891`–`#c8e6b2`），顶栏无文字。当前展开的热点槽位 `.map-hotspot-slot--active` 使用 `z-index: 1000`，弹层 `.map-popover` 使用 `z-index: 50` 高于同槽内按钮，避免其它热点盖住正文。`DataScreenLayout` 中 `.screen-main > .screen-center` 设 `z-index: 20`，减少左右栏压住弹层。

### 大屏闪电图钉热点（2026-04-08）

**修改**：`DataScreenCenter.vue` 在每个红点上方增加 **`.map-pin-btn`**：外层浅绿圆角框（`#c8e8d4`）、内层中绿渐变方角、白色闪电 SVG，底部三角指向热力点；与红点共用 `toggleHotspot`。文档点击关闭时排除 `.map-pin-btn`。信息框 `bottom` 增至 `calc(100% + 50px)`，避开图钉区域。

### 大屏地图信息框按线路展示隧道数（2026-04-08）

**修改**：`mapHotspotsFromTree.ts` 在 level=1 根下遍历 level=2 线路，对每条线路统计子树中 level=3 隧道数量，得到 `lineTunnelRows`；信息框保留「管理单位」后按行显示「线路名：N 条隧道」，多条线路依次列出；无数据时显示「暂无线路数据」。正文区支持 `max-height` 纵向滚动。

### WebSocket 隧道车流：车速与发车间隔（2026-03-31）

**需求**：穿洞车辆动画偏慢，需**加快 1 倍**（相对原步长约 2 倍）；同批车不再短随机扎堆，改为在 **5 分钟（300000ms）** 内按索引**均匀**错开发车。

**实现**（`useTunnelWebSocket.ts`）：

1. 常量 `CAR_SPEED_MULTIPLIER = 2`，`carsRun` 中 `step = tunnelCarNormalizedStepPerTick(...) * CAR_SPEED_MULTIPLIER`。
2. 常量 `WS_SPAWN_WINDOW_MS = 5 * 60 * 1000`；`carsPush` 对 `n` 辆车，第 `i` 辆延时 `n <= 1 ? 0 : Math.round((i / (n - 1)) * WS_SPAWN_WINDOW_MS)`（首辆 0、末辆 300000ms）；移除原 `WS_SPAWN_SPREAD_MS` / `WS_SPAWN_JITTER_MS`。
