package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SolverTest {
    @Test
    public void solvesBasicHand() {
        String solution = Solver.solveCards(new int[]{1, 1, 7, 7});
        assertTrue(solution != null);
    }

    @Test
    public void solvesHighCards() {
        String solution = Solver.solveCards(new int[]{10, 11, 12, 13});
        assertTrue(solution != null);
    }

    @Test
    public void toleranceConstantMatchesSpec() {
        assertTrue(Math.abs(Solver.EPS - 1e-6) < 1e-15);
    }
}
