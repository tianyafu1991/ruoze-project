[hadoop@hadoop01 ~]$ cd software/
[hadoop@hadoop01 software]$ tar -xvf kibana-7.9.2-linux-x86_64.tar.gz -C ~/app/
[hadoop@hadoop01 software]$ cd ~/app/
[hadoop@hadoop01 app]$ ln -s kibana-7.9.2-linux-x86_64 kibana
[hadoop@hadoop01 app]$ echo -e '# KIBANA ENV\nexport KIBANA_HOME=/home/hadoop/app/kibana\nexport PATH=$KIBANA_HOME/bin:$PATH' >> ~/.bashrc
[hadoop@hadoop01 app]$ source ~/.bashrc
[hadoop@hadoop01 app]$ cd ~/app/kibana/config/
[hadoop@hadoop01 config]$ vim kibana.yml
server.port: 15601
server.host: "hadoop01"
elasticsearch.hosts: ["http://hadoop01:9200"]
i18n.locale: "zh-CN"

# 启动
[hadoop@hadoop01 config]$ cd $KIBANA_HOME 
[hadoop@hadoop01 kibana]$ nohup ./bin/kibana &