#!/bin/bash

# 修改代码后,需要先关闭容器:
# 查看当前容器状态
# $sudo docker ps -a
# 应该会看到两个或更多的 eclipse che 相关的容器,分别是平台和具体的 workspace,两
# 个容器都需要关闭,根据启动时间先关闭最近启动的容器,再关闭最早启动的容器
# $sudo docker stop {containerID}
# $sudo docker stop 8487c1262362  #eclipse-che/workspace4y0ryq9e60jt6ucc_null_che_dev-machine
sudo docker stop 05f47423dcb1  # eclipse/che-server:6.16.0

# 重新编译该插件
# 返回至 demo git 目录下
mvn clean fmt:format install -DskipTests

# 重新启动 eclipse che 查看修改结果
sudo docker run -ti --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v ~/Check:/data \
  -v $(pwd)/assembly/assembly-main/target/eclipse-che-6.16.0/eclipse-che-6.16.0:/assembly \
  eclipse/che:6.16.0 start -fast
# 浏览器打开{IP 地址}:8080

# 集成到 CPIntergration 后，启动命令是
sudo docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \
  -v ~/Check:/data \
  -v $(pwd)/assembly/assembly-main/target/eclipse-che-6.16.0/eclipse-che-6.16.0:/assembly \
  -v /root/.m2:/home/cbc/.m2 \
  eclipse/che:6.16.0 start