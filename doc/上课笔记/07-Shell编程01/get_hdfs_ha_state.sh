#!/bin/bash

NN1_HOSTNAME=""
NN2_HOSTNAME=""
NN1_SERVICEID=""
NN2_SERVICEID=""
NN1_SERVICESTATE=""
NN2_SERVICESTATE=""

#EMAIL=2643854124@qq.com

CDH_BIN_HOME=/opt/cloudera/parcels/CDH/bin
#CDH_BIN_HOME=/home/hadoop/app/hadoop/bin


ha_name=$(${CDH_BIN_HOME}/hdfs getconf -confKey dfs.nameservices)
namenode_serviceids=$(${CDH_BIN_HOME}/hdfs getconf -confKey dfs.ha.namenodes.${ha_name})

for node in $(echo ${namenode_serviceids//,/ }); do
	state=$(${CDH_BIN_HOME}/hdfs haadmin -getServiceState $node)

	if [ "$state" == "active" ]; then
		NN1_SERVICEID="${node}"  
		NN1_SERVICESTATE="${state}" 
		NN1_HOSTNAME=`echo $(${CDH_BIN_HOME}/hdfs getconf -confKey dfs.namenode.rpc-address.${ha_name}.${node}) | awk -F ':' '{print $1}'`
		#echo "${NN1_HOSTNAME} : ${NN1_SERVICEID} : ${NN1_SERVICESTATE}"

	elif [ "$state" == "standby" ]; then
		NN2_SERVICEID="${node}"
		NN2_SERVICESTATE="${state}"
		NN2_HOSTNAME=`echo $(${CDH_BIN_HOME}/hdfs getconf -confKey dfs.namenode.rpc-address.${ha_name}.${node}) | awk -F ':' '{print $1}'`
		#echo "${NN2_HOSTNAME} : ${NN2_SERVICEID} : ${NN2_SERVICESTATE}"
	else
		echo "hdfs haadmin -getServiceState $node: unkown"
	fi

done

echo "                                                                "
echo "Hostname		Namenode_Serviceid		Namenode_State"
echo "${NN1_HOSTNAME}		${NN1_SERVICEID}		${NN1_SERVICESTATE}"
echo "${NN2_HOSTNAME}		${NN2_SERVICEID}		${NN2_SERVICESTATE}"


#exit
#save current NN1/2_HOSTNAME state
echo "${NN1_HOSTNAME}           ${NN1_SERVICEID}	${NN1_SERVICESTATE}" > HDFS_HA.log
echo "${NN2_HOSTNAME}           ${NN2_SERVICEID}        ${NN2_SERVICESTATE}" >> HDFS_HA.log


if [ -f HDFS_HA_LAST.log ];then
        	HISTORYHOSTNAME=`cat HDFS_HA_LAST.log| awk '{print $1}' | head -n 1`

        	if [ "$HISTORYHOSTNAME" != "${NN1_HOSTNAME}"  ];then

		 echo "send a mail"
                #echo -e "`date "+%Y-%m-%d %H:%M:%S"` : Please to check namenode log." | mail \
                #-r "From: alertAdmin <ruozedata@163.com>" \
                #-s "Warn: CDH HDFS HA Failover!." ${EMAIL}
		
		fi

fi

cat HDFS_HA.log > HDFS_HA_LAST.log




