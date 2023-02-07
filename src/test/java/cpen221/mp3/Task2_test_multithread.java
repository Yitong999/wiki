package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class Task2_test_multithread {
    private static FSFTBuffer<BufferableInteger> intBuffer;

    @BeforeAll
    public static void setupTests(){
        intBuffer = new FSFTBuffer<>(16, 5);
    }


    @Test
    public void putAndGetTest() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 16; i++) {
                    BufferableInteger val = new BufferableInteger(i, Character.toString('a' + i));
                    Assertions.assertTrue(intBuffer.put(val));
                }
            }

        });
        TimeUnit.SECONDS.sleep(2);
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 16; i++) {
                    BufferableInteger val = new BufferableInteger(i, Character.toString('a' + i));
                    Assertions.assertTrue(intBuffer.put(val));
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        BufferableInteger expect = new BufferableInteger(3, Character.toString('a' + 3));
        Assertions.assertEquals(expect, intBuffer.get(Character.toString('a' + 3)));
    }



    //why should this fail
    @Test
    public void touchTest() throws InterruptedException {
        BufferableInteger val;
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferableInteger val = new BufferableInteger(3, Character.toString('a' + 3));
                intBuffer.put(val);
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                intBuffer.touch(Character.toString('a' + 3));
            }
        });

        t1.start();
        TimeUnit.SECONDS.sleep(2);
        t2.start();
        t1.join();
        t2.join();
        TimeUnit.SECONDS.sleep(5);

        BufferableInteger expect = new BufferableInteger(3, Character.toString('a' + 3));
        Assertions.assertEquals(expect, intBuffer.get(Character.toString('a' + 3)));
    }

}
