package com.ruoze.bigdata.homework.day20200929.utils

object CompressUtils {

  def getCompressCodecSuffix(codec: String): String = {
    codec match {
      case "gzip" => "gz"
      case "bzip2" => "bz2"
      case _ => "gz"
    }
  }

}
