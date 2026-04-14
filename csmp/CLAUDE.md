# CLAUDE.md

本文档是 CSMP (启智宸安云安全管理平台) 的工程圣经，面向 Claude Code AI 助手及所有开发人员。
**规则**: 本文档优先级高于一般实践，冲突时以本文档为准。

---

## 1. 项目元数据

| 属性 | 值 |
|------|-----|
| **项目名称** | CSMP - 启智宸安云安全管理平台 |
| **版本** | 2.6.0 |
| **Java 版本** | 17 (LTS) |
| **Spring Boot** | 3.5.12 |
| **Spring Cloud** | 2025.0.1 |
| **构建工具** | Maven 3.9+ |
| **编码** | UTF-8 |

---

## 2. 技术架构（不可变更）

### 2.1 核心技术栈
- **微服务框架**: Spring Cloud + Spring Cloud Alibaba + Dubbo 3.x
- **注册配置**: Nacos 2.x (必须开启认证)
- **认证授权**: Sa-Token (禁止直接使用 Spring Security)
- **ORM**: MyBatis Plus 3.5.x (禁止原生 JDBC/MyBatis)
- **缓存**: Redisson (禁止直接使用 Jedis/Lettuce)
- **分布式事务**: Seata AT 模式
- **消息队列**: RocketMQ 5.x
- **对象存储**: MinIO (S3 API)
- **工作流**: Warm-Flow (禁止Activiti/Flowable混用)
- **搜索引擎**: Elasticsearch 8.x
- **链路追踪**: SkyWalking (强制接入)

### 2.2 模块架构（目录即规范）
```
csmp/
├── csmp-auth/              # 认证授权中心 [端口: 9000]
├── csmp-gateway/           # WebFlux 网关 [端口: 8080]
├── csmp-gateway-mvc/       # Servlet MVC 网关（JDK21+虚拟线程）[端口: 8080]
│   ⚠️ 注意: gateway 和 gateway-mvc 二选一，不能同时部署
├── csmp-modules/           # 业务模块 [端口段: 9001-9099]
│   ├── csmp-system/        # 系统管理 [端口: 9001]
│   ├── csmp-gen/           # 代码生成器 [端口: 9002]
│   ├── csmp-job/           # 定时任务 (SnailJob) [端口: 9003]
│   ├── csmp-resource/      # 资源服务 (OSS/SMS) [端口: 9004]
│   └── csmp-workflow/      # 工作流服务 [端口: 9005]
├── csmp-api/               # Dubbo API 定义（纯接口+DTO）
│   ├── csmp-api-system/
│   ├── csmp-api-resource/
│   └── csmp-api-workflow/
├── csmp-common/            # 公共模块（禁止循环依赖）
│   ├── csmp-common-core/   # 基础工具、常量、异常
│   ├── csmp-common-web/    # Web 封装、统一响应、全局异常
│   ├── csmp-common-mybatis/# 数据访问、分页、多租户
│   ├── csmp-common-redis/  # 缓存封装
│   ├── csmp-common-satoken/# 认证封装
│   └── csmp-common-bom/    # 依赖管理
├── csmp-visual/            # 监控告警 [端口: 9100]
└── script/
    ├── docker/             # Docker Compose 基础设施
    ├── sql/                # 初始化 SQL 和 Flyway 基准脚本
    └── k8s/                # Kubernetes 部署模板
```

---

## 3. 开发规范（强制性）

### 3.1 模块开发规范

#### 3.1.1 新模块创建检查清单
创建新模块前，必须确认以下事项：

- [ ] 模块命名: `csmp-{business-name}`，使用 kebab-case
- [ ] 包结构: `com.csmp.{business}`，全小写，禁止使用下划线
- [ ] 端口申请: 在本文档 2.2 节登记，范围 9001-9099，避免冲突
- [ ] 数据库: 独立 Schema，命名 `{business}`（如 `csmp_system`）
- [ ] 加入聚合: 在根 `pom.xml` 的 `<modules>` 中注册

