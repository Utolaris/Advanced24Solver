using Advanced24SolverCSharp;

namespace Advanced24SolverCSharp.Tests;

public sealed class ProgramTests
{
    [Fact]
    public void GeneratesExpectedCombinationCount()
    {
        Assert.Equal(1820, Program.AllCombinations().Count);
    }

    [Fact]
    public void FormatsCardsKey()
    {
        Assert.Equal("1,1,7,7", Program.CardsKey([1, 1, 7, 7]));
    }

    [Fact]
    public void WorkerCountMatchesRequirement()
    {
        Assert.Equal(8, Program.WorkerCount);
    }

    [Fact]
    public void InvalidEnvKeepsDefaultWorkerCount()
    {
        var previousValue = Environment.GetEnvironmentVariable("A24_THREADS");

        try
        {
            Environment.SetEnvironmentVariable("A24_THREADS", "0");
            Assert.Equal(Program.WorkerCount, Program.ConfiguredWorkerCount());
        }
        finally
        {
            Environment.SetEnvironmentVariable("A24_THREADS", previousValue);
        }
    }
}
