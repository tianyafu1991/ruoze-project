[hadoop@hadoop01 ~]$ cd ~/software/
[hadoop@hadoop01 software]$ wget http://download.redis.io/releases/redis-5.0.5.tar.gz
[hadoop@hadoop01 software]$ su - root
密码：
上一次登录：三 9月 30 21:44:16 CST 2020pts/0 上
[root@hadoop01 ~]# yum install -y gcc g++ make gcc-c++ kernel-devel automake autoconf libtool make wget tcl vim  unzip git
[hadoop@hadoop01 software]$ tar -xzf redis-5.0.5.tar.gz
[root@hadoop01 ~]# cd /usr/local/
[root@hadoop01 local]# mkdir redis
[root@hadoop01 local]# cd /home/hadoop/software/redis-5.0.5
[root@hadoop01 redis-5.0.5]# make && make install  PREFIX=/usr/local/redis

[hadoop@hadoop01 redis-5.0.5]$ cp redis.conf redis.conf.bak 
[hadoop@hadoop01 redis-5.0.5]$ vim redis.conf
bind 0.0.0.0
protected-mode no
port 16379
daemonize yes


[hadoop@hadoop01 redis-5.0.5]$ ~/software/redis-5.0.5/src/redis-server ~/software/redis-5.0.5/redis.conf