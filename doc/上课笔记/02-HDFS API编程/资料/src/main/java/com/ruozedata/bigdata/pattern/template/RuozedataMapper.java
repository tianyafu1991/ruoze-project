package com.ruozedata.bigdata.pattern.template;

/**
 * @author PK哥
 **/
public class RuozedataMapper extends Mapper{

    @Override
    void setUp() {
        System.out.println("------RuozedataMapper setUp-----");
    }

    @Override
    void cleanup() {
        System.out.println("------RuozedataMapper cleanup-----");
    }

    @Override
    void map() {
        System.out.println("------RuozedataMapper map-----");
    }
}
