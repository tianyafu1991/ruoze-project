package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.etl

import java.lang.reflect.Method

import com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.utils.{FileUtils, SparkLogETLUtils}
import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path, RemoteIterator}
import org.apache.spark.internal.Logging
import org.apache.spark.{SparkConf, SparkContext}
import org.lionsoul.ip2region.{DbConfig, DbSearcher}

object SparkETL2 extends Logging {

  def main(args: Array[String]): Unit = {
    val dbPath = "ip2region.db"
    val conf = new SparkConf()
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[DbConfig], classOf[DbSearcher], classOf[Method]))
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
    FileUtils.delete(configuration, odsPathConf)

    val fileSystem = FileSystem.get(configuration)

    logError(s"原始日志目录:${input}||etl中间临时目录:${tmpPath}||ods目录${output}")


    // 标记为lazy的 等到用的时候才去读取hdfs上的ip2region.db文件
    lazy val config: DbConfig = SparkLogETLUtils.getDbConfig
    lazy val searcher: DbSearcher = SparkLogETLUtils.getDbSearcher(config, dbPath)
    lazy val m: Method = SparkLogETLUtils.getMethod(searcher)

    //先过滤脏数据，filter之后再处理日志解析。这里因为本身只有904条脏数据，filter之后就不用coalesce算子了。
    sc.textFile(input).filter(x => {
      !"-".equals(x.split("\t")(9))
    }).map(log => {
      val access = SparkLogETLUtils.parseLog(log, searcher, m)
      access.toString
    }).saveAsTextFile(tmpPath)

    logError("日志解析完成")

    //移除对应的原有的分区目录
    logError(s"开始移除对应的原有的分区目录${output}")
    FileUtils.delete(configuration, output)
    //创建对应的分区目录
    logError(s"开始创建对应的分区目录${output}")
    val outputPath = new Path(output)
    fileSystem.mkdirs(outputPath)
    //从临时目录中移动数据到对应的分区目录下
    logError(s"开始从临时目录${tmpPath}中移动数据到对应的分区目录下${output}")
    val tmpHDSFPath = new Path(tmpPath)
    val remoteIterator: RemoteIterator[LocatedFileStatus] = fileSystem.listFiles(tmpHDSFPath, true)
    while (remoteIterator.hasNext) {
      val fileStatus = remoteIterator.next()
      val filePath = fileStatus.getPath()
      fileSystem.rename(filePath, outputPath)
    }

    //删除临时目录及数据
    logError(s"开始删除临时目录及数据${tmpPath}")
    FileUtils.delete(configuration, tmpPath)

    sc.stop()
  }

}
