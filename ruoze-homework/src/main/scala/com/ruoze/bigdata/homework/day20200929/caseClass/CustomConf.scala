package com.ruoze.bigdata.homework.day20200929.caseClass

case class CustomConf(confName:String
                      ,domain:String
                      ,compressCodec:String
                      ,fileNamePrefix:String
                      ,fileNameTimeFormat:String
                      ,fileNameSuffix:String
                      ,logContentFields:String
                      ,fieldsDelimiter:String){
  override def toString: String = f"${confName}\t${domain}\t${compressCodec}\t${fileNamePrefix}\t${fileNameTimeFormat}\t${fileNameSuffix}\t${logContentFields}\t${fieldsDelimiter}"
}
