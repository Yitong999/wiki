package cpen221.mp3;

import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class Task1Test {

    private static FSFTBuffer<BufferableInteger> intBuffer;
    private static FSFTBuffer<BufferableInteger> smallBuffer;

    @BeforeAll
    public static void setupTests(){
        intBuffer = new FSFTBuffer<>(16, 5);
        smallBuffer = new FSFTBuffer<>(3, 5);
    }

    /* Task 1 Tests */

    @Test
    public void simplePutTest(){
        BufferableInteger val = new BufferableInteger(2, "a");
        Assertions.assertTrue(intBuffer.put(val));
    }

    @Test
    public void putToCapacityTest() {
        BufferableInteger val;
        for (int i = 0; i < 16; i++) {
            val = new BufferableInteger(i, Character.toString('a' + i));
            Assertions.assertTrue(intBuffer.put(val));
        }
    }

    @Test
    public void putSucessWithRemoveTest() throws InterruptedException {
        /* this test covers with remove and also some element disappearing */
        BufferableInteger val;
        for (int i = 0; i < 16; i++) {
            val = new BufferableInteger(i, Character.toString('a' + i));
            Assertions.assertTrue(intBuffer.put(val));
        }
        TimeUnit.SECONDS.sleep(2);
        val = new BufferableInteger(17, "new");
        Assertions.assertTrue(intBuffer.put(val));
    }

    //why should this fail
    @Test
    public void putFailedNormalUpdateTest() throws InterruptedException {
        BufferableInteger val;
        for (int i = 0; i < 16; i++) {
            val = new BufferableInteger(i, Character.toString('a' + i));
            Assertions.assertTrue(intBuffer.put(val));
        }
        val = new BufferableInteger(17, "new");
        Assertions.assertTrue(intBuffer.put(val));
    }

    @Test
    public void putSuccessWithTwoValRemoved() throws InterruptedException {
        BufferableInteger val;
        intBuffer.put(new BufferableInteger(9, "st"));
        for (int i = 0; i < 14; i++) {
            val = new BufferableInteger(i, Character.toString('a' + i));
            Assertions.assertTrue(intBuffer.put(val));
            intBuffer.touch(Character.toString('a' + i));
            intBuffer.touch(Character.toString('a' + i));
        }
        intBuffer.put(new BufferableInteger(19, "hello"));
        TimeUnit.SECONDS.sleep(2);
        Assertions.assertTrue(intBuffer.put(new BufferableInteger(17, "new")));
    }

    //at max capacity and no objects have expired - remove first accessed
    @Test
    public void putMaxCapNoExpire() throws InterruptedException {
        BufferableInteger val;
        intBuffer.put(new BufferableInteger(9, "st"));
        for (int i = 0; i < 16; i++) {
            val = new BufferableInteger(i, Character.toString('a' + i));
            Assertions.assertTrue(intBuffer.put(val));
        }
        TimeUnit.SECONDS.sleep(2);
        Assertions.assertTrue(intBuffer.put(new BufferableInteger(17, "new")));
    }

    @Test
    public void putNull() throws InterruptedException {
        Assertions.assertFalse(intBuffer.put(null));
    }

    @Test
    public void putExpired() throws InterruptedException {
        BufferableInteger test = new BufferableInteger(5, "a");
        Assertions.assertTrue(intBuffer.put(test));
        TimeUnit.SECONDS.sleep(6);
        Assertions.assertTrue(intBuffer.put(test));
        Assertions.assertEquals(intBuffer.get("a"), test);

    }

    /* update and touch successful is already tested, so update and touch testing will only test failed cases */
    @Test
    public void testUpdateTouchNothing(){
        BufferableInteger val = new BufferableInteger(1, "a");
        Assertions.assertFalse(intBuffer.update(val));
        Assertions.assertFalse(intBuffer.touch("a"));
    }

    //objects should still be in list at t = timeout
    @Test
    public void updateTouchTimeBoundary() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(1, "a");
        intBuffer.put(val);
        TimeUnit.SECONDS.sleep(5);
        Assertions.assertTrue(intBuffer.update(val));
        Assertions.assertTrue(intBuffer.touch("a"));
    }

    @Test
    public void updateTouchExpiredElementTest() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(1, "a");
        intBuffer.put(val);
        TimeUnit.SECONDS.sleep(6);
        Assertions.assertFalse(intBuffer.update(val));
        Assertions.assertFalse(intBuffer.touch("a"));
    }

    @Test
    public void touchNonexistentElement() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(1, "a");
        BufferableInteger val2 = new BufferableInteger(2, "b");
        intBuffer.put(val);
        TimeUnit.SECONDS.sleep(5);
        Assertions.assertFalse(intBuffer.update(val2));
        Assertions.assertFalse(intBuffer.touch("b"));
    }

    /* get test */
    @Test
    public void testSuccessGet() throws AssertionFailedError {
        BufferableInteger val = new BufferableInteger(0, "0");
        intBuffer.put(val);
        try{
            Assertions.assertEquals(intBuffer.get("0"), val);
        } catch(NoSuchElementException nsee){
            throw new AssertionFailedError();
        }
    }

    @Test
    public void getNotExist(){
        BufferableInteger val = new BufferableInteger(0, "0");
        intBuffer.put(val);
        Assertions.assertThrows(NoSuchElementException.class, () -> {intBuffer.get("1");});
    }

    @Test
    public void getUntilExpire() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(0, "0");
        intBuffer.put(val);
        try{ //get object will not affect expiry time
            Assertions.assertEquals(intBuffer.get("0"), val);
            TimeUnit.SECONDS.sleep(2);
            Assertions.assertEquals(intBuffer.get("0"), val);
            TimeUnit.SECONDS.sleep(4);
        } catch(Exception e){
            throw e;
        }
        //object should have timed out after 6 seconds
        Assertions.assertThrows(NoSuchElementException.class, () -> {intBuffer.get("0");});
    }

    @Test
    public void priorityUpdateAfterGet() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(0, "0");
        BufferableInteger val1 = new BufferableInteger(1, "1");
        BufferableInteger val2 = new BufferableInteger(2, "2");
        BufferableInteger val3 = new BufferableInteger(3, "3");
        smallBuffer.put(val); //since val was put in first, it was accessed earliest
        smallBuffer.put(val1);
        smallBuffer.put(val2);
        try{ //but "get" will make val most recently accessed
            Assertions.assertEquals(smallBuffer.get("0"), val);
        } catch(NoSuchElementException e){
            throw new NoSuchElementException();
        }

        smallBuffer.put(val3);

        try{ //val should still be in cache, val1 should be removed after new object put in
            Assertions.assertEquals(smallBuffer.get("0"), val);
        } catch(NoSuchElementException e){
            throw new NoSuchElementException();
        }
        //object should have timed out after 6 seconds
        Assertions.assertThrows(NoSuchElementException.class, () -> {smallBuffer.get("1");});
    }



    @Test
    public void getFromEmptyBuffer(){
        Assertions.assertThrows(NoSuchElementException.class, () -> {intBuffer.get("0");});
    }

    @Test
    public void testGetExpired() throws InterruptedException{
        BufferableInteger val = new BufferableInteger(0, "0");
        intBuffer.put(val);
        TimeUnit.SECONDS.sleep(6);
        Assertions.assertThrows(NoSuchElementException.class, () -> {intBuffer.get("0");});
    }
}