#!/bin/sh

## 数据库连接信息

SORUCE_MYSQL_URL="jdbc:mysql://hadoop:3306/ruozedata_platform?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
SOURCE_MYSQL_USER="root"
SOURCE_MYSQL_PASSWORD="root"
SOURCE_MYSQL_DRIVER="com.mysql.jdbc.Driver"

TARGET_MYSQL_URL="jdbc:mysql://hadoop:3306/ruozedata_recommend?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8"
TARGET_MYSQL_USER="root"
TARGET_MYSQL_PASSWORD="root"
TARGET_MYSQL_DRIVER="com.mysql.jdbc.Driver"