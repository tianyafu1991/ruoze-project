package com.ruozedata.bigdata.junit;

import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author PK哥
 **/
public class CalculatorTest03 {

    static Calculator calculator;


    @BeforeClass
    public static void setUp(){
        calculator = new Calculator();
        System.out.println("------setUp-----");
    }

    @AfterClass
    public static void tearDown(){
        calculator = null;
        System.out.println("------tearDown-----");
    }


    @Test
    public void testAdd() {
        int result = calculator.add(2, 5);
        assertEquals(7, result);  // 方法是ok
        System.out.println("------testAdd-----");
    }

    @Test
    public void testDivide(){
        int result = calculator.divide(9, 3);
        assertEquals(3, result);
        System.out.println("------testDivide-----");
    }

    @Test(expected = ArithmeticException.class)
    @Ignore
    public void testDivide2(){
        int result = calculator.divide(9, 0);
        assertEquals(3, result);
        System.out.println("------testDivide2-----");
    }
}
