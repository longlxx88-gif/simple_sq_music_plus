# SQMUSIC_LITE

面向 N1 / ARM64 的轻量音乐搜索与下载服务。项目基于 simple_sq_music_plus 精简，保留搜索、解析、下载队列、歌词、音频标签和 MCP；关闭阿里云盘、歌单监听、自动签到、定时同步等常驻功能。

## N1 部署

准备 Docker Compose 后执行：

```bash
mkdir -p data music
docker compose -f docker-compose-n1.yml up -d
```

- Web：`http://N1_IP:8096`
- 后端/MCP：`http://N1_IP:8099`
- MCP：`http://N1_IP:8099/mcp`
- MCP 请求头：`sqmusic: <登录后获得的 JWT Token>`

后端只挂载两个目录：`./data` 保存 H2 数据库和配置，`./music` 保存下载文件。无需 MySQL、酷狗 API 或网易云 API 容器；对应插件仍可按设置启用外部接口。

默认账号密码仍为 `admin / admin`，首次登录后请立即修改。MCP 默认开启，后续可直接给 Hermes 使用；如需关闭，设置 `MCP_ENABLED=false`。

## 构建

```bash
./mvnw -DskipTests package
docker build --build-arg JAR_FILE=target/sqmusic_lite.jar -t sqmusic_lite:local .
```

镜像运行时使用系统 FFmpeg，避免把 JavaCV/JAVE 的多平台原生包打进镜像；N1 ARM64 镜像由 GitHub Actions 自动构建。

## QQ 音乐修复

QQ 搜索请求已修复 `num_per_page` 字段前多余空格、错误的 `Content-Type`，并增加搜索词 JSON 转义、缺省分页参数和异常响应提示。
