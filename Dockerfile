# Docker 镜像构建
FROM openjdk:8
#作者
MAINTAINER enjoy

# 复制主机jar包至镜像内，复制的目录需放置在 Dockerfile 文件同级目录下
ADD target/friend-backend-master-0.0.1-SNAPSHOT.jar /root/app/app.jar

EXPOSE 8080

#执行jar包
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]

