# SQMUSIC_LITE

面向 Amlogic S905D N1 / ARM64 的轻量音乐搜索与下载服务。项目基于 simple_sq_music_plus 精简，前端只有搜索、下载、解析歌单三个主入口和必要设置；保留歌词、音频标签和 MCP，关闭阿里云盘、歌单监听、自动签到、定时同步等常驻功能。

发布镜像仅包含 `linux/arm64`。原先携带 x86-64 原生库的 WebP ImageIO 依赖已移除，封面兼容转换由 ARM64 Alpine FFmpeg 完成。

原版五个搜索入口为：某我、猪厂、鹅厂 VIP下载、移动、Tidal。入口显示与接口可用性分别处理：网易云需要可用的外部 API，QQ 下载可能需要登录和相应账号权限。显示 FLAC 按钮不等于文件已下载成功。Compose 中应用代理指向本机 Mihomo 7890；Docker 拉取镜像使用宿主机 Docker 的代理设置，需要单独配置。

## N1 部署

准备 Docker Compose 后执行：

```bash
mkdir -p data music
docker compose -f docker-compose-n1.yml up -d
```

- Web：`http://N1_IP:8196`
- 后端/MCP：`http://N1_IP:8199`
- MCP：`http://N1_IP:8199/mcp`
- MCP 请求头：`sqmusic: <登录后获得的 JWT Token>`

后端只挂载两个目录：`./data` 保存 H2 数据库和配置，`./music` 保存下载内容。无需 MySQL；网易云、酷狗可按原版设置连接外部 API。

N1 默认 JVM 参数为 `-Xms64m -Xmx320m -Xss512k -XX:+UseSerialGC`，H2 连接池和调度线程池均按 2GB 设备收缩。歌曲下载并发仍保持原版默认值 **8**：超出的任务写入 H2 等待队列，调度器每 10 秒检查空位并继续执行。

默认账号密码仍为 `admin / admin`，首次登录后请立即修改。MCP 默认开启，后续可直接给 Hermes 使用；如需关闭，设置 `MCP_ENABLED=false`。

## 构建

```bash
./mvnw -DskipTests package
docker build --platform linux/arm64 --build-arg JAR_FILE=target/sqmusic_lite.jar -t sqmusic_lite:local .
```

镜像运行时使用系统 FFmpeg，避免把 JavaCV/JAVE 及 x86-64 原生包打进镜像；N1 ARM64 镜像由 GitHub Actions 自动构建。

## QQ 音乐修复

QQ 搜索使用已保存的 QQ 音乐登录信息，校验外层及内层错误码，并限制单次请求为 15 秒。上游 `req.code=2001` 不再被当作零条搜索结果；请在设置中登录或重新登录 QQ 音乐后测试。缺少有效账号时，匿名搜索可能被上游拒绝，不能保证可用。搜索词转义和分页参数修正仍保留。

前端源码位于 `web/`，独立发布为 `sqmusic_lite_web` 镜像，不能继续使用原版前端镜像来验证精简界面。构建：`cd web && npm ci && npm run build`。
