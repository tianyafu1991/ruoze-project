package com.ruoze.flink.source

import com.ruoze.flink.bean.Domain.Access
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{ParallelSourceFunction, RichParallelSourceFunction, SourceFunction}

import scala.util.Random

/**
 * @author PK哥
 **/
class AccessSource03 extends RichParallelSourceFunction[Access]{

  var isRunning = true

  override def open(parameters: Configuration): Unit = super.open(parameters)


  override def close(): Unit = super.close()

  override def getRuntimeContext: RuntimeContext = super.getRuntimeContext

  override def run(ctx: SourceFunction.SourceContext[Access]): Unit = {
    val random = new Random()
    val domains = Array("ruozedata.com","ruoze.ke.qq.com","github.com/ruozedata")

    while (isRunning) {
      val timestamp = System.currentTimeMillis()
      1.to(10).map(x => {
        ctx.collect(Access(timestamp, domains(random.nextInt(domains.length)), random.nextInt(1000) + x))
      })

      Thread.sleep(5000)
    }

  }

  override def cancel(): Unit = {
    isRunning = false
  }
}
