# csmp-supply

## 模块职责

`csmp-supply` 用于承载供应链管理域，包括：

1. 供应商管理
2. 物理资源管理
3. 云平台管理
4. 快照采集配置与执行日志
5. 实时事件订阅与标准化入库
6. 云租户快照与组织绑定

## 依赖服务

1. `csmp-system`
   - 组织、租户、字典、用户能力
2. `csmp-job`
   - SnailJob 调度执行

## 关键文档

1. `docs/api/supply-management-api.md`
2. `docs/superpowers/specs/2026-04-12-supply-management-design.md`
3. `docs/superpowers/plans/2026-04-12-supply-management.md`

## 路由说明

- 标准对外路径统一为 `/supply/{resource}`
- 兼容保留 `/api/supply/{resource}` 访问别名
- 控制器资源路径统一为 `/{resource}`，例如供应商分页列表标准对外地址为 `/supply/suppliers/list`
