# zttunnel-admin 修改日志

## 2026-03-18 登录页与 zttunnel-frontend 风格统一

- **背景与资源**：从 `zttunnel-frontend/public/bg5.svg` 复制到本仓库 `public/bg5.svg`（frontend 的 `style.css` 中写的是 `bg5.png`，仓库内实际仅有 `bg5.svg`，故使用 svg）。
- **新增组件**（与 frontend 的 LoginCard / InputField / PrimaryButton 一致）：
  - `src/components/login/LoginCard.vue`
  - `src/components/login/LoginInputField.vue`
  - `src/components/login/LoginPrimaryButton.vue`
- **`src/views/login/LoginPage.vue`**：重做为与 frontend 相同的标题文案、半透明绿色渐变登录框、`--login-scale` 自适应缩放、表单项与「记住密码」样式；背景在页面容器上使用 `bg5` + `#0f7f62` 底色；登录逻辑仍走 `useUserStore().login` + 全页刷新 `window.location.href = '/'`。
- **`index.html`**：增加 Google Fonts `Hind`（英文标题与 frontend 一致）；未拷贝青鸟华光字体（frontend 仓库中无 `/fonts/*.ttf`），中文标题使用 `Source Han Serif SC` / 宋体后备。
- **安全**：移除登录表单的默认账号密码占位。

## 2026-03-18 登录页文案与字体

- **主标题**：改为「隧道管理系统」，使用 `@font-face` 引用 `public/fonts/QingNiaoHuaGuangJianDaBiaoSong-2.ttf`（青鸟华光简大标宋）。
- **去掉**英文副标题；**卡片标题**：「系统登录」改为「后台登录」。
- **`index.html`**：移除已无用途的 Google Fonts `Hind` 引用。

## 2026-03-18 Dashboard 接口对齐后端

- **`src/api/analyze.js`**：`getLightByMonthAll` 改为 `getUserPowerOverview`，`POST /analyze/dashboard/userPowerOverview`，body `{ year }`。
- **`src/api/device.js`**：`countAllDevices` 改为 `getDeviceStatusDistribution`，`GET /analyze/dashboard/deviceStatusDistribution`。
- **`src/views/dashboard/Dashboard.vue`**：设备统计与饼图用分布接口（在线/离线；故障暂为 0）；能耗趋势与第四张卡片用 `userPowerOverview`（`monthlyData` → 图表，`annualOverview.totalSaving` → **年累计节电量**）；未接 `todayPowerSummary`；隧道列表逻辑未改。
- **年累计节电量**：展示单位改为 **万 kWh**（接口 kWh ÷ 10000，保留两位小数）。

## 2026-03-18 Dashboard 卡片对齐 zttunnel-frontend

- 新增 `components/dashboard/DashboardFrontendCardShell.vue`（绿系标题条 + `#f0fbef→#e0e9da` 内容区阴影，对齐 Card4/Card6）。
- `DashboardDeviceStatusCard.vue`：环形图样式与 frontend `Card4Device` 一致（在线/离线、`#6bb88d`/`#9e9e9e`、tooltip 深绿）。
- `DashboardMonthlyTrendCard.vue`：12 个月网格 + 用电/节电数值（万 kWh）+ 分段进度条，对齐 `Card6Trend`（无仓库内 PNG 时用 CSS 渐变/三角标替代切图）。
- `views/dashboard/Dashboard.vue`：两卡改用上述组件；布局大屏 `lg` 为 9+15 列，小屏堆叠。
- **Dashboard 卡片白底**：`DashboardFrontendCardShell` 标题与内容均为白色背景、浅灰描边阴影。
- **总体用电/能耗**：`DashboardMonthlyTrendCard` 改为双柱 ECharts（用电=渐变橙、能耗=渐变绿，数据：用电=consumption、能耗=节电量 saving 万 kWh 展示）。
- **设备状态环图**：图表区固定 320px 高（与用电/能耗柱图一致），`nextTick`+`resize`+`ResizeObserver` 避免 flex 下宽高为 0 不渲染；`Dashboard` 两列 `el-col` 拉伸使两卡同高。
- **设备状态**：改为实心 **饼状图**（`radius: 62%`），配色改为低饱和 `#a3ccb5` / `#c9ced1`。
- **总体用电/能耗**：柱状渐变改为 **浅色低饱和** 橙系/绿系（`#fff5e6→#dfc4a8`、`#eef6f0→#a8cdb9`）。

## 2026-03-18 设备列表 / 设备控制表格宽度与导出

- **`src/views/device/list/DeviceList.vue`**：表格外包 `.table-responsive`（横向滚动）；列 `width` 改 `min-width`，操作列 `fixed="right"`；`exportDevices` 改为 POST body 对象 `{ tunnelId, deviceType, keyword?, deviceStatus }`；补充 `ElMessageBox` 引用。
- **`src/views/device/components/*Tab.vue`**（边缘控制器 / 电能终端 / 灯具 / 引道灯）：同样外包表格 + `min-width` + 操作列固定右侧，窄屏可横向滚动。
- **`src/api/tunnelParam.js`**：`deleteApproachLamps` 改为 `DELETE /approachLamps/delete?idList=` 单参数拼接（对齐后端 `List<Long> idList`）。
- **`LampsManagement.vue` / `RadarManagement.vue` / `TerminalManagement.vue`**：`exportDevices` 统一为对象参数，与 `device.js` 中 `POST /device/list/export` 一致。

## 2026-03-18 设备管理 / 电力配置 / 能碳数据表格铺满宽度

- **`/device/management`**：`DeviceManagement.vue` 根与 `.card`（`:deep`）设 `width:100%`、`min-width:0`；`.tab-panel` 设 `display:block`。四个 Tab 内 `el-table` 改为 **`table-layout="fixed"`**，表格容器 `:deep` 强制 **`width:100%!important`** 并同步 **`el-table__inner-wrapper` / header / body** 宽度，宽屏下列随 `min-width` 均分剩余空间，窄屏仍横向滚动。
- **`/power/config`**（`PowerConfig.vue`）：主表容器 **`.table-container.table-responsive`**；列改为 **`min-width` + `show-overflow-tooltip`**；`table-layout="fixed"`；隧道级联 **`min(400px,100%)`**；`.power-config` / `:deep(.card)` 占满宽度。
- **`/carbon/data`**（`CarbonData.vue`）：表格外包 **`.table-responsive`**，同上 **fixed + min-width**；页面与 `.card` **100% / min-width:0**；隧道级联响应式宽度。

## 2026-03-18 隧道列表「新建隧道」Excel 导入

- **对齐 zt_tunnel_web**（`table5.vue`）：`el-upload` 提交 Excel 至 **`POST /easyExcel/excelInput`**（表单字段 **`file`**）。
- **`src/api/tunnel.js`**：新增 **`importTunnelExcel`**。
- **`src/utils/request.js`**：响应无 JSON 体（后端 `void`）且 HTTP 200 时归一为 `{ code: 200 }`，避免误报 Error。
- **`TunnelList.vue`**：工具栏增加 **「新建隧道」**（`el-upload` + `http-request`）、**「下载模板」**（指向 `public/static/邕宁东管理中心新建隧道参数上传.xlsx`，需自备；说明见 `public/static/隧道Excel模板说明.txt`）。

## 2026-03-18 隧道列表工具栏排版

- **`TunnelList.vue`**：筛选与操作分区；操作区 **`.tunnel-toolbar-actions`** 使用 **flex + wrap + gap**，工具栏表单项 **`flex: 1 1 100%`** 独占一行，避免 `el-upload` / `el-link` 与按钮叠压。

## 2026-03-18 隧道列表工具栏按钮样式

- **搜索**：`type="primary"` + **`plain`**（浅色边框主色）。**新建隧道**：实心 **`type="primary"`**（与原样式互换）。**下载模板**：改为 **`el-button` + `plain`**，`tag="a"` + `download`，与新建隧道原样式一致。

## 路由：隐藏「设备检测」菜单

- **`src/router/index.js`**：`/device/check` 子路由 **`meta.hidden: true`**，左侧导航不再展示「设备检测」；路由仍存在，直接访问 `/device/check` 仍可打开（需权限）。恢复菜单时删除 **`hidden`** 即可。

## 2026-03-24 文档：新增隧道涉及数据表（梳理说明，无业务代码改动）

- 根据 `zt_project_tunnel` 中 **`EasyExcelServiceImpl.excelInput`**（`/easyExcel/excelInput`）、**`TunnelNameListener`**、**`DeviceListener`**、**`TunnelNameResultServiceImpl.updateTunnelDeviceInfoById`**、**`KmlUtil`** 等代码，整理「新增/补全一条隧道」时可能写入的表与流程，供「复制隧道」功能设计参考（详见当次对话回复正文）。

