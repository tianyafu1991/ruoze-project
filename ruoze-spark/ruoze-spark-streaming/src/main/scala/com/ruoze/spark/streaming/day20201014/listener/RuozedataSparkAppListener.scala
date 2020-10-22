package com.ruoze.spark.streaming.day20201014.listener

import com.ruoze.spark.streaming.utils.{MsgUtils, RedisUtils}
import org.apache.spark.SparkConf
import org.apache.spark.executor.TaskMetrics
import org.apache.spark.internal.Logging
import org.apache.spark.scheduler.{SparkListener, SparkListenerTaskEnd}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json
import redis.clients.jedis.Jedis

import scala.collection.mutable

class RuozedataSparkAppListener(conf:SparkConf) extends SparkListener with Logging{

  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    val jedis: Jedis = RedisUtils.getJedis
    jedis.select(10)

    val appName: String = conf.get("spark.app.name")

    logError(s"当前的Application的名称是:${appName}")
    val metrics: TaskMetrics = taskEnd.taskMetrics

    val taskMetricsMap = mutable.HashMap(
      "executorDeserializeTime" ->metrics.executorDeserializeTime,
        "executorDeserializeCpuTime" ->metrics.executorDeserializeCpuTime,
        "executorRunTime" ->metrics.executorRunTime,
        "executorCpuTime" ->metrics.executorCpuTime,
        "resultSize" ->metrics.resultSize,
        "jvmGCTime" ->metrics.jvmGCTime,
        "resultSerializationTime" ->metrics.resultSerializationTime,
        "memoryBytesSpilled" ->metrics.memoryBytesSpilled,
        "diskBytesSpilled" ->metrics.diskBytesSpilled,
        "peakExecutionMemory" ->metrics.peakExecutionMemory
    )

    val taskMetricsKey = s"tasks_${appName}"
    println(taskMetricsKey)


    jedis.set(taskMetricsKey,Json(DefaultFormats).write(taskMetricsMap))
    jedis.expire(taskMetricsKey,36000)

    if(true == conf.get("spark.enable.send.mail","true")){
      MsgUtils.send("1045159389@qq.com",taskMetricsKey,"作业执行成功")
    }


  }

}
