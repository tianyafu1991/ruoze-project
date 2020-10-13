# 创建2个topic
kafka-topics.sh --create --bootstrap-server hadoop:9092 --replication-factor 1 --partitions 3 --topic streaming1
kafka-topics.sh --create --bootstrap-server hadoop:9092 --replication-factor 1 --partitions 3 --topic streaming2

# 查看topic
kafka-topics.sh --bootstrap-server hadoop:9092 --list

