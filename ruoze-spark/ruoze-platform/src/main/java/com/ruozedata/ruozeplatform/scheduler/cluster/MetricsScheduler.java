package com.ruozedata.ruozeplatform.scheduler.cluster;

import cn.hutool.core.date.DateUtil;
import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.domain.cluster.HadoopMetrics;
import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;
import com.ruozedata.ruozeplatform.service.cluster.MetricsService;
import com.ruozedata.ruozeplatform.utils.HttpClientUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@EnableScheduling
public class MetricsScheduler {

    @Value("${ruozedata.hadoop.nn.uri}")
    private String nnUri;

    @Value("${ruozedata.hadoop.rm.uri}")
    private String rmUri;

    @Autowired
    private MetricsService metricsService;

    Logger logger = LoggerFactory.getLogger(getClass());

    FastDateFormat format = FastDateFormat.getInstance("HH:mm:ss");

    /*@Scheduled(fixedRate = 5000)
    public void test01(){
        logger.info("当前执行时间:"+format.format(new Date()));
        logger.info("HDFS访问地址:"+ nnUri);
        logger.info("YARN访问地址:"+ rmUri);
    }*/

    @Scheduled(fixedRate = 5000)
    public void collectMetrics(){

        collectHDFSMetrics();
        collectYarnMetrics();
    }

    HttpClientUtils client = new HttpClientUtils(null);

    public static final String NAMENODE_INFO = "http://%s/jmx?qry=%s";

    public static final String NAMENODEINFO = "Hadoop:service=NameNode,name=NameNodeInfo";
    public static final String FSNAMESYSTEM = "Hadoop:service=NameNode,name=FSNamesystem";
    public static final String FSNAMESYSTEMSTATE = "Hadoop:service=NameNode,name=FSNamesystemState";

    public static final String QUEUEMETRICS = "Hadoop:service=ResourceManager,name=QueueMetrics,q0=root";
    public static final String CLUSTERMETRICS = "Hadoop:service=ResourceManager,name=ClusterMetrics";

    public void collectYarnMetrics(){
        String queueUrl = String.format(NAMENODE_INFO,rmUri,QUEUEMETRICS);
        String clusterUrl = String.format(NAMENODE_INFO,rmUri,CLUSTERMETRICS);

        YARNSummary yarnSummary = new YARNSummary();
        try {
            HadoopMetrics queueMetrics = client.get(HadoopMetrics.class, queueUrl, null, null);
            HadoopMetrics clusterMetrics = client.get(HadoopMetrics.class, clusterUrl, null, null);

            yarnSummary.setTagQueue(queueMetrics.getMetricsValue("tag.Queue").toString());
            yarnSummary.setAllocatedMB(Long.parseLong(queueMetrics.getMetricsValue("AllocatedMB").toString()));

            yarnSummary.setAllocatedVCores(Long.parseLong(queueMetrics.getMetricsValue("AllocatedVCores").toString()));
            yarnSummary.setAllocatedContainers(Long.parseLong(queueMetrics.getMetricsValue("AllocatedContainers").toString()));

            yarnSummary.setAvailableMB(Long.parseLong(queueMetrics.getMetricsValue("AvailableMB").toString()));
            yarnSummary.setAvailableVCores(Long.parseLong(queueMetrics.getMetricsValue("AvailableVCores").toString()));

            yarnSummary.setReservedMB(Long.parseLong(queueMetrics.getMetricsValue("ReservedMB").toString()));
            yarnSummary.setReservedVCores(Long.parseLong(queueMetrics.getMetricsValue("ReservedVCores").toString()));
            yarnSummary.setReservedContainers(Long.parseLong(queueMetrics.getMetricsValue("ReservedContainers").toString()));

            yarnSummary.setActiveUsers(Long.parseLong(queueMetrics.getMetricsValue("ActiveUsers").toString()));
            yarnSummary.setActiveApplications(Long.parseLong(queueMetrics.getMetricsValue("ActiveApplications").toString()));

            yarnSummary.setNumActiveNMs(Long.parseLong(clusterMetrics.getMetricsValue("NumActiveNMs").toString()));
            yarnSummary.setNumLostNMs(Long.parseLong(clusterMetrics.getMetricsValue("NumLostNMs").toString()));

            yarnSummary.setDelete(false);
            yarnSummary.setCreateTime((int) DateUtil.currentSeconds());

            //SAVE

            metricsService.addYarnSummary(yarnSummary);

            System.out.println(yarnSummary);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void collectHDFSMetrics(){


        String url = String.format(NAMENODE_INFO,nnUri,NAMENODEINFO);
        HDFSSummary hdfsSummary = new HDFSSummary();
        try {
            HadoopMetrics hadoopMetrics = client.get(HadoopMetrics.class, url, null, null);
            hdfsSummary.setTotal(Long.parseLong(hadoopMetrics.getMetricsValue("Total").toString()));
            hdfsSummary.setDfsUsed(Long.parseLong(hadoopMetrics.getMetricsValue("Used").toString()));
            hdfsSummary.setPercentUsed(
                    Float.parseFloat(hadoopMetrics.getMetricsValue("PercentUsed").toString()));
            hdfsSummary
                    .setDfsFree(Long.parseLong(hadoopMetrics.getMetricsValue("Free").toString()));
            hdfsSummary.setNonDfsUsed(
                    Long.parseLong(hadoopMetrics.getMetricsValue("NonDfsUsedSpace").toString()));
            hdfsSummary.setTotalBlocks(
                    Long.parseLong(hadoopMetrics.getMetricsValue("TotalBlocks").toString()));
            hdfsSummary
                    .setTotalFiles(Long.parseLong(hadoopMetrics.getMetricsValue("TotalFiles").toString()));
            hdfsSummary.setMissingBlocks(
                    Long.parseLong(hadoopMetrics.getMetricsValue("NumberOfMissingBlocks").toString()));

            String fsNameSystemStateUrl = String
                    .format(NAMENODE_INFO, nnUri, FSNAMESYSTEMSTATE);
            HadoopMetrics fsNameSystemMetrics = client
                    .get(HadoopMetrics.class, fsNameSystemStateUrl, null, null);
            hdfsSummary
                    .setLiveDataNodeNums((int) fsNameSystemMetrics.getMetricsValue("NumLiveDataNodes"));
            hdfsSummary
                    .setDeadDataNodeNums((int) fsNameSystemMetrics.getMetricsValue("NumDeadDataNodes"));

            hdfsSummary.setDelete(false);
            hdfsSummary.setCreateTime((int) DateUtil.currentSeconds());

            metricsService.addHDFSSummary(hdfsSummary);

            System.out.println(hdfsSummary);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
