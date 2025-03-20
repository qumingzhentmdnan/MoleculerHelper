## Moleculer Helper
#### 实现方法跳转
- Mac: ⌘ + Click
- Windows: Ctrl + Click
#### 重新扫描服务
- 鼠标右键，点击"重新扫描 Moleculer 服务"
#### 方法调用查看
- 选中要查看的方法，鼠标右键，点击"查看方法调用"
#### 注意
- 请遵循 Moleculer 的服务命名规范，否则可能会出现无法扫描到服务的情况
- 服务命名规范：`项目目录/services/xxx.service.js`
- 调用服务请使用ctx.call('xxx.method', params)或者this.broker.call('xxx.method', params)，如ctx.call('auth.admin-user-role.getRoles', { filter: { codes: excludeRoleCodes } })或者await this.broker.call('order.base.getOrders', { filter: { classId }, projection: { id: 1 } })
- 如果出现无法扫描到服务的情况，请尝试重新扫描服务
