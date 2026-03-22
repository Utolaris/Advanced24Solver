# Advanced 24 Solver (Java)

这是 `Advanced24Solver` 的 Java 版本，直接由 Rust 版本翻译而来。

## 环境要求
* Java 21+
* Maven 3.6+
* GraalVM（可选，用于编译为原生可执行文件）

## 运行方式

执行正式测速：

```bash
./benchmark.sh
```

`benchmark.sh` 会从脚本启动开始计时；如果 `target/classes/org/example/Main.class` 不存在，或源码比产物更新，会先自动执行 `mvn -DskipTests compile`，随后直接运行 `target/classes` 下的主程序并输出统一口径的 `总耗时`。

测速时建议连续执行 6 轮，并忽略前 2 轮：

```bash
for i in {1..6}; do ./benchmark.sh; done
```

如需单独运行 Maven 启动的 Java 版本：

```bash
mvn compile exec:java -Dexec.mainClass="org.example.Main"
```

运行单元测试：
```bash
mvn test
```

如需手动编译为 GraalVM 原生可执行文件（需配置好 GraalVM Native Image 环境）：
```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home"
mvn clean package -Pnative native:compile
```

## 功能特点
* 枚举所有可能的 4 张牌面（1-13）组合，共 1820 种
* 使用基于 ForkJoinPool 的并发遍历（默认为 8 线程计算，可通过 `A24_THREADS` 环境变量修改）
* 深度优先搜索可能出现的 + - * / ^ log sqrt ! 各种数学组合
* 剪枝去重使用离散桶哈希算法，大幅提高计算速度
* 计算结果最终写入 `results.json`
