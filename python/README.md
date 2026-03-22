# Advanced24Solver

使用 `uv` 初始化、`Cython` 加速、`ProcessPoolExecutor(max_workers=8)` 并行求解 24 点高级规则版本。

## 初始化命令

```bash
cd ~/Desktop
mkdir -p Advanced24Solver
uv init --package Advanced24Solver
cd Advanced24Solver
uv add cython
```

## 项目文件

- `main.py`: 入口，负责扩展预编译、1820 组数字生成、8 进程并发、JSON 落盘与总耗时统计。
- `solver.pyx`: Cython DFS 求解器，包含浮点容差、严格剪枝、阶乘/开方/乘方/对数规则。
- `setup.py`: `build_ext --inplace` 编译脚本。

## 运行方式

仓库统一使用 `benchmark.sh` 作为正式测速入口：

```bash
./benchmark.sh
```

这个脚本会从启动命令开始计时，执行 `uv run python main.py`，并在扩展缺失时自动触发 `build_ext --inplace`。

测速时建议连续执行 6 轮，并忽略前 2 轮：

```bash
for i in {1..6}; do ./benchmark.sh; done
```

执行结束后，会在项目根目录生成 `results.json`。

`results.json` 中的每条记录都包含：

- `cards`: 原始 4 张牌数组。
- `cards_key`: 逗号拼接后的稳定键。
- `solved`: 是否找到解。
- `expression`: 解表达式，若未找到则为 `无解`。
