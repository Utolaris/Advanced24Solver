package org.example;

import java.util.*;

/**
 * 24 点核心求解器，支持高级数学运算
 */
public class Solver {
    public static final double TARGET = 24.0;
    public static final double EPS = 1e-6;
    private static final double DIV_EPS = 1e-12;
    private static final double VALUE_BOUND = 1_000_000.0;
    private static final int MAX_FACTORIAL = 10;
    private static final int MAX_UNARY_DEPTH = 2;
    private static final double EXPONENT_BOUND = 10.0;

    /**
     * 解决特定的数字组合
     * @param numbers 4个数字的数组
     * @return 若能得到靶值结果则返回表达式字符串，否则返回 null
     */
    public static String solveCards(int[] numbers) {
        List<State> initialStates = new ArrayList<>();
        for (int number : numbers) {
            initialStates.add(new State(number, String.valueOf(number), 0));
        }
        return dfs(initialStates, new HashSet<>());
    }

    /**
     * 深度优先搜索，探索所有可能的运算路径
     * @param states 当前状态列表
     * @param visited 已访问的状态集合，用于剪枝
     */
    private static String dfs(List<State> states, Set<List<Long>> visited) {
        List<Long> key = canonicalKey(states);
        // NOTE: 如果该状态组合已访问过，说明处于重复计算路径，直接修剪
        if (!visited.add(key)) {
            return null;
        }

        // 仅剩一个数字时，判断是否接近目标值
        if (states.size() == 1) {
            State state = states.get(0);
            if (close(state.value(), TARGET)) {
                return state.expr();
            }
            return null;
        }

        // 任取两个数字进行组合
        for (int i = 0; i < states.size(); i++) {
            for (int j = i + 1; j < states.size(); j++) {
                List<State> remaining = new ArrayList<>(states.size() - 2);
                for (int k = 0; k < states.size(); k++) {
                    if (k != i && k != j) {
                        remaining.add(states.get(k));
                    }
                }

                List<State> leftVariants = expandUnary(states.get(i));
                List<State> rightVariants = expandUnary(states.get(j));

                // 尝试所有可能的单目与双目运算组合
                for (State left : leftVariants) {
                    for (State right : rightVariants) {
                        List<State> combinedStates = combineStates(left, right);
                        for (State combined : combinedStates) {
                            List<State> variants = expandUnary(combined);
                            for (State variant : variants) {
                                List<State> nextStates = new ArrayList<>(remaining);
                                nextStates.add(variant);
                                String answer = dfs(nextStates, visited);
                                if (answer != null) {
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

    /**
     * 计算状态数组的规范化表示，用作哈希化键
     */
    private static List<Long> canonicalKey(List<State> states) {
        List<Long> buckets = new ArrayList<>(states.size());
        for (State state : states) {
            buckets.add(bucket(state.value()));
        }
        Collections.sort(buckets);
        return buckets;
    }

    /**
     * 对左右两个状态进行所有合法的双目运算
     */
    private static List<State> combineStates(State left, State right) {
        List<State> results = new ArrayList<>();

        pushUnique(results, left.value() + right.value(), "(" + left.expr() + " + " + right.expr() + ")");
        pushUnique(results, left.value() - right.value(), "(" + left.expr() + " - " + right.expr() + ")");
        pushUnique(results, right.value() - left.value(), "(" + right.expr() + " - " + left.expr() + ")");
        pushUnique(results, left.value() * right.value(), "(" + left.expr() + " * " + right.expr() + ")");

        if (Math.abs(right.value()) > DIV_EPS) {
            pushUnique(results, left.value() / right.value(), "(" + left.expr() + " / " + right.expr() + ")");
        }
        if (Math.abs(left.value()) > DIV_EPS) {
            pushUnique(results, right.value() / left.value(), "(" + right.expr() + " / " + left.expr() + ")");
        }

        if (validPower(left.value(), right.value())) {
            pushUnique(results, Math.pow(left.value(), right.value()), "(" + left.expr() + " ^ " + right.expr() + ")");
        }
        if (validPower(right.value(), left.value())) {
            pushUnique(results, Math.pow(right.value(), left.value()), "(" + right.expr() + " ^ " + left.expr() + ")");
        }

        if (validLog(left.value(), right.value())) {
            pushUnique(results, Math.log(left.value()) / Math.log(right.value()), "log_" + right.expr() + "(" + left.expr() + ")");
        }
        if (validLog(right.value(), left.value())) {
            pushUnique(results, Math.log(right.value()) / Math.log(left.value()), "log_" + left.expr() + "(" + right.expr() + ")");
        }

        return results;
    }

    /**
     * 扩展可能的一元操作（例如平方根、阶乘）
     */
    private static List<State> expandUnary(State state) {
        List<State> states = new ArrayList<>();
        states.add(state); // 原值
        
        if (state.unaryDepth() >= MAX_UNARY_DEPTH) {
            return states;
        }

        if (state.value() >= -EPS) {
            double result = state.value() > 0.0 ? Math.sqrt(state.value()) : 0.0;
            pushUniqueWithDepth(states, result, "sqrt(" + state.expr() + ")", state.unaryDepth() + 1);
        }

        Integer integerValue = approxInteger(state.value());
        if (integerValue != null && integerValue <= MAX_FACTORIAL) {
            pushUniqueWithDepth(states, (double) factorial(integerValue), "(" + state.expr() + ")!", state.unaryDepth() + 1);
        }

        return states;
    }

    private static void pushUnique(List<State> states, double value, String expr) {
        pushUniqueWithDepth(states, value, expr, 0);
    }

    private static void pushUniqueWithDepth(List<State> states, double value, String expr, int unaryDepth) {
        if (!validNumber(value)) {
            return;
        }
        // 如果相同数值已经存在，则忽略
        for (State state : states) {
            if (close(state.value(), value)) {
                return;
            }
        }
        states.add(new State(value, expr, unaryDepth));
    }

    private static boolean validNumber(double value) {
        return Double.isFinite(value) && Math.abs(value) <= VALUE_BOUND;
    }

    private static boolean validPower(double base, double exponent) {
        if (Math.abs(exponent) > EXPONENT_BOUND) {
            return false;
        }
        // 如果底数为负数，指数必须为整数，否则会出现复数
        if (base < 0.0 && approxInteger(exponent) == null) {
            return false;
        }
        double result = Math.pow(base, exponent);
        return validNumber(result);
    }

    private static boolean validLog(double argument, double base) {
        return argument > 0.0 && base > 0.0 && !close(base, 1.0);
    }

    /**
     * 获取最接近的非负整数值，如果差异超过容差则返回 null
     */
    private static Integer approxInteger(double value) {
        if (value < -EPS) {
            return null;
        }
        double rounded = (double) Math.round(value);
        if (close(value, rounded) && rounded >= 0.0) {
            return (int) rounded;
        } else {
            return null;
        }
    }

    private static long factorial(int value) {
        long result = 1;
        for (int i = 2; i <= value; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * 将浮点数转换为整型离散桶，用于判重哈希
     */
    private static long bucket(double value) {
        if (value >= 0.0) {
            return (long) (value * 1_000_000.0 + 0.5);
        } else {
            return (long) (value * 1_000_000.0 - 0.5);
        }
    }

    /**
     * 判断两个浮点数是否足够相近
     */
    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= EPS;
    }
}
