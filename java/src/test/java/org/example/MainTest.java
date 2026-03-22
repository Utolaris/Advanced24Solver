package org.example;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {
    @Test
    public void generatesExpectedCombinationCount() {
        List<int[]> combinations = Main.allCombinations();
        assertEquals(1820, combinations.size());
    }

    @Test
    public void formatsCardsKey() {
        assertEquals("1,1,7,7", Main.cardsKey(new int[]{1, 1, 7, 7}));
    }
}
