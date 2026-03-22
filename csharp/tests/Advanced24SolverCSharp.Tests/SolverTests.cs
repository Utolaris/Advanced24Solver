using Advanced24SolverCSharp;

namespace Advanced24SolverCSharp.Tests;

public sealed class SolverTests
{
    [Fact]
    public void SolvesBasicHand()
    {
        Assert.NotNull(Solver.SolveCards([1, 1, 7, 7]));
    }

    [Fact]
    public void SolvesHighCards()
    {
        Assert.NotNull(Solver.SolveCards([10, 11, 12, 13]));
    }

    [Fact]
    public void ToleranceConstantMatchesSpec()
    {
        Assert.Equal(1e-6, Solver.Eps, 12);
    }
}
