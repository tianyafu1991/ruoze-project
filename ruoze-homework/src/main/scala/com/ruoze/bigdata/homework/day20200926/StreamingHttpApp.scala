package com.ruoze.bigdata.homework.day20200926

import com.alibaba.fastjson.JSON
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * tianya,11111111,116.480881,39.989410
 * pk,11111111,116.480881,39.989410
 */
object StreamingHttpApp extends Logging {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(this.getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    val key = "59ac9a835eaf3c97f5c10ce6aef0b564"

    ssc.socketTextStream("hadoop01", 9527)
      .foreachRDD(
        rdd => {
          if (!rdd.isEmpty()) {
            rdd.foreachPartition(
              partition => {
//                val httpClient = HttpClientPoolManager.getHttpClient
                val httpClient = HttpClients.createMinimal(PoolingHttpClientConnectionManagerSingleton.getInstance())
                logError(s"...........${httpClient}")

                partition.foreach(x => {
                  val splits = x.split(",")
                  val userId = splits(0)
                  val time = splits(1)
                  val longtitude = splits(2).toDouble
                  val latitude = splits(3).toDouble
                  val url = s"https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${longtitude},${latitude}"

                  var province: String = ""
                  var response: CloseableHttpResponse = null
                  try {
                    val httpGet = new HttpGet(url)
                    response = httpClient.execute(httpGet)
                    val status = response.getStatusLine.getStatusCode
                    val entity = response.getEntity


                    if (status == 200) {
                      val result = EntityUtils.toString(entity)
                      val json = JSON.parseObject(result)

                      val regeocode = json.getJSONObject("regeocode")
                      if (null != regeocode) {
                        val addressComponent = regeocode.getJSONObject("addressComponent")
                        province = addressComponent.getString("province")
                        logError(s"////////////////${province}")
                        //                        Access(userId, time, longtitude, latitude, province)
                      }
                    }
                  } catch {
                    case e: Exception => e.printStackTrace()
                  } finally {
                    if (null != response) {
                      response.close()
                    }
                  }
                })

                //TODO 这里不能close,httpclient中有一个PoolingHttpClientConnectionManager的引用，这里关闭了之后，manager也就关闭了
//                httpClient.close()

              }
            )
          }
        }


      )


    ssc.start()
    ssc.awaitTermination()
  }

}

case class Access(userId: String, time: String, longtitude: Double, latitude: Double, province: String)
