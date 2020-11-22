package com.ruoze.bigdata.tututuhomework.day20201112

object Domain {

  case class Order(orderId: Int, userId: Int)

  case class OrderItem(itemId: Int, orderId: Int, skuId: Int, skuName: String, skuNum: Long)

  case class OrderDetail(var orderId: Int = 0, var userId: Int = 0, var itemId: Int = 0, var skuId: Int = 0, var skuName: String = "", var skuNum: Long = 0L) {
    def buildOrder(order: Order): OrderDetail = {
      if (order != null) {
        this.orderId = order.orderId
        this.userId = order.userId
      }
      this
    }


    def buildItem(orderItem: OrderItem): OrderDetail = {
      if (null != orderItem) {
        this.itemId = orderItem.itemId
        this.skuId = orderItem.skuId
        this.skuName = orderItem.skuName
        this.skuNum = orderItem.skuNum
      }
      this
    }
  }

}
