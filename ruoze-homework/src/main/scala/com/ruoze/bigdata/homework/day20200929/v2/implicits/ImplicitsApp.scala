package com.ruoze.bigdata.homework.day20200929.v2.implicits

import org.apache.spark.SparkContext

object ImplicitsApp {

  implicit def sc2RichSc(sc: SparkContext): RichSparkContext = new RichSparkContext(sc)


}
