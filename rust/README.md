# Advanced24SolverRust

Rust 版本的高级 24 点求解器，功能与桌面上的 Python/Cython 项目对应：

- 数字范围 `1..=13`，四张牌，可重复，共 `1820` 种组合。
- 运算支持 `+ - * / ! sqrt x^y log_y(x)`。
- 使用带剪枝的 DFS。
- 浮点判定容差 `1e-6`。
- 限制负数开方、非法对数、过大阶乘和一元运算嵌套。
- 固定 `8` 线程并发。
- 输出结构化 `results.json`。

## 检查

```bash
cargo fmt --check
cargo clippy --all-targets --all-features -- -D warnings
cargo test
```

## 运行

```bash
./benchmark.sh
```

`benchmark.sh` 会从脚本启动开始计时；如果 `target/release/advanced24solver_rust` 不存在，或源码比产物更新，会先自动执行一次 `cargo build --release`，随后运行求解器并输出统一口径的 `总耗时`。

测速时建议连续执行 6 轮，并忽略前 2 轮：

```bash
for i in {1..6}; do ./benchmark.sh; done
```
