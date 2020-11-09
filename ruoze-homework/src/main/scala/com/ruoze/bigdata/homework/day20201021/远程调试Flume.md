# 服务端开启远程调试端口
```$xslt
# 备份脚本
[hadoop@hadoop ~]$ cd ~/app/flume/bin/
[hadoop@hadoop bin]$ cp flume-ng flume-ng.bak
# 修改启动脚本，开放调试端口
[hadoop@hadoop bin]$ vim flume-ng
#JAVA_OPTS="-Xmx20m"
JAVA_OPTS="-Xmx20m -Xdebug -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y"

```