## 2026-03-24 文档：`device_id` / `devicelist_id` 唯一性与「复制逐表」清单

- 依据 `TunnelDevicelist`、`TunnelDevicelistTunnelinfo`、`TunnelLampsTerminal`、`TunnelLampsEdgeComputing`、`TunnelOutOfRadar`、`TunnelDevice` 等实体及 `DeviceListener` / `TunnelLampsTerminalMapper.getDeviceLamp` 用法整理；**未在仓库中发现建表 SQL**，唯一性以 **MyBatis-Plus 主键与业务校验** 为准（详见当次对话回复正文）。

## 2026-03-24 隧道列表「复制隧道群」与后端 `/tunnel/copy/tunnelGroup`

### 后端 `zt_project_tunnel/scsdky-admin`

- **`TunnelCopyRequestDto` / `TunnelCopyResultVo`**：`domain/dto`、`domain/vo`。
- **`TunnelCopyService` + `TunnelCopyServiceImpl`**：事务内复制 level=3 隧道群及全部 level=4 子节点；复制 `tunnel_edge_computing_terminal`、`tunnel_devicelist` + `tunnel_devicelist_tunnelinfo`（边缘+电能）、`tunnel_device_param`、`tunnel_lamps_terminal` + `tunnel_lamps_edge_computing` + `tunnel_lamps_terminal_node`、`tunnel_out_of_radar`、`tunnel_power_edge_computing`、`t_tunnel_device`、`tunnel_approach_lamps_terminal`。占位规则见下条。
- **`TunnelCopyController`**（独立类，**不修改** `TunnelNameController`）：`POST /tunnel/copy/tunnelGroup`，权限 `system:tunnel:update`。

### 前端 `zttunnel-admin`

- **`src/api/tunnel.js`**：`copyTunnelGroup`。
- **`TunnelList.vue`**：level=3 操作列 **「复制」**；弹窗 **新隧道名称** 默认 **当前隧道群名称 +「复制」**（可改）；成功后仅 **刷新列表**，不自动进入隧道参数页。
- **`TunnelParam.vue`**：仍支持 URL **`?tunnelId=`** 打开时选中对应隧道（便于书签/外链，与复制流程无关）。
- **占位 `device_id`（后端，`TunnelCopyServiceImpl`）**：**`tunnel_devicelist`**（**BIGINT**）：`yy * 10^9 + [0, 10^9)`；**`tunnel_lamps_terminal`**（**`smallint unsigned`**）：`[1, 65535]`；**洞外雷达**（BIGINT）：`* 10^9 + …`；**`t_tunnel_device`**（String）：11 位 `%02d%09d`。

## 2026-03-24 隧道复制占位 ID：`TunnelCopyServiceImpl` 收尾

- **`TunnelCopyServiceImpl.java`**：占位生成按列类型分支（见后文 **devicelist SMALLINT / 灯具 INT / 洞外雷达 BIGINT / t_tunnel_device 字符串**）；移除旧 **`CP`**、**12 位时间戳** 方案。

## 2026-03-24 隧道复制：灯具 device_id 适配 MySQL INT

- **原因**：`tunnel_lamps_terminal.device_id` 为 **INT**（最大 **2147483647**），**`yy * 10^9 + 九位`** 会越界。
- **处理**：灯具使用 **`nextUniqueIntScopeDeviceId`**（`yy * 10^7 + [0,10^7)`）。洞外雷达列类型以库为准（后续确认为 BIGINT 后改回 `yy * 10^9` 方案，见下条）。

## 2026-03-24 隧道复制：`tunnel_lamps_terminal.device_id` 越界说明与修正

- **原因**：报错 **不是撞号**，而是 **`device_id` 列实际比 INT 更窄**（常见 **MEDIUMINT** 上限 **8388607** 或 **SMALLINT** **32767**）。原 **`yy * 10^7 + …`** 约 **2.6×10⁸**，必然 **Out of range**。
- **`tunnel_devicelist` 为 `bigint(12)`** 时 devicelist 恢复 **`nextUniqueLong11`**（`yy * 10^9 + [0,10^9)`）。
- **灯具**：改为 **`nextUniqueLampDeviceId`**，在 **`[1, LAMP_DEVICE_ID_MAX]`** 随机；库为 **`smallint(5) unsigned zerofill`** 时 **`LAMP_DEVICE_ID_MAX = 65535`**。

## 2026-03-24 隧道管理：公司列表菜单与接口

### 后端 `zt_project_tunnel/scsdky-admin`

- **`TunnelCompanyAddDto`**：`tunnelName`（`@NotBlank`）。
- **`TunnelNameResultService` / `TunnelNameResultServiceImpl`**：`listLevel1Companies()` 查询 `level = 1` 按 `id` 升序；`addLevel1Company` 写入 `parent_id = 0`、`level = 1`、`status = 0`，`tunnel_name` 全局去重（与复制隧道校验一致）。
- **`TunnelCompanyController`**：`GET /tunnel/company/list`（`tunnel:list:view`）；`POST /tunnel/company`（`system:tunnel:update`）。

### 前端 `zttunnel-admin`

- **`src/router/index.js`**：隧道管理下新增子路由 **`/tunnel/company`**，标题 **公司列表**，权限 **`tunnel:list:view`**，图标 **`OfficeBuilding`**（置于「隧道列表」之前）。
- **`src/api/tunnel.js`**：`getTunnelCompanyList`、`addTunnelCompany`。
- **`src/views/tunnel/company/TunnelCompanyList.vue`**：表格展示 level=1 数据；具备 **`system:tunnel:update`** 时显示 **新增** 弹窗录入公司名称。

## 2026-03-24 隧道复制简化 + 绑定列表

### 复制规则（`TunnelCopyServiceImpl`）

- **边缘控制器**（type=1）与**电能终端**（type=2）的 **`tunnel_devicelist.device_id`** 均使用 **9916** 开头、共 **12** 位十进制占位，共用 **`nextUnique9916DevicelistId`** 去重（`991600000000`～`991699999999`）。
- **灯具、洞外雷达、`t_tunnel_device`**：复制时 **保留源 `device_id`**；若与全局唯一约束冲突需人工处理。

### 绑定列表（后端 `scsdky-admin`）

- **`PlaceholderEdgeTunnelVo`**（含 **deviceTypeId / deviceTypeName**）、**`TunnelPlaceholderBindServiceImpl`**：查询 type **1、2** 且 `device_id` 在 **9916 段** 的 devicelist，按 **link.type** 关联四级隧道与隧道群。
- **`TunnelBindController`**：`GET /tunnel/bind/placeholder-edge/list`（`tunnel:list:view`）。

### 前端 `zttunnel-admin`

- **`src/router/index.js`**：**绑定列表** 路由 **`/tunnel/bind`**。
- **`src/api/tunnel.js`**：`getPlaceholderEdgeBindList`。
- **`TunnelBindList.vue`**：表格 + 跳转 **隧道参数**（`?tunnelId=`）。

## 2026-03-24 绑定列表：tunnelinfo 反查 + 对齐 zt_tunnel_web 列表

### 方案说明

- 从 **`tunnel_devicelist_tunnelinfo`** 筛选 **`devicelist_id` ∈ [991600000000, 991699999999]**（与 BIGINT「以 9916 开头的 12 位数」一致，优于字符串 `LIKE`，且更易用索引），再对 **`tunnel_id` 去重**，可覆盖边缘+电能两条 link，**不易遗漏**。
- **`PlaceholderEdgeTunnelVo`**：字段为 **线路名**（level2）、**隧道名**（level3 群）、**隧道方向**（level4 名）、**创建时间**、**tunnelLevel4Id**，对齐 **zt_tunnel_web** `table5` 隧道管理列表。
- **`TunnelPlaceholderBindServiceImpl`**：不再从 `tunnel_devicelist` type 反查，改为仅依赖 **tunnelinfo**。

### 前端

- **`TunnelBindList.vue`**：**绑定** → **`/device/management?tunnelId=&tab=edgeController`**；**编辑** → **`/tunnel/param?tunnelId=`**。
- **`DeviceManagement.vue`**：支持 **`tunnelId`、`tab`** 查询参数，进入时自动选隧道并切换 Tab（`edgeController` / `powerTerminal` / `lampsController` / `approachLamps`）。
- **`TunnelList.vue`**：复制说明与成功提示文案与上述规则一致。

## 2026-03-24 后端：placeholder-edge 列表 SQL + 批量名称查询

- **`zt_project_tunnel` `TunnelPlaceholderBindServiceImpl`**：`tunnel_devicelist_tunnelinfo` 改为 **`SELECT DISTINCT tunnel_id`**（Mapper 新方法）；`t_tunnel_name_result` 用 **3 次 `listByIds`** 替代按隧道循环 `getById`，消除 N+1。
- **建议库表**：`tunnel_devicelist_tunnelinfo(devicelist_id)` 索引。

