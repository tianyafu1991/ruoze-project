package com.ruoze.spark.streaming.utils


import java.util.Properties

import com.sun.mail.util.MailSSLSocketFactory
import javax.mail._
import javax.mail.internet.{InternetAddress, MimeMessage}


/**
 * 邮件发送工具类
 */
object MsgUtils {

  def send(recivers:String,title:String,context:String): Unit ={

    val properties = new Properties()

    properties.setProperty("mail.host","smtp.qq.com") //qq 服务器
    properties.setProperty("mail.transport.protocol","smtp") //邮件发送协议
    properties.setProperty("mail.smtp.auth","true")//验证用户名密码

    val factory = new MailSSLSocketFactory()
    factory.setTrustAllHosts(true)

    properties.setProperty("mail.smtp.ssl.enable","true")
    properties.put("mail.smtp.ssl.socketFactory",factory)

    //发邮件，是从哪个邮箱发的
    val authenticator: Authenticator = new Authenticator {
      override def getPasswordAuthentication: PasswordAuthentication = {
        val username: String = "1193835093@qq.com"
        val password = "fhpcfxaqmufqgbac"
        new PasswordAuthentication(username, password)
      }
    }

    val session: Session = Session.getInstance(properties, authenticator)


    val message: MimeMessage = new MimeMessage(session)

    val from: InternetAddress = new InternetAddress("1193835093@qq.com")
    message.setFrom(from)

    val tos: Array[Address] = InternetAddress.parse(recivers).toArray[Address]

    message.setRecipients(Message.RecipientType.TO,tos)

    message.setSubject(title)
    message.setContent(context,"text/html;charset=UTF-8")
    Transport.send(message)

  }

  def main(args: Array[String]): Unit = {
    send("1045159389@qq.com","这个是一个测试邮件","测试邮件是否配置成功")
  }

}
