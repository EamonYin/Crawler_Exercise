# 第一步：清理target目录
mvn clean
# 第二步：打包并指定JAR名称
mvn package
# 第二步：Docker构建
docker build -t crawler:v5 .
docker stop Crawler_Exercise
docker image rm crawler:v4
docker run -itd -p 0.0.0.0:8085:8083 --name Crawler_Exercise crawler:v5
docker logs -f Crawler_Exercise