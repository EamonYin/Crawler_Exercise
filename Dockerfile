# 拉取jdk8作为基础镜像
FROM openjdk:8-jdk
# 输入参数，改参数为打包后的jar包，例如 api_gateway/target/api_gateway-1.0-SNAPSHOT.jar
ARG JAR_FILE=target/*.jar
# 添加jar到镜像并命名为app.jar, copy命令是把 api_gateway-1.0-SNAPSHOT.jar直接添加到镜像里面不解压，ADD是添加入镜像并且解压
COPY ${JAR_FILE} app.jar
# 镜像启动后暴露的端口
EXPOSE 8083
# jar运行命令，参数使用逗号隔开
ENTRYPOINT ["java","-jar","/app.jar"]
