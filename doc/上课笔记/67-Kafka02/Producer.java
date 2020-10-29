package com.ruozedata.kafka;

/**
 * Created by jepson ON 2019/10/18 1:21 PM.
 *
 * 生产者 压测
 */

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class Producer extends Thread {
    private final KafkaProducer<Integer, String> producer;
    private final String topic;
    private final Boolean isAsync;
    private final int sendCount;

    public Producer(String topic, Boolean isAsync,int sendCount) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  "ruozedata001:9092" );
        //props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,  "ruozedata001:9092,ruozedata002:9092,ruozedata003:9092" );
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "ruozedataProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG,"all");
        props.put(ProducerConfig.RETRIES_CONFIG,100);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG,1000);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG,32*1024*1024L);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG,600000);
        producer = new KafkaProducer<>(props);
        this.topic = topic;
        this.isAsync = isAsync;
        this.sendCount = sendCount;
    }

    @Override
    public void run() {
        int messageNo = 1;
        String messageStr = "";
        long startTime =0L;

        while (true) {
            messageStr =  "www.ruozedata.com"+String.valueOf(messageNo);
            startTime = System.currentTimeMillis();

            if (isAsync) { // Send asynchronously
                producer.send(new ProducerRecord<>(topic,
                        messageNo,
                        messageStr), new CallBack(startTime, messageNo, messageStr));

            } else { // Send synchronously
                try {
                    producer.send(new ProducerRecord<>(topic,
                            messageNo,
                            messageStr)).get();
                    System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if(messageNo-sendCount==0) {
                System.out.println("Send message count:"+sendCount);
                break;
            }
            ++messageNo;

            }
    }

    public static void main(String[] args) {
        //异步提交
        new Producer("g9",false,15000).run();
    }
}

class CallBack implements Callback {

    private final long startTime;
    private final int key;
    private final String message;

    public CallBack(long startTime, int key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }

    /**
     * A callback method the user can implement to provide asynchronous handling of request completion. This method will
     * be called when the record sent to the server has been acknowledged. When exception is not null in the callback,
     * metadata will contain the special -1 value for all fields except for topicPartition, which will be valid.
     *
     * @param metadata  The metadata for the record that was sent (i.e. the partition and offset). Null if an error
     *                  occurred.
     * @param exception The exception thrown during processing of this record. Null if no error occurred.
     */
    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                            "), " +
                            "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            exception.printStackTrace();
        }
    }


}
