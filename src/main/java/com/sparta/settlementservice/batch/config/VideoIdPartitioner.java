package com.sparta.settlementservice.batch.config;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class VideoIdPartitioner implements Partitioner {
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> partitionMap = new HashMap<>();
        long minVideoId = 1;  // videoId 최소 값
        long maxVideoId = 4000; // videoId 최대 값

        long partitionSize = (maxVideoId - minVideoId + 1) / gridSize;

        for (int i = 0; i < gridSize; i++) {
            long lowerBound = minVideoId + (i * partitionSize);
            long upperBound = (i == gridSize - 1) ? maxVideoId : (lowerBound + partitionSize - 1);


            // 로그 추가
            System.out.println("Partition " + i + ": lowerBound = " + lowerBound + ", upperBound = " + upperBound);

            ExecutionContext context = new ExecutionContext();
            context.putLong("lowerBound", lowerBound);
            context.putLong("upperBound", upperBound);

            partitionMap.put("partition" + i, context);
        }

        return partitionMap;
    }
}