## 2026-03-24 隧道列表操作列与「设备绑定」全页工作台

- **后端**：`GET /tunnel/bind/placeholder-tunnel-ids`；`devicelist_id` 用 `CAST(...AS CHAR) LIKE '9916%'` 筛 distinct `tunnel_id`；**不修改**树实体字段。
- **前端路由**：`/tunnel/device-bind?tunnelId=`（`hidden`，权限 `tunnel:param:view`）；**`TunnelBindList.vue`** 绑定列表页（修复 Vite 缺文件）。
- **`DeviceBindWorkspace.vue`**：多 Tab + 各页签提交/刷新。
- **后端（zt_project_tunnel）**：`/tunnel/power/list` 支持 `tunnelId`，修复电表 Tab 仅传隧道 ID 时无数据问题。
- **`TunnelList.vue`**：并行拉树与 `placeholder-tunnel-ids`，**设备绑定** 用 Set 匹配；**保留编辑，移除删除**。
- **2026-03-24 隧道列表内嵌子页**：始终 ` /tunnel/list`，用 `?panel=param|device|device-bind|edit` 切换；列表区 `v-show` 保持挂载，**返回** `replace` 清空 query **不重新拉树**（除复制/导入/保存等主动 `getTunnelList`）；树默认**全部展开**（`allExpanded` 默认 true + 数据加载后 `expandAllTreeRows`）。**编辑** 改为子视图表单。`TunnelBindList` 跳转同上。**`DeviceManagement`** 增加 `embedded` 隐藏级联；**`DeviceBindWorkspace`** 增加 `embedded` + `@close`。
- **`PowerMeterTable.vue`**：`defineExpose({ loadData })`。

## 2026-03-24 设备绑定默认 Tab + 公司列表独立菜单

- **`DeviceBindWorkspace.vue`**：进入设备绑定后 **默认选中「边缘控制器」**（`activeTab` 初值 `edge`）；支持查询参数 **`?bindTab=`** 覆盖（`param` | `edge` | `power` | `lamps` | `approach` | `meter`），与路由 `watch` 同步。
- **`src/router/index.js`**：新增顶级 **公司管理**（`/company`，meta `title: 公司管理`，`icon: Briefcase`），子路由 **`/company/list`**（`CompanyList`，组件仍为 `TunnelCompanyList.vue`）；从 **隧道管理** 下移除原「公司列表」子路由，隧道菜单仅保留隧道相关项。

## 2026-03-24 移除绑定列表菜单；设备绑定隧道 ID 不出现在 URL

- **`src/router/index.js`**：删除 **`/tunnel/bind`**（绑定列表）路由；删除 **`TunnelBindList.vue`**。
- **`src/utils/deviceBindContext.js`**（新）：用 **`sessionStorage`** 保存当前设备绑定对应的隧道 ID（与同源标签页刷新仍可恢复）；离开子页或返回时清除。
- **`TunnelList.vue`**：进入「设备绑定」仅 **`?panel=device-bind`**，不再带 `tunnelId` / `tunnel_id`；子组件传入 **`tunnel-id-prop`**；监听子面板切换，离开 **device-bind** 时 **`clearDeviceBindContextTunnelId`**。
- **`DeviceBindWorkspace.vue`**：嵌入模式只认 **`tunnelIdProp`**；独立页 **`/tunnel/device-bind`** 若地址栏仍带旧版 `tunnelId`/`tunnel_id`，**首次加载**写入 session 并 **`replace` 去掉该查询参数**；返回列表时清除 session。提示文案改为「未选择隧道或会话已失效…」。
- **复制成功提示**：去掉已删除的「绑定列表」文案。

## 2026-03-24 设备绑定：电表表格铺满；边缘控制器「绑定」弹窗

- **`PowerMeterTable.vue`**：表格外包 **`table-wrapper table-responsive`**，列改为 **`min-width` + `table-layout="fixed"`**，与电能终端等 Tab 一致横向铺满；小屏仍可横向滚动。
- **`DeviceBindWorkspace.vue`**：`.bind-tabs` 对 **`el-tabs__content` / `el-tab-pane`** 设 **`width:100%`**、`min-width:0`，避免子页内容挤在左侧；说明文案改为边缘控制器用「绑定」弹窗仅改设备号。
- **`EdgeControllerTab.vue`**：新增 **`deviceBindMode`**（设备绑定页传入 **`device-bind-mode`**）。为真时操作列按钮为 **「绑定」**，打开 **「绑定设备号」** 弹窗（仅一项输入）；保存时对原行 **`spread` + 新 `deviceId`** 调 **`updateDevicelist`**（与编辑相同接口，其余字段随行数据保留）。**设备管理**页未传该属性，仍为 **「编辑」** 全量表单。

## 2026-03-24 设备绑定-边缘控制器列表对齐 zt_tunnel_web

- **`EdgeControllerTab.vue`**（**`deviceBindMode`**）：表格列与 **zt_tunnel_web** `table5.vue` 隧道弹窗内「边缘控制器」一致：**设备号、状态（在线/离线/故障 Tag）、最后数据（`lastUpdate` 格式化）、雷达（`radarStatus`）、亮度仪（`brightnessMeterStatus`）、主板固件（`softVer`）、操作（控制 / 绑定，无删除）**。数据仍来自 **`POST /tunnel/get/devicelist`**，`body: { tunnelId, type: 1 }`（即 `getEdgeControllerList`），与旧项目 **`getDevicelist({ tunnelId, type: 1 })`** 同源。
- **绑定保存**：校验改为依赖 **`deviceId`**（主键），不再误用不存在的 **`id`**。

## 2026-03-25 边缘绑定：rebind 级联、`lastUpdate` 修复、绑定页表格与档案编辑

- **`src/utils/devicelistPayload.js`**（新）：`sanitizeDevicelistForUpdate` 去掉 **`lastUpdate`**、VO 扩展字段（雷达/亮度仪/电表数等），避免更新接口 Jackson 报错。
- **`src/api/tunnelParam.js`**：`rebindDevicelist` → **`POST /tunnel/rebind/devicelist`**。
- **`EdgeControllerTab.vue`**（**`deviceBindMode`**）：列表增加 **终端名称、桩号、区段、回路、信号强度、在线状态、设备状态、最后数据、雷达、亮度仪、主板固件、网络信息**；操作 **控制 | 绑定 | 编辑**。「绑定」走 **`rebindDevicelist`**；「编辑」打开档案弹窗（不改设备号），提交走 **`updateDevicelist(sanitize(...))`**。非绑定模式逻辑不变。
- **`DeviceBindWorkspace.vue`**：顶部说明与上述行为一致。
- **后端 `zt_project_tunnel`**：`TunnelDevicelist` **`lastUpdate` Jackson 格式**；**`TunnelDevicelistMapper#updateDevicePrimaryKey`**；**`rebindDeviceId`** 事务 + **`TunnelNameController` `/tunnel/rebind/devicelist`**（详见工程 **`log.md`**）。

## 2026-03-24 隧道列表 level-4：查看 / 编辑 / 绑定 + `tunnel-detail` 内嵌工作台

- **`TunnelList.vue`**：**具体隧道（level=4）** 操作列仅保留 **查看**、**编辑**、**绑定**（**绑定** 仍仅在 `placeholderTunnelIdSet` 命中时显示）；操作列宽度改为 **280**；复制说明中「设备绑定」改为 **「绑定」工作台**。
- **路由 query**：**查看** → `?panel=tunnel-detail&mode=view&tunnelId=`；**编辑** → `mode=edit`；**绑定** 仍为 `?panel=device-bind` + session 隧道 ID。
- 移除 level-4 原 **隧道参数 / 设备管理** 独立子区块（`panel=param` / `panel=device`）及对应 `TunnelParamForm` / `DeviceManagement` 内嵌；改由 **`DeviceBindWorkspace`** 两份实例：`device-bind` 时 **`edge-device-bind-mode=true`**、默认 **边缘** Tab；`tunnel-detail` 时 **`edge-device-bind-mode=false`**、默认 **隧道参数** Tab，**查看** 传 **`read-only`**，**编辑** 可保存与各设备 Tab 操作列（与此前只读改造一致）。
- **非 level-4** 的 **编辑**（树节点基本信息子视图 `panel=edit`）逻辑未改。

## 2026-03-24 隧道列表操作列仅 level-4 显示

- **`TunnelList.vue`**：**level 1～3** 操作列不再展示 **编辑 / 添加子级 / 复制** 等按钮，仅 **level 4** 显示 **查看 / 编辑 / 绑定**；操作列宽度改为 **200**。（`handleEdit`、`handleAddChild`、`openCopyDialog` 及 `panel=edit`、复制对话框等脚本仍保留，便于需要时从其它入口或手工 URL 复用。）

