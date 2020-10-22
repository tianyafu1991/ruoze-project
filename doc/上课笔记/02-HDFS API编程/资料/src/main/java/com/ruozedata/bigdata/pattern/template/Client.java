package com.ruozedata.bigdata.pattern.template;

/**
 * @author PK哥
 **/
public class Client {

    public static void main(String[] args) {

        Mapper pkMapper = new PKMapper();
        pkMapper.run();

        System.out.println("-------------");

        Mapper ruozedataMapper = new RuozedataMapper();
        ruozedataMapper.run();
    }
}
