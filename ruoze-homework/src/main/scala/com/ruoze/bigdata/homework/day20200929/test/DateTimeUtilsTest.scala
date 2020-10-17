package com.ruoze.bigdata.homework.day20200929.test

import java.text.SimpleDateFormat
import java.time.{LocalDateTime, Month}
import java.time.format.DateTimeFormatter
import java.util.Date


object DateTimeUtilsTest {

  val format: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss")

  def main(args: Array[String]): Unit = {
    val time = "01/10/2020:02:39:34"
    val sdf = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss")
    val sdfDate: Date = sdf.parse(time)

    val localDateTime: LocalDateTime = LocalDateTime.parse(time, format)
    val year: Int = localDateTime.getYear
    val month: Int = localDateTime.getMonthValue
    val day: Int = localDateTime.getDayOfMonth
    val hour: Int = localDateTime.getHour
    val minute: Int = localDateTime.getMinute
    val second: Int = localDateTime.getSecond
    println(sdfDate)
    println(localDateTime)
    println(s"${year}\t${month}\t${day}\t${hour}\t${minute}\t${second}")
  }

}