## 2026-03-24 设备 Tab 列表列补全（查看 / 编辑 / 绑定共用 `DeviceBindWorkspace`）

- **`deviceStatus.js`**：导出 **`formatDisplayTime`**（列表时间统一格式化）。
- **`EdgeControllerTab.vue`**：**绑定 / 非绑定** 共用同一套数据列（终端名称、桩号、区段、回路、信号、在线 Tag、设备状态文本、最后数据、雷达、亮度仪、固件、网络）；**仅非绑定** 显示 **设备密码**；`EdgeControllerTab` 改从工具库引用 `formatDisplayTime`；表格横向 `min-width` 加大。
- **`PowerTerminalTab.vue`**：在原有列基础上增加与 `TunnelDevicelistVo` 一致的 **终端名称、区段、回路、设备状态、最后数据、固件、网络、工作模式、雷达、亮度仪** 等列；表格 `min-width` 加大。
- **`LampsControllerTab.vue`**：增加 **终端名称、灯具序号、区段2、设备状态文本、蓝牙编号、雷达设备号、电力载波、更新时间、版本号**；引入 `formatDisplayTime`；表格 `min-width` 加大。
- **`ApproachLampsTab.vue`**：设备号/安装里程兼容 **DTO 字段**（`deviceNo`、`installationMileage`）；增加 **区段名称、蓝牙强度**；最后更新用 **`formatDisplayTime(lastUpdate || lastTime)`**；状态 Tag 兼容 **`online` / `status`**。
- **`PowerMeterTable.vue`**：增加 **记录ID、序号、是否虚拟表** 列；表格 `min-width` 略增。

## 2026-03-24 隧道工作台：无 tunnelId 的 URL、顶部隧道名称、参数 Tab 置顶

- **`src/utils/tunnelDetailContext.js`**（新）：查看/编辑子页的 **隧道 ID、名称** 仅存 **sessionStorage**，query 仅保留 `panel=tunnel-detail`、`mode=view|edit`。
- **`deviceBindContext.js`**：`setDeviceBindContextTunnelId(id, tunnelName?)` 可同时写入 **隧道名称**；新增 **`peekDeviceBindTunnelName`**；清除 ID 时一并清除名称。
- **`TunnelList.vue`**：`goTunnelView` / `goTunnelEdit` 不再带 **`tunnelId`**；`subTunnelId` 来自 **session + `tunnelDetailContext` 同步**；`closeListSubview` 与面板切换时清理详情 session；**旧书签** 中含 `tunnelId` 时迁入 session 并 **`replace` 去掉**；**`tunnelDetailOpenKey`** 驱动子组件将「隧道参数」滚到顶部。
- **`DeviceBindWorkspace.vue`**：移除「查看模式：参数与设备列表仅供浏览…」**`el-alert`**；卡片顶部 **`el-alert`** 展示 **当前隧道：{名称}**（不传 `tunnelId`）；**`tunnelBannerName`** / 独立页 **`peekDeviceBindTunnelName`**；**`scrollParamTopKey` + `tunnelId`** 时在嵌入且默认 **`param`** 下切换到隧道参数并 **`scrollIntoView`** 置顶。
- **编辑能力**：各设备 Tab 仍走与 **zt_tunnel_web** `getDevicelist` / 各 `update*` 一致的后端路径（`tunnelParam` 等），**未新增接口**。

## 2026-03-24 隧道详情：level-1～4 全称 + Tab 卡片固定视口高度内滚动

- **`src/utils/tunnelTree.js`**（新）：**`getTunnelPathLabels`**、**`formatTunnelHierarchyPath`**（默认 ` / ` 拼接）；**`buildTunnelWorkspaceMeta(rawData, row)`** 生成 **`{ pathFromRoot, level }`**，供 **`setTunnelDetailWorkspace(id, leafName, meta)`**、**`setDeviceBindContextTunnelId(id, leafName, meta)`** 第三参数使用，使「当前隧道」展示 **自管理单位到具体隧道的全称**。
- **`DeviceBindWorkspace.vue`**：**`displayTunnelBanner`** 优先 **session `pathFromRoot`**（详情 `peekTunnelDetailWorkspace`、绑定 **`peekDeviceBindTunnelMeta`**），否则回退单名称；**`device-bind-workspace--fill-height`**：`height: calc(100vh - 60px - 40px)`，卡片与 **Tab 内容区 flex 撑满**，**各 `el-tab-pane` 内部 `overflow-y: auto`**；长全称 **`el-alert` 标题可换行**。
- **列表页接入**：在 **`goTunnelView` / `goTunnelEdit` / `goDeviceBind`** 中传入 **`buildTunnelWorkspaceMeta(rawData.value, row)`**（与 **`tunnelDetailContext` / `deviceBindContext` 已有 meta 字段一致**）。旧 session 无 path 时仍显示 **`tunnelName`**。

## 2026-03-24 恢复 `TunnelList.vue` 约定交互

- **`TunnelList.vue`** 自旧版单卡列表恢复为：**默认 `allExpanded=true`**，加载后 **`expandAllTreeRows`**；**仅 level=4** 操作列展示 **查看 / 编辑 / 绑定**（绑定仍依 **`placeholderTunnelIdSet`**）；**level 1～3 无操作按钮**。
- 保留 **Excel 新建隧道 / 下载模板、占位隧道 API、`DeviceBindWorkspace` 子视图、session 隧道上下文、`buildTunnelWorkspaceMeta` 全路径** 等与 `tunnelDetailContext` / `deviceBindContext` 一致逻辑。

## 2026-03-24 隧道查看/编辑/绑定：仅参数 Tab 提交 + 子布局填满视口、抑制外层滚动

- **`DeviceBindWorkspace.vue`**：各设备 Tab 取消顶部 **提交**；**隧道参数** Tab 内 **`param-tab-scroll` + `param-tab-footer`**，**提交** 置于 Tab 内容区 **右下角**（非只读显示）。**`el-tab-pane`** 增加 **`tab-pane--param`**：该页 **内部滚动 + 底栏固定**；**其余 Tab** **`overflow-y: auto`**，在 Tab 区域内滚动。
- **`TunnelList.vue`**：存在子面板（`listSubPanel`）时根节点增加 **`tunnel-sublayout-fill`**：**`height/max-height: calc(100vh - 60px - 40px)`**、纵向 flex、**`overflow: hidden`**；**嵌入** 的 **`device-bind-workspace--fill-height`** 使用 **`flex:1; min-height:0; height:auto !important`**，与工作台顶栏分担视口高度。**`panel=edit`** 时 **`list-subview-card`** 内表单区域 **`overflow-y: auto`**，页脚固定。
- **`layout/index.vue`**：**`.content`** 增加 **`min-height: 0`**、**`display: flex; flex-direction: column`**，保证主内容区子页面可用 flex 正确限高，减少整页与卡片高度叠加导致的 **`.content` 外层滚动条**。

## 2026-03-24 设备 Tab / 电表列表：可拖动「显示列」侧栏 + 操作列固定宽度

- **`src/components/device/DeviceTableColumnSplit.vue`**（新）：左侧 **显示列**（`el-checkbox-group`），与表格之间 **约 14px 宽分隔条**（渐变色 + 中部 **`split-handle-grip`**），**`col-resize`** 拖动调节侧栏宽度（0～380px，默认 200）；**`storageKey`** 下将宽度与已选列持久化到 **localStorage**（键名 **`deviceColSplit:w:` / `deviceColSplit:v:`** 前缀）。**`.split-table-area`** 内 **`table-wrapper`** 参与 **flex 限高**。全局 **`body.device-col-split--dragging`** 在拖动时保持 **`col-resize`** 与 **禁止选中**（**`styles/index.scss`**）。
- **`EdgeControllerTab.vue` / `PowerTerminalTab.vue` / `LampsControllerTab.vue` / `ApproachLampsTab.vue`**：主表外包 **`DeviceTableColumnSplit`**，数据列按 **`visibleColumns`** 与选项 **key** 切换显示；**操作列** 仍为 **`fixed="right"`**，宽度改为固定 **`width`（px）**（边缘绑定/非绑定、电能、灯具、引道一致策略），避免与横向滚动列一起被挤压。
- **`PowerMeterTable.vue`**：工具栏仍在上方；**表格区域** 同样接入 **`DeviceTableColumnSplit`**，操作列 **`width="176"`**。

## 2026-03-24 设备表：不撑破卡片 + 内部横向滚动条 + 右侧固定列

- **`DeviceTableColumnSplit.vue`**：根节点 **`max-width:100%` / `overflow:hidden` / `flex:1`**；**`.split-table-area`** **`overflow:hidden`**；**`.table-wrapper`** **`overflow:hidden`**（曾误用外层横向滚动，易与 **`fixed`** 列冲突）。
- **`DeviceBindWorkspace.vue`**：非「隧道参数」**`el-tab-pane`** 增加 **`min-width:0`**、纵向 flex，**直接子节点** **`flex:1; min-height:0; min-width:0`**，使 Tab 内表格区在 flex 链上可被限宽。
- **`PowerMeterTable.vue`**：**`.table-header`** **`flex-shrink:0`**，避免与下方分割区抢高。

