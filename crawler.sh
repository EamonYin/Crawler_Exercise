cd /Users/eamonyin/做过的项目/Crawler_Exercise
docker build -t crawler:v1 .
docker stop crawler
docker rm crawler
docker run -itd -p 0.0.0.0:8085:8083 --name Crawler_Exercise crawler:v1
docker logs -f Crawler_Exercise