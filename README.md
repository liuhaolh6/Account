# 随手记账 — 个人财务记账 Android 应用

一款基于 **Kotlin + Jetpack Compose** 的本地记账应用，支持收支记录、分类管理、月度统计与预算控制。
数据全部存储在本机，无需联网、无需注册。

> 本文档用途：GitHub 项目 README / 简历附件 / 面试讲解稿

---

## 一、项目概览

| 项目 | 说明 |
|---|---|
| 类型 | 独立开发 |
| 平台 | Android（minSdk 24 / targetSdk 35） |
| 代码量 | 47 个 Kotlin 文件，约 3,700 行 |
| 页面数 | 6 个（首页 / 流水列表 / 记账表单 / 统计 / 预算 / 详情） |
| 单元测试 | 26 个，全部通过 |
| 构建产物 | `app/build/outputs/apk/debug/app-debug.apk` |

---

## 二、技术栈

| 类别 | 选型 | 版本 |
|---|---|---|
| 语言 | Kotlin | 2.0.21 |
| UI | Jetpack Compose（Material 3） | BOM 2024.09.00 |
| 导航 | Navigation Compose | 2.8.4 |
| 状态管理 | ViewModel + StateFlow + `collectAsStateWithLifecycle` | lifecycle 2.8.7 |
| 本地数据库 | Room（KSP 注解处理） | 2.6.1 |
| 偏好存储 | DataStore Preferences | 1.1.1 |
| 异步 | Kotlin Coroutines + Flow | — |
| 构建 | AGP 8.11.0 + Gradle 8.13 + JDK 21 | — |
| 测试 | JUnit 4 + kotlinx-coroutines-test | — |

---

## 三、架构设计

采用 **UI — ViewModel — Domain — Data** 四层结构，核心原则是 **Domain 层不依赖任何 Android / Compose API**，
因此核心业务逻辑可在纯 JVM 环境下单元测试。

```
┌─────────────────────────────────────────────────────────┐
│                        UI 层 (Compose)                   │
│   HomeScreen / TransactionListScreen / AddTransaction   │
│   StatsScreen / BudgetScreen / DetailScreen             │
└────────────────────────┬────────────────────────────────┘
                         │ 观察 StateFlow
                         ▼
┌─────────────────────────────────────────────────────────┐
│                     ViewModel 层                         │
│   HomeViewModel / TransactionListViewModel / ...         │
│   持有状态、校验输入、组织数据（屏幕旋转状态不丢失）      │
└────────────────────────┬────────────────────────────────┘
                         │ 依赖抽象接口
                         ▼
┌─────────────────────────────────────────────────────────┐
│              Domain 层（纯 Kotlin，零 Android 依赖）     │
│   model: Transaction / Category / Summary / TransactionType│
│   repository: LedgerRepository（接口）                   │
└────────────────────────┬────────────────────────────────┘
                         │ 实现
                         ▼
┌─────────────────────────────────────────────────────────┐
│                        Data 层                           │
│   LedgerRepositoryImpl ──┬── Room（流水持久化）           │
│                          └── DataStore（预算偏好）        │
└─────────────────────────────────────────────────────────┘
```

### 分层职责与收益

| 层 | 职责 | 为什么不合并 |
|---|---|---|
| `ui` | 只负责渲染与事件分发 | 页面不含业务逻辑，便于预览与测试 |
| `ui/*/XxxViewModel` | 持有状态、校验输入、组织数据 | 屏幕旋转后状态不丢失 |
| `domain` | 定义模型与仓库接口 | ViewModel 依赖抽象，便于替换实现与写单测 |
| `data` | Room 与 DataStore 具体实现 | 换存储方案时不影响上层 |

---

## 四、关键设计决策

### 1. 金额用 `Long` 存"分"，不使用 `Double`

浮点数存在二进制精度误差（`0.1 + 0.2 != 0.3`），账目场景不可接受。
存储与运算统一用"分"为单位，仅在展示时用 `BigDecimal` 转换为元。

```kotlin
// util/AmountValidator.kt
fun parseToCents(raw: String): Long? {
    val text = raw.trim()
    if (!text.matches(Regex("""^\d{1,9}(\.\d{1,2})?$"""))) return null
    val cents = runCatching {
        BigDecimal(text).multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP).longValueExact()
    }.getOrNull() ?: return null
    return cents.takeIf { it in 1..MAX_AMOUNT_IN_CENTS }
}
```

### 2. 分类图标用字符串名称而非资源 id

`Category.icon` 存 `"Restaurant"` 这类名称，由 UI 层映射表解析为 `ImageVector`。
这样 `domain` 与 `data` 层完全不依赖 Compose 与 Android 资源，**可在纯 JVM 单测中运行**。

### 3. 统一左闭右开的时间区间

所有按月/按天的查询都使用 `[start, end)` 区间（`startOfMonth` ~ `nextMonthStart`），
避免月初或月末记录被重复统计或遗漏。

```sql
SELECT * FROM transactions
WHERE occurred_at >= :startMillis AND occurred_at < :endMillis
```

### 4. 两个"支出合计"有意保持不同口径

预算页的"本月支出"只统计当月；流水页的"支出"是当前结果集（默认全部历史）的合计。
前者用于判断是否超支，后者用于核对筛选结果。界面上分别标注了统计范围。

> 历史问题：早期 `spentCents` 只在 ViewModel 初始化时查询一次，新增流水后不刷新。
> 现已改为订阅 `observeSummarySource` 的 Flow 持续重算。