#### 3.1.2 目录结构标准（强制执行）
```
csmp-{module}/
├── src/
│   ├── main/
│   │   ├── java/com/csmp/{module}/
│   │   │   ├── Csmp{Module}Application.java  # 启动类
│   │   │   ├── controller/                  # HTTP 接口层（仅此处允许 @RestController）
│   │   │   │   └── {Domain}Controller.java  # 命名: XxxController
│   │   │   ├── service/
│   │   │   │   ├── {Domain}Service.java     # 接口
│   │   │   │   └── impl/
│   │   │   │       └── {Domain}ServiceImpl.java  # 实现（必须 @Service）
│   │   │   ├── domain/                      # 领域层（可选，复杂业务启用 DDD）
│   │   │   │   ├── entity/                  # 数据库实体（MyBatis Plus Entity）
│   │   │   │   ├── vo/                      # 视图对象（返回给前端）
│   │   │   │   ├── dto/                     # 数据传输对象（内部/外部）
│   │   │   │   └── enums/                   # 枚举（必须实现 IEnum<?>）
│   │   │   ├── mapper/                      # 数据访问层（继承 BaseMapper）
│   │   │   ├── client/                      # Dubbo 消费者（调用外部服务）
│   │   │   └── config/                      # 配置类（必须 @Configuration）
│   │   └── resources/
│   │       ├── mapper/                      # XML 映射文件（与 Mapper 接口同包）
│   │       ├── db/
│   │       │   └── migration/               # Flyway 脚本（V{timestamp}__{desc}.sql）
│   │       └── application-{module}.yml     # 服务配置
│   └── test/                                # 测试代码（镜像 main 结构）
│       └── java/com/csmp/{module}/
├── Dockerfile                               # 必须存在（使用 eclipse-temurin:17-jre）
├── pom.xml
└── README.md                                # 模块说明（接口清单、依赖服务）
```

#### 3.1.3 依赖管理铁律
- **强制父 POM**: 必须继承 `csmp-common-bom`
- **禁止依赖**: 不允许直接引入 `spring-boot-starter-web`（已封装在 `csmp-common-web`）
- **数据库**: 必须引入 `csmp-common-mybatis`
- **缓存**: 必须引入 `csmp-common-redis`（禁止直接使用 `spring-boot-starter-data-redis`）
- **API 暴露**: 如需暴露 Dubbo 接口，必须创建 `csmp-api-{module}` 模块

### 3.2 API 设计规范

#### 3.2.1 REST 接口标准
- **基础路径**: `/{module}/{resource}`
  - 示例: `/system/users`, `/resource/oss/buckets`
- **HTTP 方法**:
  - `GET`: 查询（幂等，禁止修改数据）
  - `POST`: 创建（非幂等）
  - `PUT`: 全量更新（幂等）
  - `PATCH`: 部分更新
  - `DELETE`: 删除（物理删除需二次确认）
- **路径命名**: 小写，名词复数，`-` 连接多单词
  - 正确: `/system/user-groups`
  - 错误: `/system/userGroups`, `/system/getUsers`

#### 3.2.2 统一响应结构（强制）
```java
@Data
@Schema(description = "统一响应")
public class R<T> implements Serializable {
    @Schema(description = "状态码：0成功，非0失败")
    private Integer code;

    @Schema(description = "提示消息")
    private String message;

    @Schema(description = "数据")
    private T data;

    @Schema(description = "链路追踪ID")
    private String traceId;

    @Schema(description = "时间戳（毫秒）")
    private Long timestamp;
}
```

