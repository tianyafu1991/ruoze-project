package com.ruozedata.ruozesparksb.controller

import com.ruozedata.ruozesparksb.service.AccessService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.{PathVariable, RequestMapping, RestController}

@RestController
@RequestMapping(value = Array("/access"))
class AccessController @Autowired()(val accessService: AccessService) {

  // http://hadoop:9527/spark/access/20190101
  @RequestMapping(value = Array("/{date}"))
  def processAccessLog(@PathVariable date: String): Unit = {
    accessService.processAccessLog(date)
  }

}
