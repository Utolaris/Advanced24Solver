package org.example;

/**
 * 记录中间计算状态
 * @param value 当前状态的数值大小
 * @param expr 产生当前数值的表达式字符串
 * @param unaryDepth 当前数值已经过的一元操作（如开方、阶乘）深度，避免无限循环
 */
public record State(double value, String expr, int unaryDepth) {}
