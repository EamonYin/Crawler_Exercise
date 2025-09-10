# 第一步：清理target目录
mvn clean
# 第二步：打包并指定JAR名称
mvn package
# 第三步：Docker构建
docker build -t crawler:v3 .
# 第四步：停止旧容器
docker stop Crawler_Exercise
# 第五步：删除旧容器
docker rm Crawler_Exercise
# 第六步：删除旧镜像
docker image rm crawler:v2
# 第七步：运行新容器
docker run -itd -p 8085:8083 --name Crawler_Exercise crawler:v3
# 第八步：打印docker日志
docker logs -f Crawler_Exercise