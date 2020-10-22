package com.ruozedata.bigdata.hdfs.wc;

/**
 * @author PK哥
 **/
public class CaseIgnoreWordCountMapper implements RuozedataMapper{

    @Override
    public void map(String line, RuozedataContext context) {
        // ruoze,ruoze,ruoze
        String[] splits = line.toLowerCase().split(",");
        for(String word : splits) {
            Object value = context.get(word);
            if(null == value) {  // 单词是第一次出现
                context.write(word, 1);
            } else {  // 该单词已经出现过了
                context.write(word, Integer.parseInt(value.toString()) + 1);
            }
        }

    }
}