#### 3.2.3 错误码规范（全局唯一）
| 区间 | 归属 | 说明 |
|------|------|------|
| 0 | 全局 | 成功 |
| 1-99 | 系统 | 通用错误（参数错误、系统繁忙等） |
| 100-199 | 网关 | 路由、限流、鉴权错误 |
| 1000-1999 | csmp-auth | 认证授权错误 |
| 2000-2999 | csmp-system | 系统管理错误 |
| 3000-3999 | csmp-resource | 资源服务错误 |
| 4000-4999 | csmp-workflow | 工作流错误 |
| 5000-5999 | csmp-job | 定时任务错误 |
| 6000-6999 | csmp-gen | 代码生成错误 |
| {module}000-{module}999 | 自定义 | 各模块独立分配，需在模块 README 登记 |

**错误码定义示例**:
```java
public enum SystemErrorCode implements IErrorCode {
    USER_NOT_FOUND(2001, "用户不存在"),
    ROLE_BIND_ERROR(2002, "角色已绑定用户，无法删除");

    private final int code;
    private final String message;
}
```

### 3.3 数据库规范（Flyway 管理）

**声明**: 生成 SQL 时必须遵循：
- 适配postgresql的sql语句
- 使用 `BIGINT` 主键（非自增），应用层雪花算法生成
- 必须包含 `tenant_id` 字段，BIGINT DEFAULT 000000
- 逻辑删除使用 `deleted BOOLEAN DEFAULT FALSE`
- 时间戳使用 `TIMESTAMPTZ` 类型
- 显式指定 `public` schema 或业务 schema
- 索引命名：`idx_{table}_{field}` / `uk_{table}_{field}`
- 为 `update_time` 自动更新创建触发器
- 大字段（TEXT/JSONB）考虑拆分独立表
- Flyway 脚本命名：`V{timestamp}__{jira-id}_{desc}.sql`
- 包含 `-- ROLLBACK:` 注释说明回滚逻辑
- **审计字段**（必须）:
  - `create_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
  - `update_time` DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)
  - `create_by` VARCHAR(64) DEFAULT ''
  - `update_by` VARCHAR(64) DEFAULT ''

### 3.4 代码质量规范

#### 3.4.1 日志规范（SLF4J + Logback）
- **声明**: 必须使用 `@Slf4j` Lombok 注解，禁止手动 `LoggerFactory.getLogger`
- **级别**:
  - `DEBUG`: 详细调试（开发环境，生产关闭）
  - `INFO`: 业务关键节点（服务启动、配置加载、重要状态变更）
  - `WARN`: 可恢复异常（参数校验失败、业务限制触发）
  - `ERROR`: 系统错误（数据库连接失败、未知异常），必须包含 `traceId`
- **格式**: 使用占位符 `{}`，禁止字符串拼接
  - 正确: `log.info("用户登录成功，userId={}", userId);`
  - 错误: `log.info("用户登录成功，userId=" + userId);`
- **敏感数据**: 禁止记录密码、Token、手机号、身份证号（脱敏后也不行）

#### 3.4.2 异常处理规范
- **业务异常**: 使用 `BusinessException extends RuntimeException`，必须携带错误码
- **系统异常**: 统一转换为 `R.fail(500, "系统繁忙")`，禁止直接暴露堆栈给前端
- **Controller 层**: 禁止 try-catch，统一由 `@RestControllerAdvice` 处理
- **Service 层**: 
  - 可预见的业务异常抛 `BusinessException`
  - 数据库操作抛 `DataAccessException` 子类
  - 事务方法必须指定 `rollbackFor = Exception.class`

#### 3.4.3 测试规范（质量门禁）
- **单元测试**（强制）:
  - 路径: `src/test/java` 镜像主代码包结构
  - 命名: `{ClassName}Test.java`
  - 框架: JUnit 5 + Mockito + AssertJ
  - 覆盖率: 行覆盖 ≥ 70%，分支覆盖 ≥ 60%（CI 阻断）
  - 核心逻辑必须测试: Service 方法、工具类、Domain 实体行为

- **集成测试**（关键路径）:
  - 命名: `{ClassName}IT.java` (IT = Integration Test)
  - 使用 `@SpringBootTest` + Testcontainers（MySQL/Redis）
  - 必须覆盖: MyBatis Mapper、Redis 序列化、配置加载

- **契约测试**（Dubbo 提供者）:
  - API 提供者必须编写 `{ServiceName}ContractTest`
  - 验证序列化兼容性（Hessian2）

---

## 4. Claude Code 编码指令（AI 专用）

### 4.1 代码生成原则
当用户要求生成代码或新功能时，Claude Code 必须遵循：

1. **先查后写**: 生成代码前，先查看同模块现有代码风格（命名习惯、异常处理方式、日志格式）
2. **结构优先**: 必须按 3.1.2 节的目录结构放置文件，禁止随意创建包
3. **依赖检查**: 新增依赖必须检查是否已在 `csmp-common-bom` 中管理
4. **契约优先**: 生成 Controller 前，必须先定义好 Request/Response DTO 和错误码
5. **测试伴随**: 生成业务代码必须同时生成单元测试（使用 given-when-then 注释结构）

### 4.2 安全红线（不可违反）
- **输入校验**: 所有用户输入必须使用 Spring Validation（`@NotNull`, `@Size`, `@Pattern` 等）
- **SQL 注入**: 必须使用 `#{}` 占位符，绝对禁止 `${}` 字符串拼接（即使是动态表名也不行）
- **XSS 防护**: 返回给前端的字符串必须经过转义（使用 `HtmlUtils.htmlEscape`）
- **文件上传**:
  - 必须校验 MIME 类型（白名单）
  - 必须校验文件头魔数（防止伪造扩展名）
  - 存储到 MinIO 独立 Bucket，禁止存储到本地磁盘
