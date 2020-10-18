package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.etl

import java.lang.reflect.Method
import java.sql.PreparedStatement
import java.text.SimpleDateFormat
import com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.domain.Access
import com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.utils.{FileUtils, SparkLogETLUtils2}
import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path, RemoteIterator}
import org.apache.spark.rdd.{JdbcRDD, RDD}
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext, SparkException}
import org.lionsoul.ip2region.{DbConfig, DbSearcher}

/**
 * 不需要用
 */
object SparkETL3 {

  def main(args: Array[String]): Unit = {
    //    val uri = "hdfs://hadoop01:9000"
    val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val startTimeMillis = System.currentTimeMillis()
    val startTime = sdf.format(sdf.parse(sdf.format(startTimeMillis)))
    val dbPath = "ip2region.db"
    val conf = new SparkConf()
    val sc = new SparkContext(conf)
    val executionTime: String = conf.get("spark.execute.time", "20190101")
    val rawPathConf: String = conf.get("spark.dw.raw.path", "/ruozedata/dw/raw/access")
    val tmpPathConf: String = conf.get("spark.dw.tmp.path", "/ruozedata/dw/ods_tmp/access")
    val odsPathConf: String = conf.get("spark.dw.ods.path", "/ruozedata/dw/ods/access")
    val input = s"${rawPathConf}/${executionTime}"
    val tmpPath = s"${tmpPathConf}/${executionTime}"
    val output = s"${odsPathConf}/d=${executionTime}"
    val configuration = sc.hadoopConfiguration
    FileUtils.delete(configuration, tmpPath)
    FileUtils.delete(configuration, output)
    val fileSystem = FileSystem.get(configuration)
    val tmpHDSFPath = new Path(tmpPath)
    //    if (fileSystem.exists(tmpHDSFPath)) {
    //      fileSystem.delete(tmpHDSFPath, true)
    //    }

    val m1: (DbSearcher) => Method = SparkLogETLUtils2.getMethod _

    val s1: (DbConfig, String) => DbSearcher = SparkLogETLUtils2.getDbSearcher _

    val c1: () => DbConfig = SparkLogETLUtils2.getDbConfig _

    val l1: (String, DbSearcher, Method) => Access = SparkLogETLUtils2.parseLog _

    val validAccumulator: LongAccumulator = sc.longAccumulator("有效数据")
    val notValidAccumulator: LongAccumulator = sc.longAccumulator("无效数据")
    val allAccumulator: LongAccumulator = sc.longAccumulator("全部数据")

    val value: RDD[String] = sc.textFile(input).filter(x => {
      allAccumulator.add(1L)
      val responsesize: String = x.split("\t")(9)
      if ("-".equals(responsesize)) {
        notValidAccumulator.add(1L)
      }
      !"-".equals(responsesize)
    }).mapPartitions(
      iter => {
        val searcher: DbSearcher = s1(c1(), dbPath)
        val method: Method = m1(s1(c1(), dbPath))
        iter.map(
          log => {
            val access = l1(log, searcher, method)
            validAccumulator.add(1L)
            access.toString
          }
        )
      }
    )
    value.saveAsTextFile(tmpPath)

    //移除对应的原有的分区目录
    println("开始移除对应的原有的分区目录" + output)
    val outputPath = new Path(output)
    //    if (fileSystem.exists(outputPath)) fileSystem.delete(outputPath, true)
    FileUtils.delete(configuration, output)
    //创建对应的分区目录
    println("开始创建对应的分区目录" + output)
    fileSystem.mkdirs(outputPath)
    //从临时目录中移动数据到对应的分区目录下
    println("开始从临时目录" + tmpPath + "中移动数据到对应的分区目录下" + output)
    val remoteIterator: RemoteIterator[LocatedFileStatus] = fileSystem.listFiles(tmpHDSFPath, true)
    while (remoteIterator.hasNext) {
      val fileStatus = remoteIterator.next()
      val filePath = fileStatus.getPath()
      fileSystem.rename(filePath, outputPath)
    }

    //删除临时目录及数据
    println("开始删除临时目录及数据" + tmpPath)
    fileSystem.delete(tmpHDSFPath, true)

    val endTimeMillis = System.currentTimeMillis()
    val endTime = sdf.format(sdf.parse(sdf.format(endTimeMillis)))

    val runtimes = endTimeMillis - startTimeMillis

    val jobInfos = List(
      JobInfo("spark_etl",allAccumulator.value.toInt,validAccumulator.value.toInt,notValidAccumulator.value.toInt,runtimes.toInt,executionTime,startTime,endTime)
    )

    val jobInfoRDD: RDD[JobInfo] = sc.parallelize(jobInfos)

    jobInfoRDD.foreachPartition(
      partition => {
        val conn = SparkLogETLUtils2.getConnection()
        val sql = "insert into job_infos(task_name,totals,formats,`errors`,run_times,`day`,start_time,end_time) values(?,?,?,?,?,?,?,?)"
        val statement: PreparedStatement = conn.prepareStatement(sql)
        partition.foreach(
          jobInfo => {
            statement.setString(1, jobInfo.taskName)
            statement.setInt(2, jobInfo.totals)
            statement.setInt(3, jobInfo.formats)
            statement.setInt(4, jobInfo.errors)
            statement.setInt(5, jobInfo.runTimes)
            statement.setString(6, jobInfo.day)
            statement.setString(7, jobInfo.startTime)
            statement.setString(8, jobInfo.endTime)
            statement.executeUpdate
          }
        )
      }
    )


    sc.stop()
  }

}


case class JobInfo(taskName:String,totals:Int,formats:Int,errors:Int,runTimes:Int,day:String,startTime:String,endTime:String)
