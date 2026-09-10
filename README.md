# 随手记账 — 个人记账与财务管理应用

《移动应用开发》综合项目 · 对应任务书**项目四：个人记账与财务管理应用**

## 一、做什么

一款 Android 端个人财务记账应用，支持记录日常收支、分类管理、月度统计与预算控制，
数据全部保存在本机，无需联网、无需注册账号。

## 二、为什么做

- **场景真实**：记账是高频且需求明确的个人场景，功能边界清晰，不需要虚构业务。
- **技术完整**：一个本地数据库就能同时覆盖课程要求的"数据存储、CRUD、列表渲染、
  输入校验、状态管理、异常处理、数据统计"等全部核心能力。
- **零外部依赖**：不依赖任何第三方 API 或服务器，演示时不会因网络或密钥问题翻车。

## 三、怎么设计

### 3.1 整体架构

采用任务书建议的 **UI — ViewModel — Repository** 三层结构，并额外划分出 domain 层：

```
UI (Compose)  ──观察 StateFlow──▶  ViewModel  ──调用接口──▶  Repository (domain 接口)
                                       │                          │
                                       │                    实现类 ▼
                                       │              RepositoryImpl (data)
                                       │                    │         │
                                       └────────────────────┘         │
                                              Room (持久化)     DataStore (偏好)
```

分层的实际收益：

| 层 | 职责 | 为什么不合并到一处 |
|---|---|---|
| `ui` | 只负责渲染与事件分发 | 页面不含业务逻辑，便于预览与测试 |
| `ui/*/XxxViewModel` | 持有状态、校验输入、组织数据 | 屏幕旋转后状态不丢失 |
| `domain` | 定义模型与仓库接口 | ViewModel 依赖抽象，便于替换实现与写单测 |
| `data` | Room 与 DataStore 的具体实现 | 换存储方案时不影响上层 |

### 3.2 关键设计决策

**金额用 `Long` 存"分"，不使用 `Double`**
浮点数存在二进制精度误差（`0.1 + 0.2 != 0.3`），账目场景不可接受。
存储与运算统一用分为单位，仅在展示时用 `BigDecimal` 转换为元。

**分类图标用字符串名称而非资源 id**
`Category.icon` 存的是 `"Restaurant"` 这样的名称，由 UI 层的映射表解析为 `ImageVector`。
这样 `domain` 与 `data` 层完全不依赖 Compose 与 Android 资源，可在纯 JVM 单测中运行。

**统一左闭右开的时间区间**
所有按月/按天的查询都使用 `[start, end)` 区间（`startOfMonth` ~ `nextMonthStart`），
避免月初或月末记录被重复统计或遗漏。

**统计图表用 Compose Canvas 手绘**
环形图与占比条只用了 `drawArc` 与 `drawRoundRect`。相比引入第三方图表库，
这样做没有额外依赖，且答辩时能清楚讲出"占比如何映射为弧度"。

**依赖注入不引入 Hilt**
项目规模不需要，用 `LedgerApplication` 持有仓库单例 +
`ViewModelFactories` 统一构造 ViewModel，既解耦又不增加编译开销。

**异常一律转成中文提示**
Repository 中所有挂起操作用 `runCatching` 包裹并返回 `Result<T>`，
ViewModel 把异常映射为面向用户的中文文案（如"保存失败：数据库写入异常，请重试"），
杜绝静默失败。

## 四、怎么实现

### 4.1 技术栈

| 类别 | 选型 |
|---|---|
| 语言 / UI | Kotlin 2.0.21 + Jetpack Compose（Material 3） |
| 导航 | Navigation Compose 2.8.4 |
| 状态管理 | ViewModel + StateFlow + `collectAsStateWithLifecycle` |
| 本地数据库 | Room 2.6.1（KSP 注解处理） |
| 偏好存储 | DataStore Preferences 1.1.1 |
| 构建 | AGP 8.11.0 + Gradle 8.13 + JDK 21 |
| 测试 | JUnit 4 + kotlinx-coroutines-test |

### 4.2 页面清单（6 个页面，超出"不少于 3 个"的要求）

| 页面 | 路由 | 功能 |
|---|---|---|
| 首页 | `home` | 本月收支概览、今日支出、最近 5 条记录 |
| 流水列表 | `list` | 全量流水、关键字搜索、结果合计 |
| 记账表单 | `add` | 收支切换、金额输入、分类选择、备注、日期选择 |
| 统计 | `stats` | 月份切换、环形占比图、分类排行 |
| 预算 | `budget` | 月度预算设置、使用进度、超支提醒 |
| 记录详情 | `detail/{id}` | 完整信息展示、删除确认 |

### 4.3 数据库设计

表 `transactions`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | INTEGER PK | 自增主键 |
| `amount_in_cents` | INTEGER | 金额（分），恒正 |
| `type` | TEXT | `EXPENSE` / `INCOME` |
| `category_id` | TEXT | 分类 id |
| `note` | TEXT | 备注 |
| `occurred_at` | INTEGER | 发生时间（毫秒），建索引 |
| `created_at` | INTEGER | 创建时间 |

在 `occurred_at` 与 `category_id` 上建立索引，因为列表与统计的高频操作都是
"按时间范围过滤"和"按分类聚合"。

## 五、遇到的问题与解决

| 问题 | 原因 | 解决 |
|---|---|---|
| AGP 拒绝构建 | 项目路径含中文（`刘浩`） | `gradle.properties` 中设置 `android.overridePathCheck=true` |
| Gradle 测试工作进程崩溃，报 `ClassNotFoundException: GradleWorkerMain` | Gradle 用户目录与系统 TEMP 都位于中文路径下，传给 worker 的 classpath 编码错乱 | 创建 `C:\gradle-home` 目录联接指向 `.gradle`，并在 `build.bat` 中将 `GRADLE_USER_HOME`、`TEMP` 重定向到纯 ASCII 路径 |
| 测试类加载失败 | 项目路径本身含中文 | 创建 `C:\ledger-app` 目录联接，从该路径执行构建 |
| `GradleWrapperMain` 无法加载 | 自行拼装的 wrapper jar 缺少依赖类 | `build.bat` 直接调用本机缓存的 Gradle 发行版 |
| 统计占比全为 0 | 整数除法被截断 | 先转 `Double` 做除法再转 `Float` |
| 编译报"函数为 private" | 拆分为多文件后跨文件调用 | 跨文件复用的组件改为 `internal` |

## 六、最终效果

- `assembleDebug` 构建通过，产物：`app/build/outputs/apk/debug/app-debug.apk`
- `testDebugUnitTest` 全部 26 个单元测试通过
- 功能覆盖任务书 8 项最低标准

## 七、如何运行

```batch
:: 在项目目录双击或执行
build.bat assembleDebug          :: 编译
build.bat installDebug           :: 安装到模拟器/真机
build.bat testDebugUnitTest      :: 运行单元测试
```

`build.bat` 已内置 JDK、SDK 路径与编码修正，无需手工配置环境变量。

## 八、测试说明

| 测试类 | 覆盖内容 | 用例数 |
|---|---|---|
| `AmountValidatorTest` | 金额解析、边界值、格式化、备注校验 | 15 |
| `DateUtilsTest` | 月初月末边界、区间左闭右开、月份偏移 | 5 |
| `TransactionTest` | 收支符号、分类兜底、汇总计算、分类唯一性 | 6 |

测试重点放在**金额与时间**这两处最易出错、且出错后果最严重（账目错误）的逻辑上。