## 2026-03-24 修正：fixed 操作列 + 不再撑破卡片（Element Plus 滚动容器）

- **原因**：在 **`el-table` 根节点** 上设 **`min-width: 1680px`** 等会把**整个表格组件**撑宽，横向滚动落在外层（Tab/卡片），**`fixed="right"`** 相对错误滚动视口，操作列需拖到底才看见。
- **`DeviceTableColumnSplit.vue`**：**`.table-wrapper`** 改为 **`overflow: hidden`**（不再外层 **`overflow-x:auto`**）；为 **`el-table` 内 `el-scrollbar` 横向轨道**（**`.el-scrollbar__bar.is-horizontal` / `__thumb`**）加粗、主题色；表格本身 **`width:100%; max-width:100%`**。
- **`EdgeControllerTab` / `PowerTerminalTab` / `LampsControllerTab` / `ApproachLampsTab` / `PowerMeterTable`**：**`el-table`** 增加 **`flexible`**、**`scrollbar-always-on`**，**`style="width:100%"`**；**删除**对各表 **`el-table` 根的 `min-width`** 样式，列宽仍由各列 **`min-width`** 与表格内部布局计算，**横向滚动仅在 `ElTable` 的 `body-wrapper` 内 `ElScrollbar`**。

## 2026-03-24 设备列表：横向滚动条对齐 Tab 内容区底部

- **`DeviceBindWorkspace.vue`**：非「隧道参数」**`el-tab-pane`** 由 **`overflow-y: auto`** 改为 **`overflow: hidden`**，列表高度由内部 **flex** 撑满 Tab 内容区，避免整页竖向滚动导致横条上下跳动。
- **`DeviceTableColumnSplit.vue`**：**`.table-wrapper.table-responsive`** 改为 **`display:flex; flex-direction:column`**，与 **`flex:1; min-height:0`** 配合，把高度传给 **`el-table`**。
- **`EdgeControllerTab` / `PowerTerminalTab` / `LampsControllerTab` / `ApproachLampsTab` / `PowerMeterTable`**：**`el-table`** 增加 **`height="100%"`**，表体区域占满列表视口，**横向滚动条固定在列表区最下沿**，切换 Tab 时底边位置一致。
- **`DeviceManagement.vue`**：**`.tab-panels`** 增加 **`min-height:55vh`** 与纵向 **flex**；**`.tab-panel`** **`flex:1; min-height:0`**，与隧道工作台复用同一套表格时 **`height="100%"`** 可解析。

## 2026-03-24 隧道列表：顶部筛选区仅保留「搜索」「重置」

- **`src/views/tunnel/list/TunnelList.vue`**：**筛选表单**（隧道名称、层级、状态）不变；**工具栏按钮** 仅保留 **搜索**、**重置**。移除：Excel **新建隧道** / **下载模板**、**新增管理单位**、**全部展开/收起**、**重新计算里程**。
- **代码清理**：删除上述能力对应的 **`el-upload` / `importTunnelExcel` / `recalculateTunnelMileage` / `handleRecalculateMileage` / `toggleExpandAll`**；移除已无入口的 **新增管理单位 `el-dialog`** 及 **`tunnelFormRef` / `dialogVisible` 等**；嵌入编辑保存仍走 **`tunnelFormEmbedRef` + `handleSubmit`**。树表默认仍 **`default-expand-all` 与 `allExpanded`**，加载后 **`expandAllTreeRows`** 行为不变。

## 2026-03-24 隧道列表筛选同一行 + 隧道参数页常驻级联面板

- **`TunnelList.vue`**：**筛选区** 使用 **`flex-wrap: nowrap`**、**`align-items: center`**，**搜索 / 重置** 与隧道名称、层级、状态 **同一行**；**`.search-form`** 增加 **`overflow-x: auto`**，窄屏可横向滚动；工具栏 **`el-form-item`** 设为 **`flex: 0 0 auto`** 避免被挤占整行。
- **`TunnelParam.vue`**：下拉 **`el-cascader`** 改为 **`el-card` + `el-cascader-panel`**，**始终展开** 多级列；卡片标题行保留 **参数保存 / 关闭**；数据未载入时用 **`el-skeleton`**，无数据用 **`el-empty`**。**默认选中**：仍由 **`stores/tunnel.js` 的 `loadTunnelData`** 在路径为空时选中 **首个 level=4 隧道** 并写入 **`currentTunnelPath`**，面板 **`model-value`** 与之同步即可展开并高亮。

## 2026-03-24 隧道参数表单：四列对齐 zt_tunnel_web

- **`src/views/tunnel/param/components/TunnelParamForm.vue`**：可编辑区改为 **四列网格**，每列 **标题 + 左标签 / 右控件** 竖向紧凑排列，对齐原 **`zt_tunnel_web` `table5.vue`** 结构：
  1. **隧道基本参数**（含线路/隧道编号只读、总里程、调试/引道/雷达、洞外开灯桩号、入库限值、**备用1～5**）；
  2. **计算参数**（物理区段、回路功率、车流/速度/调光打折、分时、折减、洞外与预设亮度、电压与功率报警、全洞亮度设计、路面系数、**工作模式**、当前洞外亮度只读、车流上报重复次数）；
  3. **灯具参数**（额定功率、亮灯比值、照射面积、异常时长、数量、维护系数、光通量、空间系数、布灯方式、碳排放三项）；
  4. **预亮灯配置控制**：**20 行**「等待时长/持续时长」，与旧版一致 **`preOnConfig`** 为 40 个逗号分隔数值，**加载解析 / 保存序列化**；请求体增加 **`preOnConfig`**。
- **响应式**：宽度不足时 **4→2→1 列**；列内 **`max-height` + 纵向滚动**。控件统一 **`size="small"`** 以压缩行高。
- **行内占比**：**左标签约 64%**、**右输入区约 36%**（`flex-basis` + `max-width`），长标签更易读、输入区适当缩窄。
- **预亮灯配置**：展示规则与 **zt_tunnel_web table5** 一致——单元格以 **数字 0** 占位；解析时空串/缺省/非数字统一为 **0**；无配置或长度非 40 时整表 **0 填充**；序列化时每格输出 **0 的字符串**（与旧版 `|| '0'` 一致）。
- **`TunnelParamForm.vue`**：表单行 **左侧标签 12px**；**右侧输入/选择/数字框内文字 14px**（覆盖 `size="small"`）；预亮灯表内数字框同样 **14px**。

## 2026-03-27 边缘控制器：设备状态 与旧版对齐

- **`EdgeControllerTab.vue`**：`/tunnel/get/devicelist` 返回的 **`deviceStatus` 常为 null**（与旧版一致）；旧版 **「状态」** 实际用 **`online` + getSee(1/0→正常/异常)**。逻辑已统一到 **`deviceStatus.js`** 的 **`getDevicelistDeviceStatusText`**。
- **雷达 / 亮度仪**：仍完全来自后端 **`setEdgeControllerStatus`**（`TunnelEdgeComputeData.edgeComputeStatus0` 位运算）；新旧环境 **lastUpdate、工况数据、库是否一致** 会导致 **「正常」与「异常」** 不同，非前端口径错误。

## 2026-03-24 电能终端列表：安装里程、状态回退、去掉工作模式

- **`src/utils/deviceStatus.js`**：新增 **`getDevicelistDeviceStatusText(row)`**，与边缘侧一致：**`deviceStatus` 非空则用之**，否则按 **`online`** **1→正常、0→异常**。
- **`EdgeControllerTab.vue`**：本地 **`getEdgeDeviceStatusText`** 删除，改为从 **`deviceStatus.js`** 引入 **`getDevicelistDeviceStatusText`**。
- **`PowerTerminalTab.vue`**：
  - **设备状态** 使用 **`getDevicelistDeviceStatusText`**。
  - **去掉「工作模式」** 列及 **`columnOptions` 中的 `workmode`**。
  - **去掉「雷达」「亮度仪」** 列（电能终端列表不再展示 **`radarStatus` / `brightnessMeterStatus`**；边缘控制器列表保留）。
  - **安装里程**：旧版电能终端表将 **「安装里程」** 绑在 **`device_password`**（**`devicePassword`**）；列表与编辑表单项展示名 **「安装里程」**（仍写 **`device_password`**）。

## 2026-03-24 灯具终端：设备状态与旧版 table5 一致

