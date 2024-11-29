package com.sparta.settlementservice.batch.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class VideoIdPartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        System.out.println("[Partitioner] Partitioning data into " + gridSize + " parts");
        Map<String, ExecutionContext> partitionMap = new HashMap<>();

        long minVideoId = 1;  // videoId 최소 값
        long maxVideoId = 4000; // videoId 최대 값

        // 파티션 크기 계산 (전체 범위를 gridSize로 나누기)
        long partitionSize = (maxVideoId - minVideoId + 1) / gridSize;

        for (int i = 0; i < gridSize; i++) {
            long lowerBound = minVideoId + (i * partitionSize);
            long upperBound = lowerBound + partitionSize - 1;

            // 범위가 올바르게 점프하도록 1, 1001, 2001 ... 이렇게 설정
            if (i == gridSize - 1) {
                upperBound = maxVideoId; // 마지막 파티션의 upperBound가 maxVideoId로 설정
            }

            ExecutionContext context = new ExecutionContext();
            context.putLong("lowerBound", lowerBound);
            context.putLong("upperBound", upperBound);

            partitionMap.put("partition" + i, context);

            // 로그 추가
            System.out.println("Partition " + i + ": lowerBound=" + lowerBound + ", upperBound=" + upperBound);
        }

        return partitionMap;
    }

}
