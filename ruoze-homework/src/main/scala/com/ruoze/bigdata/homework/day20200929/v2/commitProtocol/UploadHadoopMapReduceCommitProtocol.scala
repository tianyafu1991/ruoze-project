package com.ruoze.bigdata.homework.day20200929.v2.commitProtocol

import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter
import org.apache.hadoop.mapreduce.{OutputCommitter, TaskAttemptContext}
import org.apache.spark.internal.io.HadoopMapReduceCommitProtocol

import scala.collection.mutable

class UploadHadoopMapReduceCommitProtocol(jobId: String, path: String, dynamicPartitionOverwrite: Boolean)
  extends HadoopMapReduceCommitProtocol(jobId, path, dynamicPartitionOverwrite) {

  @transient private var committer: OutputCommitter = _

  @transient private var partitionPaths: mutable.Set[String] = null

  private def stagingDir = new Path(path, ".spark-staging-" + jobId)

  //partitionDomain=ruoze.ke.qq.com/partitionTime=2020100101
  def getFilename(dir: Option[String]): String = {
    val suffix = ".log"
    val dirPath = dir.getOrElse("")
    val splits = dirPath.split("/")
    var time = splits(1).split("=")(1)
    val domain = splits(0).split("=")(1)
    val compression = if (domain == "ruozedata.com") ".gz" else ".bz2"
    if(domain == "ruozedata.com"){
      time = time.substring(2)
      f"${domain}_access_${time}${suffix}${compression}"
    }else {
      time = time.substring(time.length -2)
      f"${domain}_${time}${suffix}${compression}"
    }
  }

  //partitionDomain=ruoze.ke.qq.com/partitionTime=2020100101
  def getDirNew(dir: Option[String]):Option[String]={
    if(!dir.isEmpty){
      val dirStr = dir.get
      val splits = dirStr.split("/")
      var time = splits(1).split("=")(1)
      val domain = splits(0).split("=")(1)
      time = time.substring(0,time.length-2)
      Some(s"${domain}/${time}")
    }else{
      Option.empty
    }

  }

  override def newTaskTempFile(taskContext: TaskAttemptContext, dir: Option[String], ext: String): String = {

    val newDir = getDirNew(dir)

    val filename = getFilename(dir)

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
