package com.ruoze.bigdata.tututuhomework.day20201130

trait Template {

  def init()

  def biz()

  def stop()

  try{
    init()
    biz()
  }catch {
    case e:Exception => {
      println()
    }
  }finally {
    stop()
  }

}