- **`src/utils/deviceStatus.js`**：新增 **`getLampCommunicationStateText`**、**`getLampCommunicationStateTagType`**，按 **`communicationState`**：**0→正常、1→异常**；空为 **—**；其它值原样展示（对齐旧版 **`getSee`**）。
- **`LampsControllerTab.vue`**：**设备状态** 列改为仅依据 **`communicationState`**，不再展示接口字符串 **`deviceStatus`**；移除与之一致的重复列 **「通信状态」**；导出 CSV 列名与文案同步；编辑表单项 **通信状态** 改为 **设备状态**。
- **`LampsControllerTable.vue`**（隧道参数内嵌表）：设备状态列改用上述工具函数，避免非 0/1 误标为异常。

## 2026-03-24 灯具/引道灯/电表列表：工具栏「新增」

- **`LampsControllerTab.vue`**、**`ApproachLampsTab.vue`**：表格上方右侧 **`list-toolbar`** 增加 **新增**（**`v-permission="device:add"`**），仍调用已有 **`handleAdd`**（**`addLampsTerminal` / `addApproachLamps`** 与弹窗逻辑不变，对齐 **`DeviceManagement`** 顶栏行为）。
- **`PowerMeterTable.vue`**：工具栏 **右对齐**（**`justify-content: flex-end`**），按钮顺序 **刷新、导出、新增**（**新增** 在最右侧）；**`handleAdd`** 仍走 **`addPowerEdgeComputing`**（**`/tunnel/saveOrUpdate/power`**）。

## 2026-03-24 灯具终端：工具栏刷新/导出 + 新增走对端接口

- **`LampsControllerTab.vue`**、**`ApproachLampsTab.vue`**：**`list-toolbar`** 增加 **刷新**、**导出**（与 **新增** 同为右对齐、`gap`）；**只读** 模式下仍可对 **刷新 / 导出**，**新增** 仅在非只读时显示。
- **根因**：新增灯具误调 **`/device/saveOrUpdate`**（通用设备表），不会写入 **`tunnel_lamps_terminal`**。旧版 **`zt_tunnel_web`** 与后端均为 **`POST /tunnel/update/device/lamp`**（**`uniqueId` 为空** 时服务层走新增）。
- **`src/api/device.js`**：**`addLampsTerminal`** 改为请求 **`/tunnel/update/device/lamp`**（与 **`tunnelParam.updateLampsTerminal`** 同路径）。
- **`LampsControllerTab.vue`**、**`LampsControllerTable.vue`**：保存 **新增/编辑** 均调用 **`updateLampsTerminal`**；提交体显式带 **`uniqueId`（仅编辑）**、**`deviceId` 数值**、**`deviceNumStr`**（桩号字符串）、**`dimmingType`**（由 **随车/无极** 推导），避免 **`id` 等** 干扰后端 DTO。

## 2026-03-24 灯具新增/编辑弹窗对齐 zt_tunnel_web table5

- **`src/utils/lampMileage.js`**：**`mileageStrToDeviceNum`**，与旧版 **`hexToDec`** 一致（普通十进制；含 **`+`** 时按旧版拼接十六进制再解析）。
- **弹窗**：改为 **单列**；字段顺序与 **`table5.vue`** **`dengju` 的 `tableItemTh`** 一致：**设备号 → 安装里程（`deviceNumStr`）→ 所属回路 → 设备状态 / 工作状态（选项顺序 **异常 / 正常**）→ 信号强度 → 区段 / 区段2（**`GET /device/getZone`**，`nodeName` / `nodeCode`）→ 是否安装雷达 → 调光类型（**`dimmingType` 0/1**，不再用表单里的 **`deviceProperty` 文案**；提交时仍按旧后端习惯带上 **`deviceProperty` 字符串**）→ 雷达状态（**仅已装雷达时显示**，**`ldStatus` 为字符串 `'0'`/`'1'`**）→ 蓝牙编号。
- **`LampsControllerTab.vue`**、**`LampsControllerTable.vue`**、**`LampsManagement.vue`（仅新增弹窗）**：统一上述交互与 **`updateLampsTerminal`** 提交组装；列表列 **安装里程** 展示 **`deviceNumStr` 优先**。

## 2026-03-24 对照旧版新增逻辑审计 + 安装里程占位符

- **安装里程输入框**：去掉 **`LampsControllerTab.vue`**、**`LampsManagement.vue`（编辑/新增）** 等处 **`placeholder` 提示文**；**`LampsControllerTable.vue`** 已为空占位。
- **引道灯控制器 `ApproachLampsTab.vue`**（与后端 **`TunnelApproachLampsTerminal`**、旧 **`table5` yindao** 对齐）：
  - 保存体改为 **`deviceNo`、`installationMileage`、`zone`、`status`（0 离线 / 1 在线）、`bluetoothStrength`、`version`、`tunnelId`、`id`（仅更新）**，不再误传 **`deviceId` / `deviceNum` / `online`** 导致不落库。
  - 弹窗改为 **单列**；区段 **`GET /device/getZone`**（**`getZoneList`**）；去掉旧版不存在的 **回路编号** 列与表单项；表头文案对齐 **区段、状态、版本号、最后数据**。
- **其余新增与旧版对照（结论）**：
  - **灯具终端**：**`POST /tunnel/update/device/lamp`**（**`updateLampsTerminal`**）— 与旧 **`getUpdateDeviceLamp`** 一致。
  - **电表（`PowerMeterTable`）**：**`addPowerEdgeComputing` → `/tunnel/saveOrUpdate/power`** — 与旧 **`saveOrUpdatePower`** 一致。
  - **电能终端（`PowerTerminalTable` / `PowerTerminalTab`）**：**`POST /device/saveOrUpdate`**（**`addDevicelist`**）；旧版 **电能列表 Tab 顶部无「新增」**，若与产品不一致可再收敛。
  - **边缘控制器**：**`addDevicelist`**；旧版 **边缘 Tab 无「新增」**。

## 2026-03-24 电表 `PowerMeterTable`：隐藏记录 ID + 弹窗/保存对齐旧版 table5

- **列表**：去掉 **「记录ID」**（**`uniqueId`**）列及列配置项；**是否启用** 列同时兼容 **`is_enabled` / `isEnabled`**；导出同理。
- **新增/编辑弹窗**：与 **`zt_tunnel_web` `setDianbiao1TableColumns`** 一致——**单列**、标题 **「新增」/「修改」**；字段顺序：**地址号 → 电能终端设备号 → 电能终端名称 → 方向（选项顺序 左线 / 右线）→ 电能表厂商 → 是否启用（表单字段 `is_enabled`）→ 最后更新时间（禁用）**；去掉旧版不存在的 **序号、虚拟表** 表单项。
- **保存**：统一 **`POST /tunnel/saveOrUpdate/power`**（**`saveOrUpdatePower`**），提交体按 **`saveOrUpdatePowerFunction`** 规范 **`vendorId`、`is_enabled` 与 `isEnabled`**；**编辑** 时附带 **`uniqueId`**；**新增** 不传 **`uniqueId`**（后端分配序号等与旧逻辑一致）。

## 2026-03-24 电表列表：去掉「虚拟表」列

- **`PowerMeterTable.vue`**：表格与 **显示列** 配置中移除 **`isVirtual`（虚拟表）**；后端字段仍可存在，仅界面不展示。

## 2026-03-24 隧道参数菜单：全部只读

- **`TunnelParam.vue`**：**`TunnelParamForm`** 固定 **`read-only`**（**`el-form`** 整表禁用，与原有 **`saveParams` 内只读判断** 一致）；去掉 **「参数保存」** 及 **`save`** 相关状态；**选择隧道** 级联与 **关闭** 仍可用。**设备绑定 / 隧道详情工作台** 中的 **`DeviceBindWorkspace`** 仍按自身 **`readOnly`** 属性控制，不受影响。

## 2026-03-24 设备列表工作台：左树多选 + 新接口分页/导出

### 后端（`zt_project_tunnel` / `scsdky-admin`）

- 新增 **`DeviceWorkspaceQueryDto`**（已存在）：`tunnelIds`、`deviceType`、**`keyword`**、**`pageNum` / `pageSize`**。
- **`TunnelLampsTerminalService.listDeviceLampByTunnelIds`** + **`TunnelLampsTerminalServiceImpl`**：多隧道循环 **`getDeviceLamp`**，无边缘控制器隧道跳过，**`uniqueId`** 去重。
- **`DeviceWorkspaceListService` / `DeviceWorkspaceListServiceImpl`**：按 **`deviceType`** 聚合——边缘/电能走 **`TunnelDevicelistService.listDevicelistByTunnelIds`**（关键字内存过滤）；灯具走 **`listDeviceLampByTunnelIds`**；洞外雷达/亮度走 **`TunnelDevice` + `tunnel_id IN` + `device_type`**（关键字 `like`）；**内存分页** 后 **`TableDataInfo`**（**`total` + `rows`**）；导出按类型选 **`ExcelUtil`**（**`TunnelDevicelistVo` / `TunnelLampsTerminal` / `TunnelDevice`**）。
- **`DeviceWorkspaceController`**：**`POST /device/workspace/list`**、**`POST /device/workspace/export`**（不修改原 **`/device/list`**）。
- **`TunnelDevicelistServiceImpl`**：补充 **`LinkedHashMap` / `Map`** import（批量列表去重用）。

