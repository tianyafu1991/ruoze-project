#!/bin/bash

#        sh standard.sh db table where
#---------------------------------------------
#FileName:		standard.sh
#Version:		1.0
#Date:			2020-03-15
#Author:		ruozedata-J
#Description:		example of shell script
#Notes:			project ....
#		2020-07-01 �ű�����
#		2020-07-10 �ű�bug�޸�������bugΪ.....
#---------------------------------------------
set -u

USAGE="Usage : $0 db table where"
[ $# -ne 3 ] && echo "$USAGE"  && exit 1

#�ȼ�:
#if [ $# -ne 3 ];then
#	echo "$USAGE" 
#	exit 1
#fi

#start
source /root/shell/mysqlconn.sh
echo "$URL"
 
echo "www.ruozedata.com"

#end 


exit 0
