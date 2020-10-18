package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.utils

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object FileUtils {

  @throws[Exception]
  def delete(conf: Configuration, output: String): Unit = {
    val fileSystem = FileSystem.get(conf)
    val outputPath = new Path(output)
    if (fileSystem.exists(outputPath)) fileSystem.delete(outputPath, true)
  }

}
