package org.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * 主程序入口
 */
public class Main {
    private static final int WORKER_COUNT = 8;
    
    public record Metadata(
        int target,
        double tolerance,
        int workerCount,
        int combinationCount
    ) {}
    
    public record Record(
        int[] cards,
        String cardsKey,
        boolean solved,
        String expression
    ) {}
    
    public record Payload(
        Metadata metadata,
        List<Record> results
    ) {}

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        List<int[]> combinations = allCombinations();
        int workerCount = configuredWorkerCount();

        // NOTE: 使用自定义线程池控制并发核心数
        ForkJoinPool customThreadPool = new ForkJoinPool(workerCount);
        List<Record> records;
        try {
            records = customThreadPool.submit(() ->
                combinations.parallelStream().map(cards -> {
                    String expression = Solver.solveCards(cards);
                    String resultExpr = expression != null ? expression : "无解";
                    return new Record(
                        cards,
                        cardsKey(cards),
                        expression != null,
                        resultExpr
                    );
                }).collect(Collectors.toList())
            ).join();
        } finally {
            customThreadPool.shutdown();
        }
        
        // 按照卡牌数组排序
        records.sort((r1, r2) -> Arrays.compare(r1.cards(), r2.cards()));
        

        
        // NOTE: 手动构建 JSON 文件，避免在原生镜像中使用 Jackson 产生因为反射导致的问题
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"metadata\": {\n");
        sb.append("    \"target\": 24,\n");
        sb.append("    \"tolerance\": ").append(Solver.EPS).append(",\n");
        sb.append("    \"worker_count\": ").append(workerCount).append(",\n");
        sb.append("    \"combination_count\": ").append(records.size()).append("\n");
        sb.append("  },\n");
        sb.append("  \"results\": [\n");
        for (int i = 0; i < records.size(); i++) {
            Record r = records.get(i);
            sb.append("    {\n");
            sb.append("      \"cards\": [").append(r.cards()[0]).append(", ").append(r.cards()[1]).append(", ").append(r.cards()[2]).append(", ").append(r.cards()[3]).append("],\n");
            sb.append("      \"cards_key\": \"").append(r.cardsKey()).append("\",\n");
            sb.append("      \"solved\": ").append(r.solved()).append(",\n");
            String expr = r.expression().replace("\"", "\\\"");
            sb.append("      \"expression\": \"").append(expr).append("\"\n");
            sb.append("    }");
            if (i < records.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        
        File outputFile = new File(projectRoot(), "results.json");
        java.nio.file.Files.writeString(outputFile.toPath(), sb.toString());
        
        System.out.printf("完成 %d 种组合计算，结果已写入 %s\n", combinations.size(), outputFile.getName());
        if (!"1".equals(System.getenv("A24_SUPPRESS_INTERNAL_TIMING"))) {
            System.out.printf("总耗时: %.6f 秒\n", (System.currentTimeMillis() - start) / 1000.0);
        }
    }

    /**
     * 获取配置的工作线程数，优先从环境变量读取
     */
    static int configuredWorkerCount() {
        String envValue = System.getenv("A24_THREADS");
        if (envValue != null) {
            try {
                int value = Integer.parseInt(envValue);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // 忽略解析错误，使用默认值
            }
        }
        return WORKER_COUNT;
    }

    /**
     * 生成所有可能的 4 张牌的组合（范围 1 到 13，允许重复）
     */
    static List<int[]> allCombinations() {
        List<int[]> combinations = new ArrayList<>(1820);
        for (int first = 1; first <= 13; first++) {
            for (int second = first; second <= 13; second++) {
                for (int third = second; third <= 13; third++) {
                    for (int fourth = third; fourth <= 13; fourth++) {
                        combinations.add(new int[]{first, second, third, fourth});
                    }
                }
            }
        }
        return combinations;
    }

    /**
     * 将卡牌数组格式化为字符串，用作唯一标识键
     */
    static String cardsKey(int[] cards) {
        return cards[0] + "," + cards[1] + "," + cards[2] + "," + cards[3];
    }
    
    /**
     * 获取项目根目录路径
     */
    static File projectRoot() {
        return new File(System.getProperty("user.dir"));
    }
}
