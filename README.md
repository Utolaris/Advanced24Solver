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
