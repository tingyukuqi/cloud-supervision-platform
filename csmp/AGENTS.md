# Repository Guidelines

## Project Structure & Module Organization

本仓库是 CSMP 多模块微服务后端。根 `pom.xml` 聚合 `csmp-auth`、`csmp-gateway`、`csmp-visual`、`csmp-modules`、`csmp-api`、`csmp-common`；`csmp-gateway-mvc` 与 `csmp-example` 默认未加入根构建。业务代码放在 `csmp-modules/*`，Dubbo 接口与 DTO 放在 `csmp-api/*`，公共能力放在 `csmp-common/*`，脚本与初始化资源放在 `script/`，补充文档放在 `docs/`。

模块目录按 `CLAUDE.md` 执行：`controller`、`service`、`service/impl`、`domain/{entity,vo,dto,enums}`、`mapper`、`client`、`config`、`src/main/resources/mapper`、`db/migration`。仅 `controller` 允许声明 `@RestController`。

## Architecture Guardrails

技术栈不可随意替换：认证必须用 Sa-Token，ORM 必须用 MyBatis-Plus，缓存必须走 Redisson，工作流使用 Warm-Flow。禁止直接引入 Spring Security、原生 JDBC/MyBatis、Jedis/Lettuce。`csmp-common` 禁止循环依赖，新增 Dubbo 服务时先补 `csmp-api-{module}`。

## Build, Test, and Development Commands

- `mvn clean install -DskipTests`：全仓构建。
- `mvn clean package -Pdev`：按 `dev` profile 打包。
- `mvn -pl csmp-modules/csmp-system -am test -DskipTests=false`：只验证 `system` 模块。
- `mvn -pl csmp-auth -am spring-boot:run -DskipTests`：本地启动单服务。

优先做最小范围验证，不要无差别全仓联跑。

## Coding Style & Naming Conventions

遵循 `.editorconfig`，Java 4 空格，YAML/JSON 2 空格，UTF-8，LF。包名使用 `com.csmp.{module}`，全小写；类名 PascalCase；方法与字段 camelCase；常量 UPPER_SNAKE_CASE。禁止 `import *`。public 类和方法补 JavaDoc。日志统一 `@Slf4j` + `{}` 占位符，禁止记录密码、Token、手机号等敏感信息。

## Testing Guidelines

测试目录使用 `src/test/java` 镜像主包结构。单元测试命名 `*Test.java`，集成测试命名 `*IT.java`，Dubbo 契约测试命名 `*ContractTest`。当前重点测试位于 `csmp-modules/csmp-system/src/test/java`。默认使用 JUnit 5、Mockito、AssertJ；核心 Service、Mapper、配置加载和序列化兼容性要覆盖。按 `CLAUDE.md` 要求，目标覆盖率为行 70% 以上、分支 60% 以上。

## Commit & Pull Request Guidelines

提交信息遵循 Conventional Commits，例如 `feat(system): 增加组织机构层级校验`、`fix(resource): 修复 OSS 上传失败`。分支命名遵循 `feature/CSMP-{id}-{description}`。PR 必须写清影响模块、配置或 SQL 变更、验证命令；接口或权限行为变更附示例请求、截图或回归说明。禁止把无归属 `TODO`、`FIXME` 直接合入主分支。

## Security & Configuration Tips

所有输入必须做 Spring Validation。SQL 只能使用 `#{}` 占位符，禁止 `${}`。上传文件必须校验 MIME 与文件头，存储到 MinIO，不落本地磁盘。配置文件会被根 POM 按 `application*`、`bootstrap*`、`logback*` 过滤，提交前确认没有写死本地地址、账号、密钥或调试日志。
