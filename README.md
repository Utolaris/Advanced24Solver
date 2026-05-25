# Advanced24Solver

这个仓库现在同时包含四套实现，按语言分目录存放：

- `rust/`: Rust 版本，固定 8 线程并发，支持通过 `A24_THREADS` 覆盖线程数。
- `python/`: Python + Cython 版本，固定 8 进程并发。
- `java/`: Java 21 + GraalVM 导向版本，使用与 Rust 相同的 ForkJoinPool 并发并行搜索及离散桶剪枝去重算法。
- `csharp/`: C#/.NET 10 版本，使用 PLINQ 并发搜索，默认 8 线程并支持 `A24_THREADS` 覆盖。
四套实现都对应同一问题定义：

- 从 `1..=13` 中抽取 4 个数字，可重复，共 `1820` 种组合。
- 运算支持 `+ - * / ! sqrt x^y log_y(x)`。
- 使用带剪枝的 DFS。
- 结果目标是 `24`，容差 `1e-6`。
- 输出结构化 `results.json`。

## LLM 评测题

这个仓库也可以作为一道面向大模型的综合评测题，用来快速评估 AI 的规划能力、数学与领域知识、工具调用能力和编程实现能力：

> 对于经典游戏“24点”，从一副没有小丑的扑克中，随机抽出四张牌，然后通过牌面的计算让其等于 24。现在有三个问题：
>
> 1. 在标准 24 点游戏里，一共存在多少种牌面组合？有多少种组合是无解的？
> 2. 如果允许开方、阶乘、对数、乘方，那么还有多少种无解的组合？
> 3. 在第二问的条件下，如果用 Python 代码枚举所有牌面组合并获取计算表达式，你会如何设计一个 Python 项目来完成这个数学任务？请采用极致的性能优化，并说明完整设计思路。

## 统一测速口径

四个子项目都提供了各自的 `benchmark.sh`，这是仓库约定的正式测速入口。

- 计时从执行 `benchmark.sh` 开始。
- 如果构建产物不存在，或源码比产物更新，脚本会先自动构建，再执行求解。
- 计时结束于 `results.json` 写完之后。
- 对比成绩时，统一忽略前两轮，避免首次构建、JIT、缓存和进程预热影响。

建议每个项目都连续跑 6 轮，忽略前 2 轮：

```bash
for i in {1..6}; do ./benchmark.sh; done
```

## Rust

```bash
cd rust
cargo fmt --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test
./benchmark.sh
```

## Python

```bash
cd python
./benchmark.sh
```

## Java

```bash
cd java
./benchmark.sh
```

## C#

```bash
cd csharp
dotnet test tests/Advanced24SolverCSharp.Tests/Advanced24SolverCSharp.Tests.csproj
./benchmark.sh
```
