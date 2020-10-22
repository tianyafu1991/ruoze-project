package com.ruozedata.bigdata.pattern.template;

/**
 * @author PK哥
 **/
public abstract class Mapper {

    /**
     * 初始化方法
     */
    abstract void setUp();

    /**
     * 资源释放方法
     */
    abstract void cleanup();

    /**
     * 业务逻辑方法
     */
    abstract void map();

    /**
     * 定义了具体执行流程
     */
    public void run(){
        setUp();
        map();
        cleanup();
    }

}
