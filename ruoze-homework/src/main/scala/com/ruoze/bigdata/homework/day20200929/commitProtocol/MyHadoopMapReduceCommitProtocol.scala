package com.ruoze.bigdata.homework.day20200929.commitProtocol

import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter
import org.apache.hadoop.mapreduce.{OutputCommitter, TaskAttemptContext}
import org.apache.spark.internal.Logging
import org.apache.spark.internal.io.HadoopMapReduceCommitProtocol
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

class MyHadoopMapReduceCommitProtocol(jobId: String, path: String, dynamicPartitionOverwrite: Boolean)
  extends HadoopMapReduceCommitProtocol(jobId, path, dynamicPartitionOverwrite) with Logging {


  @transient private var committer: OutputCommitter = _

  @transient private var partitionPaths: mutable.Set[String] = null

  private def stagingDir = new Path(path, ".spark-staging-" + jobId)

  //    val dirPath = "time=2020100105/domain=ruoze.ke.qq.com"
  def getFilename(dir: Option[String]): String = {
    val suffix = ".log"
    val dirPath = dir.getOrElse("")
    val splits = dirPath.split("/")
    val time = splits(0).split("=")(1)
    val domain = splits(1).split("=")(1)
    val compression = if (domain == "ruozedata.com") ".gz" else ".bz2"
    f"${domain}_access_${time}${suffix}${compression}"
//    f"${domain}_access_${time}${suffix}"
  }

  override def newTaskTempFile(taskContext: TaskAttemptContext, dir: Option[String], ext: String): String = {
    /*val conf = spark.conf
    logError("开始getFile之前")
    logError(conf.toString)*/
    val filename = getFilename(dir)

    logError("开始getFile之后")
    val stagingDir: Path = committer match {
      case _ if dynamicPartitionOverwrite =>
        assert(dir.isDefined,
          "The dataset to be written must be partitioned when dynamicPartitionOverwrite is true.")
        partitionPaths += dir.get
        this.stagingDir
      // For FileOutputCommitter it has its own staging path called "work path".
      case f: FileOutputCommitter =>
        new Path(Option(f.getWorkPath).map(_.toString).getOrElse(path))
      case _ => new Path(path)
    }
    dir.map { d =>
      new Path(new Path(stagingDir, d), filename).toString
    }.getOrElse(new Path(stagingDir, filename).toString)
  }



}
