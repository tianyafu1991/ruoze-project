package com.ruozedata.bigdata.junit;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * @author PK哥
 **/
public class CalculatorTest {




    @Test
    public void testAdd() {
        Calculator calculator = new Calculator();
        int result = calculator.add(2, 5);
        assertEquals(7, result);  // 方法是ok
    }

    @Test
    public void testDivide(){
        Calculator calculator = new Calculator();
        int result = calculator.divide(9, 3);
        assertEquals(3, result);
    }

    @Test(expected = ArithmeticException.class)
//    @Ignore
    public void testDivide2(){
        Calculator calculator = new Calculator();
        int result = calculator.divide(9, 0);
        assertEquals(3, result);
    }
}