- **敏感操作**: 修改密码、删除数据等操作必须二次校验身份（当前密码/短信验证码）

### 4.3 性能约束
- **数据库**:
  - 查询必须分页（`Page<T>`），禁止无分页的大查询
  - 循环中禁止查询数据库（必须使用批量查询或 Join）
  - 必须使用连接池（HikariCP，已集成）
- **缓存**:
  - 热点数据必须加 Redis 缓存（`@Cacheable` 或手动 Redisson）
  - 缓存必须设置过期时间（默认 30 分钟，可配置）
  - 缓存更新必须保证最终一致性（Cache Aside 模式）
- **异步**:
  - 非关键路径使用 `@Async`（如发送通知、记录日志）
  - 批量操作使用线程池（配置在 `csmp-common-core` 的 `ThreadPoolConfig`）

### 4.4 代码风格（Spotless 自动格式化）
- **缩进**: 4 个空格（禁止使用 Tab）
- **换行**: Unix 风格（LF），UTF-8 编码
- **imports**: 禁止 `import *`，必须显式导入
- **Javadoc**: 所有 public 类和方法必须添加 JavaDoc（中文或英文均可）
- **命名**:
  - 类名: UpperCamelCase
  - 方法/变量: lowerCamelCase
  - 常量: UPPER_SNAKE_CASE
  - 包名: 全小写，点分隔

### 4.5 Git 操作规范
- **提交信息**: 遵循 Conventional Commits
  ```
  <type>(<scope>): <subject>

  type:
  - feat: 新功能
  - fix: 修复
  - docs: 文档
  - refactor: 重构
  - test: 测试
  - chore: 构建/工具

  scope: auth, gateway, system, resource, workflow, common, gen, job

  示例:
  feat(system): 增加用户批量导入功能
  fix(resource): 修复 OSS 上传大文件内存溢出
  ```
- **分支**: 功能分支命名 `feature/CSMP-{id}-{description}`，寿期不超过 3 天
- **禁止**: 禁止提交包含 `TODO`, `FIXME` 无归属标记的代码到 main 分支

---

**文档维护**: 本文档由架构组维护，修改需提交 PR 并通过技术评审。
**生效日期**: 2026-04-07
**版本**: 2.6.0-spec
