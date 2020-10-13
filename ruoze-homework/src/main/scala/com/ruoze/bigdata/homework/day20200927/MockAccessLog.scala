package com.ruoze.bigdata.homework.day20200927

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.{Random, StringJoiner}

object MockAccessLog {

  private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss")

  private val random = new Random()


  //time字段 [01/10/2020:05:59:34]
  def mockTime: String = {
    val dateFormat: String = LocalDateTime.now().format(dateTimeFormatter)
    f"[${dateFormat}]"
  }

  // ip字段
  def MockIp: String = {
    val range: Array[Array[Int]] = Array(Array(607649792, 608174079), // 36.56.0.0-36.63.255.255
      Array(1038614528, 1039007743), // 61.232.0.0-61.237.255.255
      Array(1783627776, 1784676351), // 106.80.0.0-106.95.255.255
      Array(2035023872, 2035154943), // 121.76.0.0-121.77.255.255
      Array(2078801920, 2079064063), // 123.232.0.0-123.235.255.255
      Array(-1950089216, -1948778497), // 139.196.0.0-139.215.255.255
      Array(-1425539072, -1425014785), // 171.8.0.0-171.15.255.255
      Array(-1236271104, -1235419137), // 182.80.0.0-182.92.255.255
      Array(-770113536, -768606209), // 210.25.0.0-210.47.255.255
      Array(-569376768, -564133889)) // 222.16.0.0-222.95.255.255

    val index: Int = random.nextInt(range.size)
    val ip: String = num2ip(range(index)(0) + new Random().nextInt(range(index)(1) - range(index)(0)))
    ip
  }

  /*
   * 将十进制转换成ip地址
   */
  def num2ip(ip: Int): String = {
    val b = new Array[Int](4)
    var x = ""
    b(0) = ((ip >> 24) & 0xff).toInt
    b(1) = ((ip >> 16) & 0xff).toInt
    b(2) = ((ip >> 8) & 0xff).toInt
    b(3) = (ip & 0xff).toInt
    x = Integer.toString(b(0)) + "." + Integer.toString(b(1)) + "." + Integer.toString(b(2)) + "." + Integer.toString(b(3))
    x
  }

  //proxyIp字段
  def mockProxyIp: String = "-"

  //referer
  def mockReferer: String = "-"

  //responseTime字段
  def mockResponseTime: String = random.nextInt(5000).toString

  //method字段
  def mockMethods: String = {
    val methods: Array[String] = Array("GET", "POST")
    methods(random.nextInt(methods.size))
  }

  //url字段
  def mockUrl: String = {
    val urlBuilder = new StringBuilder
    val randomInt: Int = random.nextInt(1000)
    val protocolPrefix = "http://"
    val domain = "ruozedata"
    val path = "/video/av5216721"
    val params = "?a=b&c=d"
    urlBuilder.append(protocolPrefix).append(domain + randomInt)
    if (randomInt % 3 == 0) urlBuilder.append(path + randomInt)
    if (randomInt % 7 == 0) urlBuilder.append(params)
    urlBuilder.toString
  }

  //httpCode字段
  def mockHttpCode: String = {
    val httpCodes = Array("200", "404", "500")
    httpCodes(random.nextInt(httpCodes.size))
  }

  //requestSize字段
  def mockRequestSize: String = {
    random.nextInt(5000).toString
  }

  //responseSize字段
  def mockResponseSize(i: Int): String = {
    if (i % 17 == 0) "-" else random.nextInt(5000).toString
  }

  //cache字段
  def mockCache: String = {
    val caches: Array[String] = Array("MISS", "HIT")
    caches(random.nextInt(caches.size))
  }

  //uaHead字段
  def mockUaHead: String = "Mozilla/5.0（compatible; AhrefsBot/5.0; +http://ahrefs.com/robot/）"

  //fileType字段
  def mockFileType: String = "text/html"

  /**
   * 主入口方法
   *
   * @return
   */
  def mockAccessLog(i:Int): String = {
    val joiner = new StringJoiner(" ")
    val time: String = mockTime
    val ip: String = MockIp
    val proxyIp: String = mockProxyIp
    val responseTime: String = mockResponseTime
    val referer: String = mockReferer
    val method: String = mockMethods
    val url: String = mockUrl
    val httpCode: String = mockHttpCode
    val requestSize: String = mockRequestSize
    val responseSize: String = mockResponseSize(i)
    val cache: String = mockCache
    val uaHead: String = mockUaHead
    val fileType: String = mockFileType
    joiner.add(time)
      .add(ip)
      .add(proxyIp)
      .add(responseTime)
      .add(referer)
      .add(method)
      .add(url)
      .add(httpCode)
      .add(requestSize)
      .add(responseSize)
      .add(cache)
      .add(uaHead)
      .add(fileType)
    joiner.toString
  }


  def main(args: Array[String]): Unit = {
//    val accessLog: String = mockAccessLog
//    println(accessLog)
  }
}
