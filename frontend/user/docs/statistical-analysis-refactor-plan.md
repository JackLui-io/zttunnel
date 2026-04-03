# 统计分析页面（statisticalAnalysis）重构方案

> 基于新前端 zttunnel-frontend 的 `/supervisoryControl` 页面结构，制定 `/statisticalAnalysis` 中央区域卡片数据适配方案。

---

## 一、SupervisoryControl 页面结构参考

### 1.1 整体布局

```
MainLayout
├── TopBar（顶部栏）
├── LeftNav（左侧隧道树，抽屉）
├── main.main-content（中央内容区）
│   └── SupervisoryControl
│       ├── sc-header（CentralNavTabs + CentralTunnelLabel）
│       └── 5 个卡片（grid 布局）
│           ├── CardEntrance
│           ├── CardBrightness
│           ├── CardTraffic
│           ├── CardPower
│           └── CardSpeed
└── RightSidebar（右侧边栏）
```

### 1.2 数据流

- **隧道选择**：`tunnelStore.selectedTunnelId`、`tunnelStore.selectedParentTunnelId`
- **卡片接收**：各卡片通过 `props: { tunnelId, parentTunnelId? }` 接收
- **数据请求**：卡片内部 `watch(tunnelId)`，`tunnelId` 非空时调用 API
- **无数据时**：使用空数组/占位，不阻塞渲染

### 1.3 卡片职责

- 每个卡片独立拉取数据，不依赖父组件传数据
- 父组件只传 `tunnelId`，卡片内部负责 API 调用与图表渲染

---

## 二、StatisticalAnalysis 当前结构

### 2.1 布局

```
MainLayout（同上）
└── StatisticalAnalysis
    ├── sa-header（CentralNavTabs + CentralTunnelLabel）
    ├── CardSaFilter（日期 + 分析类型 + 导出）
    ├── 4 × CardSaMetric（总体分析 4 个 KPI）
    └── CardSaMonthly（月度用电/节电柱状图）
```

### 2.2 现状问题

| 项目 | 现状 | 问题 |
|------|------|------|
| 隧道 ID | 未传递 | 卡片无法按隧道查询 |
| 日期范围 | CardSaFilter 内部维护，未向外传递 | 卡片无法按日期查询 |
| 数据来源 | `metricCardsMock`、`monthlyPowerMock` | 静态 mock，无真实接口 |
| 分析类型 | `activeFunc` 仅 UI 切换 | 未驱动内容区切换 |
| 导出 | `handleExport` 空实现 | 未对接导出接口 |

---

## 三、重构范围（仅中央区域卡片数据）

**目标**：保持现有 UI 布局与样式，仅将中央区域卡片接入真实 API 数据。

**不涉及**：
- MainLayout、LeftNav、RightSidebar、CentralNavTabs、CentralTunnelLabel
- 路由、权限、登录
- 车流/车速、洞内外亮度、能碳数据 等子标签的完整实现（可后续迭代）

**涉及**：
- 中央区域：CardSaFilter、4×CardSaMetric、CardSaMonthly
- 数据流：tunnelId、日期范围、API 对接

---

## 四、数据流设计

### 4.1 查询参数（与原前端一致）

```ts
interface SaQueryForm {
  startTime: string   // YYYY-MM-DD
  endTime: string     // YYYY-MM-DD
  tunnelId: number | null
}
```

### 4.2 数据来源

| 卡片 | 接口 | 参数 |
|------|------|------|
| CardSaMetric × 4 | `POST /analyze/statistics` | form |
| CardSaMonthly | `POST /analyze/lightByMonth` 或需确认 | tunnelId + year |

**说明**：原前端「月度用电/节电对比」在总体分析页展示，但 `lightByMonth` 接口需 `HomePageDto`（含 year、tunnelIds）。若后端无按日期范围的月度汇总，可暂用 mock 或后续补充接口。

### 4.3 状态提升方案

**方案 A：页面级状态 + props 下传（推荐）**

- `StatisticalAnalysis.vue` 维护 `queryForm: { startTime, endTime, tunnelId }`
- `tunnelId` 从 `tunnelStore.selectedTunnelId` 同步
- `startTime`、`endTime` 由 CardSaFilter 通过 `v-model` 或 `@update:range` 更新
- 卡片通过 props 接收 `queryForm` 或 `{ tunnelId, startTime, endTime }`

**方案 B：Composable 共享**

