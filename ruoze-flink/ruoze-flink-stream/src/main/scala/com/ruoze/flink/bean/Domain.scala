package com.ruoze.flink.bean

/**
 * @author PK哥
 **/
object Domain {

  case class Access(time:Long, domain:String, traffic:Double)

  case class Student(id:Int, name:String,age:Int)

  case class SplitAcess(province:String, city:String, traffic: Long)

  case class Temperature(name:String, time:Long, temperature:Float)
}