### 5. 统计图表用 Compose Canvas 手绘

环形图与占比条只用 `drawArc` 与 `drawRoundRect`。相比引入第三方图表库，
这样没有额外依赖，且能清楚讲出"占比如何映射为弧度"。

### 6. 依赖注入不引入 Hilt

项目规模不需要，用 `LedgerApplication` 持有仓库单例 + `ViewModelFactories` 统一构造
ViewModel，既解耦又不增加编译开销。

### 7. 异常一律转成中文提示

Repository 中所有挂起操作用 `runCatching` 包裹并返回 `Result<T>`，
ViewModel 把异常映射为面向用户的中文文案，杜绝静默失败。

```kotlin
override suspend fun save(transaction: Transaction): Result<Long> = runCatching {
    val entity = transaction.toEntity()
    if (entity.id == 0L) dao.insert(entity) else { dao.update(entity); entity.id }
}
```

---

## 五、数据库设计

表 `transactions`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | INTEGER PK | 自增主键 |
| `amount_in_cents` | INTEGER | 金额（分），恒正 |
| `type` | TEXT | `EXPENSE` / `INCOME` |
| `category_id` | TEXT | 分类 id |
| `note` | TEXT | 备注（上限 50 字） |
| `occurred_at` | INTEGER | 发生时间（毫秒），**建索引** |
| `created_at` | INTEGER | 创建时间 |

**索引策略**：在 `occurred_at` 与 `category_id` 上建立索引。
列表与统计的高频操作是"按时间范围过滤"和"按分类聚合"，索引收益最大。

**聚合下推到 SQL 侧**，避免把全量数据加载进内存再计算：

```sql
SELECT category_id AS categoryId, SUM(amount_in_cents) AS amount
FROM transactions
WHERE type = :type AND occurred_at >= :startMillis AND occurred_at < :endMillis
GROUP BY category_id
ORDER BY amount DESC
```

---

## 六、功能页面

| 页面 | 路由 | 功能 |
|---|---|---|
| 首页 | `home` | 本月收支概览、今日支出、最近 5 条记录 |
| 流水列表 | `list` | 全量流水、关键字搜索、结果合计 |
| 记账表单 | `add` | 收支切换、金额输入、分类选择、备注、日期选择 |
| 统计 | `stats` | 月份切换、环形占比图、分类排行 |
| 预算 | `budget` | 月度预算设置、使用进度、超支提醒 |
| 记录详情 | `detail/{id}` | 完整信息展示、删除确认 |

---

## 七、测试

| 测试类 | 覆盖内容 | 用例数 |
|---|---|---|
| `AmountValidatorTest` | 金额解析、边界值、格式化、备注校验 | 15 |
| `DateUtilsTest` | 月初月末边界、区间左闭右开、月份偏移 | 5 |
| `TransactionTest` | 收支符号、分类兜底、汇总计算、分类唯一性 | 6 |

测试重点放在 **金额与时间** 这两处最易出错、且出错后果最严重（账目错误）的逻辑上。

```batch
build.bat testDebugUnitTest
```

---

## 八、构建与运行

```batch
build.bat assembleDebug      :: 编译，产出 app/build/outputs/apk/debug/app-debug.apk
build.bat installDebug       :: 安装到模拟器/真机
build.bat testDebugUnitTest  :: 运行单元测试
```

`build.bat` 已内置 JDK、SDK 路径与编码修正，无需手工配置环境变量。

---

## 九、遇到的问题与解决

| 问题 | 原因 | 解决 |
|---|---|---|
| AGP 拒绝构建 | 项目路径含中文 | `gradle.properties` 设置 `android.overridePathCheck=true` |
| Gradle 测试工作进程崩溃，报 `ClassNotFoundException: GradleWorkerMain` | Gradle 用户目录与系统 TEMP 位于中文路径，传给 worker 的 classpath 编码错乱 | 创建 `C:\gradle-home` 目录联接，并在 `build.bat` 中把 `GRADLE_USER_HOME`、`TEMP` 重定向到纯 ASCII 路径 |
| 测试类加载失败 | 项目路径本身含中文 | 创建 `C:\ledger-app` 目录联接，从该路径执行构建 |
| `GradleWrapperMain` 无法加载 | 自行拼装的 wrapper jar 缺少依赖类 | `build.bat` 直接调用本机缓存的 Gradle 发行版 |
| 统计占比全为 0 | 整数除法被截断 | 先转 `Double` 做除法再转 `Float` |
| 编译报"函数为 private" | 拆分为多文件后跨文件调用 | 跨文件复用的组件改为 `internal` |

> 其中 **Gradle worker 类加载失败** 是最难的一个：我从错误堆栈定位到问题不在代码，
> 而在 Gradle 启动 worker 进程时传递的 classpath 编码，最终通过重定向
> `GRADLE_USER_HOME` 与 `TEMP` 到 ASCII 路径解决。这个过程让我理解了
> Gradle 的进程模型与 Windows 平台的编码问题。

---

## 十、后续规划

- [ ] 接入 Retrofit + OkHttp 实现云同步，Room 作为本地缓存实现离线优先
- [ ] 引入 Hilt 替换手写 `ViewModelFactories`
- [ ] 拆分为多模块（`:core` / `:feature`）
- [ ] 补充 Compose UI 测试
- [ ] 增加 GitHub Actions CI
- [ ] 加入 Baseline Profile 优化冷启动

---

## License

本项目为个人学习项目，仅用于教学与作品展示。

