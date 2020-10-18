package com.ruoze.bigdata.homework.day20200929.v2.commitProtocol

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

import com.ruoze.bigdata.homework.day20200929.utils.CompressUtils
import com.ruoze.bigdata.homework.day20200929.v2.implicits.RichConfiguration
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter
import org.apache.hadoop.mapreduce.{OutputCommitter, TaskAttemptContext}
import org.apache.spark.internal.io.HadoopMapReduceCommitProtocol

import scala.collection.mutable

class UploadHadoopMapReduceCommitProtocol(jobId: String, path: String, dynamicPartitionOverwrite: Boolean)
  extends HadoopMapReduceCommitProtocol(jobId, path, dynamicPartitionOverwrite) {

  private val timeFormatter = new SimpleDateFormat("yyyyMMddHH")

  @transient private var committer: OutputCommitter = _

  @transient private var partitionPaths: mutable.Set[String] = null

  private def stagingDir = new Path(path, ".spark-staging-" + jobId)

  //partitionDomain=ruoze.ke.qq.com/partitionTime=2020100101
  def getFilename(dir: Option[String], hadoopConfiguration: Configuration): String = {
    val jobConfKeyPrefix: String = hadoopConfiguration.get("job.conf.prefix")
    val suffix = ".log"
    val dirPath = dir.getOrElse("")
    val splits = dirPath.split("/")
    val time = splits(1).split("=")(1)
    val domain = splits(0).split("=")(1)
    val jobConfName = jobConfKeyPrefix + domain
    val confSplits: Array[String] = hadoopConfiguration.get(jobConfName).split("\t")

    val compressCodec: String = confSplits(2)
    val compressCodecSuffix: String = CompressUtils.getCompressCodecSuffix(compressCodec)
    val fileNamePrefix: String = confSplits(3)
    val fileNameTimeFormat: String = confSplits(4)
    val fileNameSuffix: String = confSplits(5)
    val date: Date = timeFormatter.parse(time)
    val customSdf = new SimpleDateFormat(fileNameTimeFormat)
    val customTime: String = customSdf.format(date)
    if (null == fileNamePrefix || "" == fileNamePrefix) {
      f"${domain}_${customTime}.${fileNameSuffix}.${compressCodecSuffix}"
    } else {
      f"${domain}_${fileNamePrefix}_${customTime}.${fileNameSuffix}.${compressCodecSuffix}"
    }

  }

  //partitionDomain=ruoze.ke.qq.com/partitionTime=2020100101
  def getDirNew(dir: Option[String]): Option[String] = {
    if (!dir.isEmpty) {
      val dirStr = dir.get
      val splits = dirStr.split("/")
      var time = splits(1).split("=")(1)
      val domain = splits(0).split("=")(1)
      time = time.substring(0, time.length - 2)
      Some(s"${domain}/${time}")
    } else {
      Option.empty
    }

  }

  override def newTaskTempFile(taskContext: TaskAttemptContext, dir: Option[String], ext: String): String = {
    val hadoopConfiguration: Configuration = taskContext.getConfiguration

    val newDir = getDirNew(dir)

    val filename = getFilename(dir, hadoopConfiguration)

    val stagingDir: Path = committer match {
      case _ if dynamicPartitionOverwrite =>
        assert(newDir.isDefined,
          "The dataset to be written must be partitioned when dynamicPartitionOverwrite is true.")
        partitionPaths += newDir.get
        this.stagingDir
      // For FileOutputCommitter it has its own staging path called "work path".
      case f: FileOutputCommitter =>
        new Path(Option(f.getWorkPath).map(_.toString).getOrElse(path))
      case _ => new Path(path)
    }
    newDir.map { d =>
      new Path(new Path(stagingDir, d), filename).toString
    }.getOrElse(new Path(stagingDir, filename).toString)
  }

  /* override def newTaskTempFile(taskContext: TaskAttemptContext, dir: Option[String], ext: String): String = {
     //    val dirPath = "time=2020100105/domain=ruoze.ke.qq.com"
     val maybeString: Option[String] = dir.map("1" + _)
     val filename = getFilename(maybeString)

     val stagingDir: Path = committer match {
       case _ if dynamicPartitionOverwrite =>
         assert(maybeString.isDefined,
           "The dataset to be written must be partitioned when dynamicPartitionOverwrite is true.")
         partitionPaths += maybeString.get
         this.stagingDir
       // For FileOutputCommitter it has its own staging path called "work path".
       case f: FileOutputCommitter =>
         new Path(Option(f.getWorkPath).map(_.toString).getOrElse(path))
       case _ => new Path(path)
     }
     maybeString.map { d =>
       new Path(new Path(stagingDir, d), filename).toString
     }.getOrElse(new Path(stagingDir, filename).toString)
   }*/
}
