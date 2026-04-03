-- 设备状态分布诊断 SQL
-- 用于排查 Dashboard 设备状态分布卡片无数据问题

-- 1. 检查 t_tunnel_device 表是否有数据
SELECT COUNT(*) AS total_devices FROM t_tunnel_device;

-- 2. 查看 t_tunnel_device 中的 tunnel_id 及 device_status 分布
SELECT tunnel_id, device_status, COUNT(*) AS cnt
FROM t_tunnel_device
GROUP BY tunnel_id, device_status
ORDER BY tunnel_id, device_status
LIMIT 50;

-- 3. 检查 tunnel_id 与 t_tunnel_name_result 的对应关系（level）
SELECT td.tunnel_id, tnr.tunnel_name, tnr.level, tnr.parent_id
FROM t_tunnel_device td
LEFT JOIN t_tunnel_name_result tnr ON td.tunnel_id = tnr.id
GROUP BY td.tunnel_id, tnr.tunnel_name, tnr.level, tnr.parent_id
LIMIT 30;

-- 4. 用户 test_dev 可见的 level-4 隧道 id（示例，需替换 user_id）
-- SELECT tnr.id FROM t_tunnel_name_result tnr
-- WHERE tnr.level = 4
-- AND EXISTS (SELECT 1 FROM t_user_tunnel u WHERE u.tunnel_name_id = tnr.id OR u.tunnel_name_id = tnr.parent_id OR ...);
