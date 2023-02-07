package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Task5Tests {

    private static WikiMediator mediator;

    @BeforeAll
    public static void setupTests(){
        mediator = new WikiMediator(5,5);
    }

    @Test
    public void pathToSelf() throws TimeoutException {
        ArrayList<String> expected = new ArrayList<>(List.of("scone"));
        Assertions.assertEquals(expected, mediator.shortestPath("scone", "scone", 5));
    }

    @Test //Page 1 does not exist
    public void pathFromBadPage() throws TimeoutException {
        ArrayList<String> expected = new ArrayList<>();
        Assertions.assertEquals(expected, mediator.shortestPath("xy32k3j9", "scone", 5));
    }

    @Test //1908 Summer is first result of pages related to England
    public void oneStepFirstResult() throws TimeoutException {
        ArrayList<String> expected = new ArrayList<>(List.of("England", "1908 Summer Olympics"));
        Assertions.assertEquals(expected, mediator.shortestPath("England", "1908 Summer Olympics", 5));
    }

    @Test  //Scones if further down the list of England related pages
    public void oneStep() throws TimeoutException {
        ArrayList<String> expected = new ArrayList<>(List.of("England", "Scones"));
        Assertions.assertEquals(expected, mediator.shortestPath("England", "Scones", 5));
    }

    @Test
    public void twoStepLowLex() throws TimeoutException { //3.5 seconds
        ArrayList<String> expected = new ArrayList<>(List.of("Philosophy", "Academic bias", "Barack Obama"));
        Assertions.assertEquals(expected, mediator.shortestPath("Philosophy", "Barack Obama", 5));
    }

    @Test
    public void twoSteps1() throws TimeoutException { //2.3 sec
        ArrayList<String> expected = new ArrayList<>(List.of("Albus Dumbledore", "Alchemy", "Unicorn"));
        Assertions.assertEquals(expected, mediator.shortestPath("Albus Dumbledore", "Unicorn", 5));
    }

    @Test
    public void timeout() throws TimeoutException { //39 sec
        ArrayList<String> expected = new ArrayList<>(List.of("Scone", "Jane Grigson", "Avocado"));
        Assertions.assertThrows(TimeoutException.class, () -> {mediator.shortestPath("Scone", "Avocado", 20);});
    }

    @Test
    public void timeOutZero() throws TimeoutException {
        Assertions.assertThrows(TimeoutException.class, () -> {mediator.shortestPath("scone", "scone", 0);});
    }
}
