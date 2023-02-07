package cpen221.mp3;

import org.fastily.jwiki.core.Wiki;


import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;


import java.util.*;
import java.util.concurrent.TimeUnit;


public class Task3Test {
    private static WikiMediator test;
    private static WikiMediator test1;

    @Test
    public void task3_1() {
        test = new WikiMediator(5, 3);
        List<String> titles = new ArrayList<>(test.search("Barack Obama", 6));
        Assertions.assertEquals(6, titles.size());
    }

    @Test
    public void task3_2() {
        test = new WikiMediator(5, 3);
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        String text = test.getPage("Barack Obama in comics");
        String text_expect = wiki.getPageText("Barack Obama in comics");

        Assertions.assertEquals(text_expect, text);
    }

    @Test
    public void task3_3() {
        test = new WikiMediator(5, 3);
        List<String> titles = test.search("Barack Obama", 5);
        titles = test.search("Barack Obama", 5);
        String text = test.getPage("Barack Obama in comics");
        List<String> mostCommon = test.zeitgeist(3);
        List<String> expect = Arrays.asList("Barack Obama", "Barack Obama in comics");
        Assertions.assertEquals(expect, mostCommon);
    }

    @Test
    public void task3_4_empty() throws InterruptedException {
        test = new WikiMediator(5, 3);
        TimeUnit.SECONDS.sleep(6);
        List<String> mostCommon = test.trending(2, 2);
        Assertions.assertEquals(0, mostCommon.size());
    }

    @Test
    public void task3_4_normal_0() throws InterruptedException {
        test = new WikiMediator(5, 3);
        List<String> titles = test.search("Barack Obama", 5);
        String text = test.getPage("Barack Obama in comics");
        TimeUnit.SECONDS.sleep(6);
        List<String> mostCommon = test.trending(2, 2);
        Assertions.assertEquals(0, mostCommon.size());
    }

    @Test
    public void task3_4_normal_1() throws InterruptedException {
        test = new WikiMediator(5, 3);
        List<String> titles = test.search("Barack Obama", 5);
        String text = test.getPage("Barack Obama in comics");
        TimeUnit.SECONDS.sleep(6);
        List<String> mostCommon = test.trending(10, 2);
        Assertions.assertEquals(2, mostCommon.size());
    }

    @Test
    public void task3_4_normal_2() throws InterruptedException {
        test = new WikiMediator(5, 3);
        List<String> titles = test.search("Barack Obama", 5);
        TimeUnit.SECONDS.sleep(1);
        String text = test.getPage("Barack Obama in comics");
        List<String> person = test.search("Chow Yun-fat", 5);
        TimeUnit.SECONDS.sleep(6);
        List<String> mostCommon = test.trending(10, 2);
        //List<String> expect = Arrays.asList("Barack Obama", "Barack Obama in comics");
        Assertions.assertEquals(2, test.trending(10, 2).size());
    }


    @Test
    public void task3_5() {
        test = new WikiMediator(5, 3);
        String text = test.getPage("Barack Obama in comics");
        List<String> a = test.zeitgeist(2);
        a = test.trending(2, 2);
        int b = test.windowedPeakLoad();

        Assertions.assertEquals(5, test.windowedPeakLoad(2));
    }

    @Test
    public void task3_6() {
        test = new WikiMediator(5, 3);
        String text = test.getPage("Barack Obama in comics");
        List<String> a = test.zeitgeist(2);
        a = test.trending(2, 2);
        int b = test.windowedPeakLoad(1);

        Assertions.assertEquals(5, test.windowedPeakLoad());
    }

    //overall test
    @Test
    public void task3_overall() throws InterruptedException {
        test = new WikiMediator(5, 3);
        boolean pass = true;
        List<String> titles = test.search("Barack Obama", 3);
        String text = test.getPage("Barack Obama in comics");
        TimeUnit.SECONDS.sleep(1);
        List<String> a = test.zeitgeist(2);
        if (!(a.get(0).equals("Barack Obama") && a.get(1).equals("Barack Obama in comics"))) {
            pass = false;
            System.out.println(1);
        }

        TimeUnit.SECONDS.sleep(6);
        List<String> b = test.trending(2, 3);
        if (b.size() != 0) {
            pass = false;
            System.out.println(2);
        }

        int count = test.windowedPeakLoad(3);
        if (count != 3) {
            System.out.println(count);
            pass = false;
            System.out.println(3);
        }

        count = test.windowedPeakLoad();
        if (count != 6) {
            pass = false;
            System.out.println(4);
        }

        Assertions.assertTrue(pass);
        test.storeState();

        test1 = new WikiMediator(8, 5); //new wikimediator should add to old data
        List<String> titles2 = new ArrayList<>(test1.search("Mouse", 6));
        test1.storeState(); //check lastState.txt for accuracy
    }
}
