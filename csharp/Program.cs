using System.Diagnostics;
using System.Text.Encodings.Web;
using System.Text.Json;

namespace Advanced24SolverCSharp;

public static class Program
{
    public const int WorkerCount = 8;

    public static void Main(string[] args)
    {
        var stopwatch = Stopwatch.StartNew();
        var combinations = AllCombinations();
        var workerCount = ConfiguredWorkerCount();

        var records = combinations
            .AsParallel()
            .WithDegreeOfParallelism(workerCount)
            .Select(cards =>
            {
                var expression = Solver.SolveCards(cards) ?? "无解";
                return new Record(
                    cards.Select(static card => (int)card).ToArray(),
                    CardsKey(cards),
                    expression != "无解",
                    expression);
            })
            .ToList();

        records.Sort(CompareRecords);

        var payload = new Payload(
            new Metadata(24, Solver.Eps, workerCount, records.Count),
            records);

        var outputPath = Path.Combine(ProjectRoot(), "results.json");
        var json = JsonSerializer.Serialize(
            payload,
            new JsonSerializerOptions
            {
                Encoder = JavaScriptEncoder.UnsafeRelaxedJsonEscaping,
                PropertyNamingPolicy = JsonNamingPolicy.SnakeCaseLower,
                WriteIndented = true
            });

        File.WriteAllText(outputPath, json);

        Console.WriteLine(
            "完成 {0} 种组合计算，结果已写入 {1}",
            combinations.Count,
            Path.GetFileName(outputPath));
        if (Environment.GetEnvironmentVariable("A24_SUPPRESS_INTERNAL_TIMING") != "1")
        {
            Console.WriteLine("总耗时: {0:F6} 秒", stopwatch.Elapsed.TotalSeconds);
        }
    }

    public static int ConfiguredWorkerCount()
    {
        var rawValue = Environment.GetEnvironmentVariable("A24_THREADS");
        return int.TryParse(rawValue, out var workerCount) && workerCount > 0
            ? workerCount
            : WorkerCount;
    }

    public static List<byte[]> AllCombinations()
    {
        var combinations = new List<byte[]>(1820);
        for (byte first = 1; first <= 13; first++)
        {
            for (byte second = first; second <= 13; second++)
            {
                for (byte third = second; third <= 13; third++)
                {
                    for (byte fourth = third; fourth <= 13; fourth++)
                    {
                        combinations.Add([first, second, third, fourth]);
                    }
                }
            }
        }

        return combinations;
    }

    public static string CardsKey(IReadOnlyList<byte> cards) => string.Join(',', cards);

    private static int CompareRecords(Record left, Record right)
    {
        for (var index = 0; index < left.Cards.Length; index++)
        {
            var comparison = left.Cards[index].CompareTo(right.Cards[index]);
            if (comparison != 0)
            {
                return comparison;
            }
        }

        return 0;
    }

    private static string ProjectRoot()
    {
        var directory = new DirectoryInfo(AppContext.BaseDirectory);
        while (directory is not null)
        {
            if (File.Exists(Path.Combine(directory.FullName, "Advanced24SolverCSharp.csproj")))
            {
                return directory.FullName;
            }

            directory = directory.Parent;
        }

        return Directory.GetCurrentDirectory();
    }
}

public sealed record Metadata(int Target, double Tolerance, int WorkerCount, int CombinationCount);

public sealed record Record(int[] Cards, string CardsKey, bool Solved, string Expression);

public sealed record Payload(Metadata Metadata, List<Record> Results);
