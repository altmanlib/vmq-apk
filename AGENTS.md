# cloudpay-apk

自用收款通知监听 Android App（Kotlin、Compose、ViewModel + UseCase + Repository）：用 `NotificationListenerService` 监听微信 / 支付宝收款通知，上报到 CloudPay 服务端。构建、测试命令与代码结构见 @README.md

## 开工流程

- 动手前先在仓根检查远程有没有新提交（`git fetch` 后 `git status -sb` 看是否 `behind`），有则先 `git pull --ff-only`；无法快进时停下来告知用户，不擅自 merge / rebase。同一会话内已拉取过的，不必重复拉取
- 先读 @ROADMAP.md；有「待决策」且阻塞当前任务时先向用户确认
- 协议以 CloudPay 仓库的监控 API 与 HMAC 契约为准（`../cloudpay/docs/reference/monitor-api.md`、`../cloudpay/docs/reference/hmac.md`）；重试见 @docs/design/payment-retry-mechanism.md；改对外契约时两仓同步
- 能直接解决的小问题顺手处理，不登记成任务

## 项目目标

- 交付通知到回调的闭环：监听权限、通知分类与金额解析、心跳、到账上报、失败重试
- 方案基于通知文案解析，受限于文案变化、厂商后台限制与系统版本；边界以 `ROADMAP.md`「暂缓」为准

## 开发约定

- 长期主义优先，不留技术债；债务当场还或登记进 `ROADMAP.md` 并写触发条件
- 提交前依次通过 `./gradlew testDebugUnitTest`、`./gradlew assembleDebugAndroidTest`、`./gradlew assembleDebug`；仅当逻辑依赖真机能力时才新增 `androidTest`，并用 `connectedDebugAndroidTest` 执行
- 新功能随附测试；Bug 修复先补能复现的测试，再改代码；重构不降低覆盖
- 测试放 `app/src/test/java`，文件名 `<被测类名>Test.kt`，方法名用反引号直述「前置条件 + 行为 + 预期」；结构 Arrange / Act / Assert，三段空行分隔
- 测试替身用 `*Fake.kt` / `*Stub.kt`，只实现测试需要的最小行为，不引入 mocking 框架
- 协程测试用 `runTest` 与 `advanceUntilIdle()`；涉及 `Dispatchers.Main` 复用 `MainDispatcherRule`；时间用虚拟时间，不用 `Thread.sleep`
- ViewModel 测试覆盖 `uiState`、`effects`、Repository 交互与缺失配置 / 非法输入 / 请求失败路径
- 纯解析、映射、拼装逻辑写成可单测的纯函数；难测的逻辑重构为可注入依赖
- 测试代码注释只用英文

## 通知误报排查

支付宝或微信收款通知误报时，在用户已连接并授权调试设备的前提下，先用 ADB 取真实通知文本，再调整解析规则：

```bash
adb devices -l
adb shell dumpsys notification --noredact | rg -n -i -C 6 'com\.eg\.android\.AlipayGphone|com\.tencent\.mm|支付宝|微信'
adb logcat -d -v brief -s NeNotificationService:D '*:S'
```

1. 从 dumpsys 记录包名、`android.title`、`android.text`；已清除的通知不保证可取
2. 对照 `NeNotificationService` 日志，确认监听服务是否收到通知、实际解析与回调的金额
3. 把脱敏后的真实标题与正文作为单元测试用例，先复现误报，再修复
4. 金额由收款语义锚定提取，不从拼接全文取最后一个数字；营销额度、保障金额、积分不得影响收款金额
5. 通知含隐私，仅用于当前排查，日志、提交信息与回复里不带无关内容

## Git 工作流

- 开发分支：`dev`；agent 改动只在该分支
- 在 `dev` 上完成功能后可自行提交；`type: summary`（`feat` / `fix` / `docs` / `chore` / `style` / `refactor` / `test`），对应 roadmap 时末尾附编号
- 在 `dev` 上可自行 `git push`；合并到其他分支须用户明确要求
- 合并 `main`：须用户明确要求；推送前上述三条 Gradle 命令通过
- 发版：须用户明确要求，只从 `main` 执行 `bash scripts/release.sh [x.y.z]`（提交版本号、打 tag、推送并触发 Release workflow）；先用 `--dry-run` 预览，缺省版本号自动递增 patch
