package com.ruoze.bigdata.homework.day20200929.test


import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer
import scala.reflect.io.{File, Path}

object recursiveApp {

  def recursiveFileLookup(path: String, paths: ArrayBuffer[String]): ArrayBuffer[String] = {
    val file: File = File(path)
    if (file.isDirectory) {
//      paths.append(file.toString)
      val subPaths: Array[Path] = file.toDirectory.list.toArray
      for (ele <- subPaths) {
        recursiveFileLookup(ele.toString(), paths)
      }
    } else {
      paths.append(file.toString())
    }
    paths
  }

  def recursiveTextFile(path: String): Array[String] = {
    var paths: ArrayBuffer[String] = ArrayBuffer[String]()
    paths = recursiveFileLookup(path, paths)
    paths.toArray
  }

  def main(args: Array[String]): Unit = {
    val path = "F:\\ruozedata\\code\\ruoze-project\\ruoze-homework\\src\\main\\scala\\com\\ruoze\\bigdata\\homework\\day20200929\\data"
    val paths: Array[String] = recursiveTextFile(path)
    paths.foreach(println)

  }

}


