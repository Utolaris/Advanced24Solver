namespace Advanced24SolverCSharp;

public static class Solver
{
    public const double Target = 24.0;
    public const double Eps = 1e-6;

    private const double DivEps = 1e-12;
    private const double ValueBound = 1_000_000.0;
    private const int MaxFactorial = 10;
    private const byte MaxUnaryDepth = 2;
    private const double ExponentBound = 10.0;

    public static string? SolveCards(byte[] numbers)
    {
        var states = numbers
            .Select(number => new State(number, number.ToString(), 0))
            .ToList();

        return Dfs(states, new HashSet<string>(StringComparer.Ordinal));
    }

    private static string? Dfs(List<State> states, HashSet<string> visited)
    {
        var key = CanonicalKey(states);
        if (!visited.Add(key))
        {
            return null;
        }

        if (states.Count == 1)
        {
            var state = states[0];
            return Close(state.Value, Target) ? state.Expression : null;
        }

        for (var leftIndex = 0; leftIndex < states.Count; leftIndex++)
        {
            for (var rightIndex = leftIndex + 1; rightIndex < states.Count; rightIndex++)
            {
                var remaining = states
                    .Where((_, index) => index != leftIndex && index != rightIndex)
                    .ToList();

                var leftVariants = ExpandUnary(states[leftIndex]);
                var rightVariants = ExpandUnary(states[rightIndex]);

                foreach (var left in leftVariants)
                {
                    foreach (var right in rightVariants)
                    {
                        foreach (var combined in CombineStates(left, right))
                        {
                            foreach (var variant in ExpandUnary(combined))
                            {
                                var nextStates = new List<State>(remaining.Count + 1);
                                nextStates.AddRange(remaining);
                                nextStates.Add(variant);

                                var answer = Dfs(nextStates, visited);
                                if (answer is not null)
                                {
                                    return answer;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private static string CanonicalKey(IReadOnlyList<State> states)
    {
        var buckets = states
            .Select(state => Bucket(state.Value))
            .Order()
            .ToArray();

        return string.Join('|', buckets);
    }

    private static List<State> CombineStates(State left, State right)
    {
        var results = new List<State>(10);

        PushUnique(results, left.Value + right.Value, $"({left.Expression} + {right.Expression})");
        PushUnique(results, left.Value - right.Value, $"({left.Expression} - {right.Expression})");
        PushUnique(results, right.Value - left.Value, $"({right.Expression} - {left.Expression})");
        PushUnique(results, left.Value * right.Value, $"({left.Expression} * {right.Expression})");

        if (Math.Abs(right.Value) > DivEps)
        {
            PushUnique(results, left.Value / right.Value, $"({left.Expression} / {right.Expression})");
        }

        if (Math.Abs(left.Value) > DivEps)
        {
            PushUnique(results, right.Value / left.Value, $"({right.Expression} / {left.Expression})");
        }

        if (ValidPower(left.Value, right.Value))
        {
            PushUnique(results, Math.Pow(left.Value, right.Value), $"({left.Expression} ^ {right.Expression})");
        }

        if (ValidPower(right.Value, left.Value))
        {
            PushUnique(results, Math.Pow(right.Value, left.Value), $"({right.Expression} ^ {left.Expression})");
        }

        if (ValidLog(left.Value, right.Value))
        {
            PushUnique(
                results,
                Math.Log(left.Value) / Math.Log(right.Value),
                $"log_{right.Expression}({left.Expression})");
        }

        if (ValidLog(right.Value, left.Value))
        {
            PushUnique(
                results,
                Math.Log(right.Value) / Math.Log(left.Value),
                $"log_{left.Expression}({right.Expression})");
        }

        return results;
    }

    private static List<State> ExpandUnary(State state)
    {
        var states = new List<State> { state };
        if (state.UnaryDepth >= MaxUnaryDepth)
        {
            return states;
        }

        if (state.Value >= -Eps)
        {
            var result = state.Value > 0.0 ? Math.Sqrt(state.Value) : 0.0;
            PushUniqueWithDepth(
                states,
                result,
                $"sqrt({state.Expression})",
                (byte)(state.UnaryDepth + 1));
        }

        var integer = ApproxInteger(state.Value);
        if (integer is not null && integer <= MaxFactorial)
        {
            PushUniqueWithDepth(
                states,
                Factorial(integer.Value),
                $"({state.Expression})!",
                (byte)(state.UnaryDepth + 1));
        }

        return states;
    }

    private static void PushUnique(List<State> states, double value, string expression) =>
        PushUniqueWithDepth(states, value, expression, 0);

    private static void PushUniqueWithDepth(
        List<State> states,
        double value,
        string expression,
        byte unaryDepth)
    {
        if (!ValidNumber(value) || states.Any(state => Close(state.Value, value)))
        {
            return;
        }

        states.Add(new State(value, expression, unaryDepth));
    }

    private static bool ValidNumber(double value) =>
        double.IsFinite(value) && Math.Abs(value) <= ValueBound;

    private static bool ValidPower(double @base, double exponent)
    {
        if (Math.Abs(exponent) > ExponentBound)
        {
            return false;
        }

        if (@base < 0.0 && ApproxInteger(exponent) is null)
        {
            return false;
        }

        return ValidNumber(Math.Pow(@base, exponent));
    }

    private static bool ValidLog(double argument, double @base) =>
        argument > 0.0 && @base > 0.0 && !Close(@base, 1.0);

    private static int? ApproxInteger(double value)
    {
        if (value < -Eps)
        {
            return null;
        }

        var rounded = Math.Round(value);
        return Close(value, rounded) && rounded >= 0.0
            ? (int)rounded
            : null;
    }

    private static long Factorial(int value)
    {
        var result = 1L;
        for (var item = 2; item <= value; item++)
        {
            result *= item;
        }

        return result;
    }

    private static long Bucket(double value) =>
        value >= 0.0
            ? (long)(value * 1_000_000.0 + 0.5)
            : (long)(value * 1_000_000.0 - 0.5);

    private static bool Close(double left, double right) =>
        Math.Abs(left - right) <= Eps;

    private sealed record State(double Value, string Expression, byte UnaryDepth);
}
