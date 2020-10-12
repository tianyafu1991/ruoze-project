package com.ruozedata.ruozeplatform.scheduler.cluster;

import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@EnableScheduling
public class MetricsScheduler {

    @Value("${ruozedata.hadoop.nn.uri}")
    private String nnUri;

    @Value("${ruozedata.hadoop.rm.uri}")
    private String rmUri;

    Logger logger = LoggerFactory.getLogger(getClass());

    FastDateFormat format = FastDateFormat.getInstance("HH:mm:ss");

    @Scheduled(fixedRate = 5000)
    public void test01(){
        logger.info("当前执行时间:"+format.format(new Date()));
        logger.info("HDFS访问地址:"+ nnUri);
        logger.info("YARN访问地址:"+ rmUri);
    }
}
