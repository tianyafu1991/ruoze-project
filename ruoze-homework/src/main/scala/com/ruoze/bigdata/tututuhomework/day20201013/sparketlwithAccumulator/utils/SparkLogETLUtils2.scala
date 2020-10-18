package com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.utils

import java.lang.reflect.Method
import java.sql.{Connection, DriverManager}
import java.text.{ParseException, SimpleDateFormat}
import java.util.{Calendar, Date}

import com.ruoze.bigdata.tututuhomework.day20201013.sparketlwithAccumulator.domain.Access
import org.lionsoul.ip2region.{DataBlock, DbConfig, DbSearcher}

object SparkLogETLUtils2 {

  def getConnection(): Connection = {
    Class.forName("com.mysql.jdbc.Driver")
    val url = "jdbc:mysql://hadoop:3306/ruozedata?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8"
    val username = "root"
    val password = "root"
    val conn = DriverManager.getConnection(url,username,password)
    conn
  }
  def getMethod(searcher:DbSearcher): Method = searcher.getClass.getMethod("btreeSearch", classOf[String])

  def getDbSearcher(config: DbConfig, dbPath: String): DbSearcher = new DbSearcher(config,dbPath)

  def getDbConfig(): DbConfig = new DbConfig

  def parseLog(log:String,searcher:DbSearcher,m:Method):Access = {
    val split: Array[String] = log.split("\t")
    val access = new Access()
    val ip = split(1)
    access.setIp(ip)
    val proxyIp = split(2)
    access.setProxyIp(proxyIp)
    val responsetime = split(3).toLong
    access.setResponseTime(responsetime)
    val referer = split(4)
    access.setReferer(referer)
    val method = split(5)
    access.setMethod(method)
    val url = split(6)
    access.setUrl(url)
    val httpcode = split(7)
    access.setHttp(httpcode)
    val requestsize = split(8).toLong
    access.setRequestSize(requestsize)
    val responsesize = split(9).toLong
    access.setResponseSize(responsesize)
    val cache = split(10)
    access.setCache(cache)
    val ua = split(11)
    access.setUaHead(ua)
    val fileType = split(12)
    access.setType(fileType)
    //解析ip
    var province = ""
    var city = ""
    var isp = ""

    val dataBlock = m.invoke(searcher, ip).asInstanceOf[DataBlock]

    val region: String = dataBlock.getRegion
    val bloks: Array[String] = region.split("\\|")
    for (block <- bloks) {
      province = bloks(2)
      city = bloks(3)
      isp = bloks(4)
    }
    access.setProvince(province)
    access.setCity(city)
    access.setIsp(isp)

    //解析url
    val urlSplits = url.split("\\?")
    val urlSplits2 = urlSplits(0).split(":")

    val http = urlSplits2(0)
    access.setHttp(http)
    val urlSpliting = urlSplits2(1).substring(2)
    var domain = urlSpliting
    var path = ""
    if (urlSpliting.contains("/")) {
      domain = urlSpliting.substring(0, urlSpliting.indexOf("/"))
      path = urlSpliting.substring(urlSpliting.indexOf("/"))
    }
    access.setDomain(domain)

    access.setPath(path)
    val params = if (urlSplits.length == 2) urlSplits(1)
    else null
    access.setParams(params)

    //解析时间
    val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss")

    var timeParse: Date = null
    try {
      var time = split(0)
      time = time.substring(1, time.length - 1)
      timeParse = simpleDateFormat.parse(time)
      val calendar: Calendar = Calendar.getInstance
      calendar.setTime(timeParse)
      val year: String = String.valueOf(calendar.get(Calendar.YEAR))
      var month: String = String.valueOf(calendar.get(Calendar.MONTH))
      var day: String = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
      month = if (month.toInt < 10) {
        "0" + month
      }
      else {
        month
      }
      day = if (day.toInt < 10) {
        "0" + day
      }
      else {
        day
      }
      access.setYear(year)
      access.setMonth(month)
      access.setDay(day)
    } catch {
      case e: ParseException =>
        e.printStackTrace()
    }
    access
  }
}
