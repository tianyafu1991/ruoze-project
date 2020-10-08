package com.ruoze.bigdata.homework.day20200929.utils

import java.text.{ParseException, SimpleDateFormat}
import java.util.{Calendar, Date}

object DateUtils {

  val simpleDateFormat: SimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy:hh:mm:ss")

  def analysistime(time: String, simpleDateFormat: SimpleDateFormat): Array[String] = {
    var timeParse: Date = null
    try {
      val realtime = time.substring(1, time.length - 1)
      //      val realtime = time.substring(1, time.length)
      timeParse = simpleDateFormat.parse(realtime)
      val calendar: Calendar = Calendar.getInstance
      calendar.setTime(timeParse)
      val year: String = String.valueOf(calendar.get(Calendar.YEAR))
      val month: String = String.valueOf(calendar.get(Calendar.MONTH))
      val day: String = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
      val hour: String = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))
      val realMonth = if (month.toInt < 10) "0" + month else month
      val realDay = if (day.toInt < 10) "0" + day else day
      val realHour = if (hour.toInt < 10) "0" + hour else hour
      return (year :: realMonth :: realDay :: realHour :: Nil).toArray
    } catch {
      case e: ParseException => e.printStackTrace()
    }
    null
  }

  def customFormatTime(time: String, format: String): String = {
    val customSimpleDateFormat: SimpleDateFormat = new SimpleDateFormat(format)

    var timeParse: Date = null

    try {
      val realtime = time.substring(1, time.length - 1)
      timeParse = simpleDateFormat.parse(realtime)
      return customSimpleDateFormat.format(timeParse)
    } catch {
      case e: Exception => e.printStackTrace()
    }
    null
  }

}