### 前端（`zttunnel-admin`）

- **`src/api/device.js`**：**`getDeviceWorkspaceList`**、**`exportDeviceWorkspace`**。
- **`src/utils/tunnelTreeLeafIds.js`**：**`leafTunnelIdsFromTreeChecks`**（勾选键 → **`level === 4`** 叶子隧道 id）。
- **`src/views/device/list/DeviceList.vue`**：左 **`el-tree`** 级联勾选；右侧关键字 + **「查询」** 后请求；**`el-tabs`** 分设备类型，**每个 Tab 内「刷新」「导出」**；列表与导出均走新接口；去掉原单隧道级联即时查询逻辑。
- **设备列表 Tab 缓存（2026-03-24 补充）**：点击 **「查询」** 时用 **`Promise.all`** 并行请求 **所有设备类型** 的第 1 页，写入 **`workspaceTabCache`**；**切换 Tab** 只 **`applyTabSnapshotToView`**，**不再发请求**；**翻页 / 改每页条数** 仅 **`fetchCurrentTabPage`** 更新当前类型缓存；**「刷新」** 并行重拉 **各 Tab 各自记住的页码与 pageSize**。

## 2026-03-28 复制隧道接口与错误提示

- **原因**：前端请求 **`POST /tunnel/copy/tunnelGroup`** 时后端无映射，Spring 返回 **`No message available`**。
- **后端**（`zt_project_tunnel`）：新增 **`TunnelCopyController`**（`/tunnel/copy/tunnelGroup`）、**`TunnelCopyService` / `TunnelCopyServiceImpl`**——校验源为 **level=3**、同级 **新名称唯一**，事务内新建 **level=3 + 全部 level=4 子节点**（先复制隧道树结构；设备/占位等逐表复制可后续迭代）。
- **前端**：**`src/utils/request.js`** 在 HTTP 错误时对 **`No message available`** 回退为 **`error` + `path`**，便于区分 404 等。

## 2026-03-28 隧道列表恢复 level-3「复制隧道群」

- **`TunnelList.vue`**：**`level === 3`** 操作列恢复 **「复制」**（权限 **`system:tunnel:update`**）；**`el-dialog`** 填写 **`newTunnelName`**，请求 **`POST /tunnel/copy/tunnelGroup`**（**`sourceTunnelGroupId`、`newTunnelName`**）；成功后刷新树表。

## 2026-03-24 隧道列表 / 公司列表 UI

- **`TunnelList.vue`**：去掉顶部 **隧道名称 / 层级 / 状态** 筛选区；表格直接展示完整树形数据（**`rawData`**），删除前端 **`filterTree`** 与 **`searchForm`**。
- **`TunnelCompanyList.vue`**：表格 **隐藏 ID 列**（**`row-key`** 仍用 **`id`**，不影响树形/选择）。

## 2026-03-24 菜单：移除设备控制、雷达管理

- **`src/router/index.js`**：删除子路由 **`/device/management`（设备控制）**、**`/device/radar`（雷达管理）**；**`/device`** 默认重定向改为 **`/device/list`**。侧栏由路由生成，故不再显示上述两项。（原 **`DeviceManagement.vue`**、**`RadarManagement.vue`** 文件仍保留在仓库中，若需彻底删除可另行清理。）

## 2026-03-28 隧道群复制：沿用已测通接口 + Service 小修正（后端本地）

- **接口不变**：仍为 **`POST /tunnel/copy/tunnelGroup`**，body **`sourceTunnelGroupId`、`newTunnelName`**，与前端、你方已验收的契约一致；无需对接「新接口」逐表对比。
- **`TunnelCopyServiceImpl`（`zt_project_tunnel/scsdky-admin`）**：
  - **`tunnel_edge_computing_terminal`**：复制时**不再**用新隧道群名去覆盖 **`lineName`**（保留原线路展示含义，仅按新 L4 更新 **`id` / `tunnelId` / `tunnelName`**）。
  - **`copyLevel4Detail`**：去掉无用的 **`newGroupName`** 形参，调用处传入原 L4 的 **`tunnelName`** 作为展示名来源。
  - **`tunnel_approach_lamps_terminal`**：**`lastUpdate`** 写入 **`LocalDateTime.now()`**（避免从 **`Date`** 误转 **`LocalDateTime`**）。

## 2026-03-28 闭环：公司新增 → 隧道列表；L2/L3 新增子级；复制后定位

### 公司列表 `TunnelCompanyList.vue`

- 新增成功：`ElMessage.success` 后 **`ElMessageBox.confirm`**，**「去隧道列表」** 跳转 **`/tunnel/list?focusCompanyId={后端返回的新公司 id}`**（与 **`TunnelCompanyController` `AjaxResult<Long>`** 一致）。
- **「稍后」** 仅关闭弹窗，留在公司列表。

### 隧道列表 `TunnelList.vue`

- **L1、L2、L3** 且 **`system:tunnel:update`**：操作列 **「新增子级」**（绿色 plain 小按钮）→ **`panel=edit&parentId=`** 打开内嵌表单；子级层级为 **父 level+1**（管理单位下增加高速公路，高速下增加隧道群，隧道群下增加具体隧道）。
- **`edit` 面板**：`subviewTitle` 在无 **`tunnelForm.id`** 时为 **「新增子级」**；合并原 **`editId`** 与 **`parentId`** 的 **`watch`**，**`editId` 优先**。
- **`focusCompanyId` / `focusTunnelId`**：树数据就绪后 **展开路径、表格行 `scrollIntoView({ block: 'center' })`、约 4.5s 淡色高亮**，然后 **`replace` 去掉 focus 参数**（保留其它 query）。
- **复制隧道群**成功：**长文案 Message**（含「请展开新隧道群并为左右线完善参数或绑定设备」）；刷新列表后对 **`newTunnelGroupId`** 执行同上 **居中 + 高亮**（不依赖路由）。
- **`findNodeById` / `findTunnelNameInTree`**：与占位 id 一致，**`String(id)`** 比较。

## 2026-03-28 隧道群 L3 名称弹窗编辑；公司列表支持改名

- **`TunnelList.vue`**：**level=3（隧道群）** **「编辑」** 与 **「复制」** 同为实心 **`type="primary"`** 蓝色按钮；弹窗可改 **隧道群名称、里程(km)、状态**（与内嵌表单一致：`el-input-number` / **有效·失效**）；**状态变更且存在左右线子节点** 时 **`ElMessageBox` 二次确认**（与 **`handleSubmit`** 文案一致）；提交 **`POST /tunnel/update`**。**level=4「编辑」** 仍进 **`tunnel-detail` 工作台**，不变。
- **`TunnelCompanyList.vue`**：具备 **`system:tunnel:update`** 时表格增加 **操作列「编辑」**；与新增共用弹窗，`id` 有值时标题 **「编辑公司」**，提交 **`updateTunnel`**（**`parentId: 0`、`level: 1`**，保留 **状态/里程**）；新增仍走 **`addTunnelCompany`** 及「去隧道列表」引导。

## 2026-03-28 隧道列表：L1 行「新增子级」

- **`TunnelList.vue`**：在 **管理单位（level=1）** 操作列同样显示 **「新增子级」**（在该公司下新增 **高速公路 L2**）。抽取 **`isAddChildLevel`**（**1/2/3**），**`edit` 面板 `parentId` 校验** 与 **`openAddChild`** 与之对齐。

## 2026-03-28 隧道列表：绑定按钮条件 + 操作列标准按钮

- **`TunnelList.vue`**：**绑定** 仍仅在 **level=4** 且 **`GET /tunnel/bind/placeholder-tunnel-ids`** 返回的隧道 id 命中时显示；占位 id 与树节点 id 统一为 **`String`** 再写入 **`Set`**、再 **`has(String(row.id))`**，避免 JSON 下 **number / string** 不一致导致整列不显示绑定。
- **操作列**：**复制**（level-3）用实心 **`el-button type="primary" size="small"`**；**查看 / 编辑 / 绑定**（level-4）用 **`plain`** + **`el-space`** 间距；列宽 **340**。子视图 **「返回隧道列表」** 改为 **`size="small" plain`** 标准按钮（去掉 **`link`** 纯文字样式）。

## 2026-03-28 隧道复制：对齐 log「复制规则」（§2026-03-24）

