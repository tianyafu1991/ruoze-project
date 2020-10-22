package com.ruozedata.bigdata.pattern.template;

/**
 * @author PK哥
 **/
public class PKMapper extends Mapper{

    @Override
    void setUp() {
        System.out.println("------PKMapper setUp-----");
    }

    @Override
    void cleanup() {
        System.out.println("------PKMapper cleanup-----");
    }

    @Override
    void map() {
        System.out.println("------PKMapper map-----");
    }
}
