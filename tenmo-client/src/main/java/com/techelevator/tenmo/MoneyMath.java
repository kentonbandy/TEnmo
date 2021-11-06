package com.techelevator.tenmo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyMath {

    /**
     * @param num1
     * @param num2
     * @return adds the two given numbers, returns with two decimal places
     */
    public static String add(String num1, String num2) {
        BigDecimal a = new BigDecimal(num1);
        BigDecimal b = new BigDecimal(num2);
        BigDecimal sum = a.add(b).setScale(2, RoundingMode.HALF_EVEN);
        return sum.toString();
    }

    /**
     * @param num1
     * @param num2
     * @return subtracts num2 from num1, returns with two decimal places
     */
    public static String subtract(String num1, String num2) {
        BigDecimal a = new BigDecimal(num1);
        BigDecimal b = new BigDecimal(num2);
        BigDecimal sum = a.subtract(b).setScale(2, RoundingMode.HALF_EVEN);
        return sum.toString();
    }

    /**
     *
     * @param num
     * @return
     */
    public static String format(String num) {
        return (new BigDecimal(num).setScale(2, RoundingMode.HALF_EVEN)).toString();
    }
}