- 依据本文件 **第 125 行起**「**复制规则（`TunnelCopyServiceImpl`）**」：
  - **`tunnel_devicelist`**：**`deviceTypeId` 为 1（边缘）或 2（电能）** 时，新主键 **`device_id`** 使用 **`991600000000`～`991699999999`** 内随机占位，**`getById` 去重**（等价于共用 **`nextUnique9916DevicelistId`**）；**其它类型** 仍走 **`yy * 10^9 + [0, 10^9)`** 且库内未占用。
  - **`tunnel_lamps_terminal` / `tunnel_out_of_radar` / `t_tunnel_device`**：复制时 **保留源记录的 `device_id`**（与「冲突时人工处理」一致，由唯一约束失败提示）。

## 2026-03-28 后端：隧道树 /tunnel/add、/tunnel/update；公司重名校验

### `zt_project_tunnel` / `scsdky-admin`

- **`TunnelNameController`**：新增 **`POST /tunnel/add`**、**`POST /tunnel/update`**（**`system:tunnel:update`**），对接管理端 **`TunnelList` 新增子级 / 内嵌编辑**。
- **`TunnelNameResultService` / `Impl`**：
  - **`addTunnelTreeNode`**：校验父级 **L1～L3**、子级 **level=父+1**、**同级名称**不重复；写入 **`t_tunnel_name_result`**，返回新 **id**。
  - **`updateTunnelTreeNode`**：允许改 **名称 / 里程 / 状态**；**状态变更**时 **递归同步子节点状态**（与前端的「子级同步」提示一致）。
  - **`addLevel1Company`**：公司名称重复校验由 **全表 tunnel_name** 改为 **仅 level=1**，避免与下级隧道同名导致误报「已存在」。
- **`tunnel.js`**：注释标明上述路径（请求仍为 **`/api` 前缀 + 代理**）。

## 2026-03-31 用户管理：新增/编辑弹窗对齐 zt_tunnel_web

- **`src/api/system.js`**：**`getUser(userId)`** 在未传 **`userId`** 时请求 **`GET /system/user/`**，用于拉取岗位 **`posts`**、角色 **`roles`**（与同后端 **`SysUserController#getInfo`** 一致）。
- **`src/views/system/user/UserManagement.vue`**：
  - 弹窗字段与 **`zt_tunnel_web`** 的 **`system/user/addUpdateModal.vue`** 一致：**用户账号、用户名称、性别、所属部门、岗位（多选）、角色（多选）、手机、邮箱、新增时密码、状态**；去掉备注；表格列标题改为 **用户账号 / 用户名称**。
  - **新增**：先 **`getUser()`** 再打开弹窗；**编辑**：**`getUser(id)`** 回填 **`data` + `postIds` + `roleIds`**；提交带 **`sex`、`postIds`**，新增带用户输入的 **`password`**，并传 **`tunnelIds: []`** 避免后端绑定逻辑空指针。
  - 性别/状态选项与若依字典 **`sys_user_sex` / `sys_normal_disable`** 一致（前端写死选项，未接字典接口）。

## 2026-03-31 用户管理：字段对齐 zt_tunnel_web「大屏」table2（七字段）

- 参考 **`zt_tunnel_web/src/views/admin/components/table2.vue`**：**用户名称**（`userName`）、**用户密码**（仅新增）、**用户昵称**（`nickName`）、**手机号码**、**所属单位**（`unit`）、**角色**（单选，提交 **`roleIds: [roleId]`**）、**所属隧道**（**`el-cascader` 多选**，**`id` / `tunnelName` / `children`**，扁平为 **`tunnelIds`** 提交）。
- **隧道树**：先 **`getTunnelTree`**，无数据再 **`getAllTunnelTree`**（管理端补全可选隧道）。编辑时按 **`tunnelIds`** 在树上反查路径回填级联。
- **列表列**：**用户名称 / 用户昵称 / 所属单位 / 角色 / 手机号码 / 状态 / 创建时间**；搜索占位与 **用户名称** 文案一致。
- 提交体默认 **`sex: '0'`**、**`postIds: []`**；若有 **`deptId`**（编辑带出）则一并提交。

## 2026-03-31 用户管理弹窗：栅格与必填星标

- **`UserManagement.vue`**：弹窗 **`el-form`** 统一 **`label-width="108px"`**、**`label-position="right"`**；**`el-row`/`el-col`** **`gutter="16"`**，两列用 **`xs24/sm12`** 与 **`sm12`** 保证窄屏叠行、宽屏对齐；**岗位/角色** 整行 **`span=24`**，去掉重复的 **`label-width`**。
- 所有 **`el-input` / `el-select` / `el-tree-select`** 通过 **`.user-dialog-form`** 的 **`:deep`** 规则 **`width: 100%`** + **`flex:1`/`min-width:0`**，避免下拉比输入框短、右缘不齐。
- **新增** 时 **`password`** 使用 **`computed` 规则** 的 **`required: true`**，与 **性别/部门/状态** 一样显示 **\***；多选岗位/角色增加 **`collapse-tags`**。
- 弹窗 **`align-center`、`destroy-on-close`、`class="user-edit-dialog"`**，并微调 **`el-dialog__body` 内边距**。

## 隧道列表：`TunnelList.vue` L1「添加高速」与内嵌表单

- **L1 操作列**：原「新增子级」在 **`scope.row.level === 1`** 时改为 **「添加高速」**（L2/L3 按钮文案见下条 **「添加隧道」** 小节）。
- **`panel=edit` 且新增 level=2**：工具条标题 **「添加高速」**（原统一「新增子级」）；内嵌表单 **名称** 为 **高速公路名称** + 占位 **请输入高速公路名称**；**父级** 文案为 **所属管理单位**（下拉仍为所选 L1 节点）；**层级** 只读为 **高速公路**；**`v-if="tunnelForm.level >= 3"`** 的里程不展示（与在高速节点不填里程一致）。
- **`tunnelRules`** 改为 **`computed`**，校验提示随 **level**（2/3/4）与占位一致。

## 隧道列表：`TunnelList.vue` L2 按钮「添加隧道」

- **L2 操作列**：**`addChildButtonText`**：**level=1** → **添加高速**，**level=2** → **添加隧道**（L3 已取消该按钮，见下条）。
- **内嵌新增 level=3**：工具条标题 **「添加隧道」**（与 L2 按钮一致）。

## 隧道详情：设备 Tab 隐藏「显示列」并默认全列

- **`DeviceTableColumnSplit.vue`**：新增 **`showColumnPanel`**（默认 **`true`**）。为 **`false`** 时不渲染左侧勾选区与拖拽条，并通过 **`emitAllColumnKeys`** 始终勾选 **`options` 中全部列**；**`onGroupChange`** 在纯表格模式下不写入 localStorage。
- **`DeviceBindWorkspace.vue`**：**`deviceTableShowColumnPanel`** = 非「嵌入且隧道查看/编辑」；嵌入绑定（**`edgeDeviceBindMode`**）仍为 **`true`**，设备管理页默认不传亦为 **`true`**。
- 各设备表组件增加 **`showColumnPanel`** 并传入 **`DeviceTableColumnSplit`**：**`EdgeControllerTab`**、**`PowerTerminalTab`**、**`LampsControllerTab`**、**`ApproachLampsTab`**、**`PowerMeterTable`**。

## 隧道列表：`TunnelList.vue` 去掉 L3「新增子级」按钮

- **`isAddChildLevel`**：由 **`[1,2,3]`** 改为 **`[1,2]`**，**L3（隧道群）** 行不再显示绿色新增按钮。
- **`parentId` 嵌入新增**：父节点仅允许 **L1/L2**；若手工带上 **L3** 的 `parentId`，**`router.replace`** 回 **无 query** 的列表，避免空白表单。
- **`addChildButtonText`**：仅区分 L1/L2（L3 不再走该按钮）。

## 2026-03-31 隧道管理：「隧道参数」改为「隧道模板」

- **`router/index.js`**：原 **`TunnelParam`** 路由改为 **`TunnelTemplate`**，组件改为 **`views/tunnel/template/TunnelTemplateList.vue`**；侧栏标题 **「隧道模板」**，图标 **`DocumentCopy`**；路径仍为 **`/tunnel/param`**、权限仍为 **`tunnel:param:view`**（与后端菜单标识兼容）。
- **新增 `TunnelTemplateList.vue`**：展示模板列表表格（当前空数据）、说明 **Alert**、工具栏 **刷新**；**从模板新建**、行内 **套用模板** 暂以 **ElMessage** 提示「功能开发中」。**存为模板 / 真实一键拷贝** 待后续接接口。
- **删除 `views/tunnel/param/TunnelParam.vue`**：原「选隧道 + 只读参数表单」独立菜单入口移除；单隧道参数编辑仍在 **设备绑定 / 隧道详情** 的 **隧道参数** Tab（**`TunnelParamForm`**）中。
