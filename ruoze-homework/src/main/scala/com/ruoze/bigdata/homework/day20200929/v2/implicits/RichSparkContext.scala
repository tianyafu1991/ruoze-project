package com.ruoze.bigdata.homework.day20200929.v2.implicits

import java.io.File

import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path, RemoteIterator}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

class RichSparkContext(val sc: SparkContext) {

  def defaultMinPartitions: Int = sc.defaultMinPartitions

  def textFile(path: String, paths: String*)(minPartitions: Int): RDD[String] = {
    val pathsArray = new Array[String](paths.length + 1)
    pathsArray(0) = path
    var i = 1
    for (elem <- paths.iterator) {
      pathsArray(i) = elem
      i += 1
    }
    sc.union(pathsArray.map(x => {
      sc.textFile(x, minPartitions)
    }))
  }

  def textFile(paths: String*)(minPartitions: Int): RDD[String] = {
    val firstPath = paths(0)
    paths.slice(1, paths.length)
    textFile(firstPath, paths.slice(1, paths.length).toArray: _*)(minPartitions)
  }


  def textFile(path: String, minPartitions: Int = defaultMinPartitions): RDD[String] = {
    val paths: Array[String] = path.split(",")
    textFile(paths: _*)(minPartitions)
  }

  def recursiveFileLookup(path: String, paths: ArrayBuffer[String]): ArrayBuffer[String] = {
    val fsPath = new Path(path)
    val fs: FileSystem = FileSystem.get(sc.hadoopConfiguration)
    val remoteIterator: RemoteIterator[LocatedFileStatus] = fs.listFiles(fsPath, true)
    while (remoteIterator.hasNext){
      val status: LocatedFileStatus = remoteIterator.next()
      if("_success" != status.getPath.getName.toLowerCase){
        paths.append(status.getPath.toString)
      }
    }
    paths
  }

  def recursiveTextFile(path: String): RDD[String] = {
    var paths: ArrayBuffer[String] = ArrayBuffer[String]()
    paths = recursiveFileLookup(path, paths)
    textFile(paths: _*)(defaultMinPartitions)

  }


}

object RichSparkContext {
  implicit def sc2RichSc(sc: SparkContext): RichSparkContext = new RichSparkContext(sc)
}