- 新建 `useStatisticalAnalysisQuery()`，返回 `queryForm`、`setDateRange` 等
- 页面与卡片均通过 composable 获取/更新查询参数

**建议**：采用方案 A，与 SupervisoryControl 的 props 模式一致，实现简单。

---

## 五、重构任务清单

### 5.1 API 层（`src/api/`）

- [ ] 新增 `getAnalyzeStatistics(form: SaQueryForm)` → `StatisticsVo`
- [ ] 新增 `statisticsExport(form)` → blob 下载（若已有可复用）
- [ ] 确认 `lightByMonth` 参数与用途，决定 CardSaMonthly 数据来源

### 5.2 页面层（`StatisticalAnalysis.vue`）

- [ ] 从 `tunnelStore` 读取 `selectedTunnelId`，写入 `queryForm.tunnelId`
- [ ] 初始化 `queryForm.startTime`、`queryForm.endTime`（默认近 7 天）
- [ ] 监听 CardSaFilter 的日期/快捷按钮，更新 `queryForm`
- [ ] 将 `queryForm` 通过 props 传给 CardSaMetric × 4、CardSaMonthly

### 5.3 CardSaFilter

- [ ] 支持 `v-model` 或 `modelValue` + `@update:modelValue` 传出日期范围
- [ ] 日期范围格式：`{ startTime: string, endTime: string }`
- [ ] 快捷按钮：近 7 天、近一月、近三月（计算逻辑与原前端一致）
- [ ] 导出：根据 `activeFunc` 调用对应导出接口，传入 `queryForm`

### 5.4 CardSaMetric × 4

- [ ] 新增 props：`queryForm` 或 `{ tunnelId, startTime, endTime }`
- [ ] 移除对 `metricCardsMock` 的依赖
- [ ] 内部调用 `getAnalyzeStatistics`，将 `StatisticsVo` 映射为 4 个 metric 结构
- [ ] `tunnelId` 为空时显示占位（如 `--`），不请求
- [ ] 加载态、错误态可后续补充

### 5.5 CardSaMonthly

- [ ] 新增 props：`queryForm` 或 `{ tunnelId, startTime, endTime }`
- [ ] 若后端有按日期范围的月度数据接口，则接入；否则暂保留 mock 或占位
- [ ] `tunnelId` 为空时清空图表

### 5.6 分析类型（activeFunc）与内容区

- **当前阶段**：仅实现「总体分析」，4 个 KPI + 月度图
- **后续**：`activeFunc === 'traffic'` 时切换为车流/车速卡片；`brightness`、`energy` 同理
- 重构方案中可预留 `v-if="activeFunc === 'overall'"` 包裹现有卡片区域

---

## 六、StatisticsVo 到 CardSaMetric 的映射

| 卡片 | 主指标 | rows[0] | rows[1] | rows[2] |
|------|--------|---------|---------|---------|
| 1 | theoreticalPowerSavingRate | actualPowerConsumption | originalPowerConsumption | 理论节电，barWidth=rate |
| 2 | theoreticalTotalPowerReduction | actualOperatingPower | originalOperatingPower | theoreticalOperatingPowerReduction |
| 3 | theoreticalCarbonEmissionReduction | actualCarbonEmission | originalCarbonEmission | 理论碳减排，barWidth=rate |
| 4 | theoreticalLightUpTimeReduction | actualLightUpTime | originalLightUpTime | 理论亮灯削减，barWidth=rate |

单位与格式需与原前端一致（kwh、kw、tCO₂、h 等）。

---

## 七、实施顺序建议

1. **API**：`getAnalyzeStatistics`、类型定义
2. **CardSaFilter**：日期范围双向绑定、快捷按钮、导出占位
3. **StatisticalAnalysis**：组装 `queryForm`，下传 props
4. **CardSaMetric**：接入 API，移除 mock
5. **CardSaMonthly**：确认数据源后接入或保留 mock
6. **导出**：对接各导出接口

---

## 八、与原前端差异说明

| 项目 | 原前端 | 新前端 |
|------|--------|--------|
| 隧道选择 | 左侧 tree，`direction.id` 作为 tunnelId | LeftNav，`tunnelStore.selectedTunnelId`（同为 level-4 节点 id） |
| 日期 | `parseTime`，近 7 天=6 天前至今 | 保持一致 |
| 响应处理 | `res.data`（axios 拦截器返回 body） | `extractData` 兼容 `code: 0` / `code: 200` |
| 分析类型 | 切换 table1~4，不同布局 | 当前仅总体分析，其余类型后续迭代 |
