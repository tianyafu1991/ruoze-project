package com.ruoze.spark.streaming.utils

import com.alibaba.fastjson.JSON
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object HttpClientApp {

  def main(args: Array[String]): Unit = {

    val longtitude = 116.480881

    val latitude = 39.989410

    val key = "59ac9a835eaf3c97f5c10ce6aef0b564"

    val url = s"https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${longtitude},${latitude}"

    val httpClient = HttpClients.createDefault()
    val httpGet = new HttpGet(url)
    val response = httpClient.execute(httpGet)


    try{
      val status = response.getStatusLine.getStatusCode
      val entity = response.getEntity


      if(status == 200){
        val result = EntityUtils.toString(entity)
        val json = JSON.parseObject(result)

        val regeocode = json.getJSONObject("regeocode")
        if(null != regeocode){
          val addressComponent = regeocode.getJSONObject("addressComponent")
          val province = addressComponent.get("province")
          println(province)
        }
        //      println(json)
      }
    }catch {
      case e:Exception => e.printStackTrace()
    }

    httpClient.close()




















  }

}
