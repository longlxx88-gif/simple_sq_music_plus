# SQMUSIC_LITE runtime image. The JAR is built by CI and copied in.
FROM amazoncorretto:21-alpine AS extractor
LABEL maintainer="SQ"
WORKDIR /extractor

# 声明构建参数（默认值兼容本地开发：先 mvn package 再 docker build）
ARG JAR_FILE=target/sqmusic_lite.jar
# 复制预构建 JAR（CI 中由 build job 产出并通过 artifact 传入）
COPY ${JAR_FILE} app.jar

RUN java -Djarmode=layertools -jar app.jar extract --destination /extractor/layers && \
    find /extractor/layers -exec touch -t 200001010000 {} +

# 第二阶段：运行环境
FROM amazoncorretto:21-alpine

# FFmpeg is used only when a provider needs audio remuxing/transcoding.
RUN apk add --no-cache ffmpeg

# 设置工作目录
WORKDIR /app

# 按稳定性从高到低复制（最稳定的放最前面，优化 Docker 缓存）
# Maven dependencies layer.
COPY --from=extractor /extractor/layers/dependencies/ ./
# spring-boot-loader 层
COPY --from=extractor /extractor/layers/spring-boot-loader/ ./
# application 层：业务代码（最常变）
COPY --from=extractor /extractor/layers/application/ ./

# 显示架构信息（便于调试）
RUN echo "Running on architecture: $(uname -m)" && \
    echo "Java version:" && java -version

# 设置 JVM 参数优化
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 暴露端口
EXPOSE 8099

# 挂载音乐目录
VOLUME ["/music", "/data"]

# 启动应用
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
