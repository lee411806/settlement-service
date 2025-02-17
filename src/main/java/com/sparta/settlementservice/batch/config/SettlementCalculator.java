package com.sparta.settlementservice.batch.config;

import org.springframework.context.annotation.Configuration;

public class SettlementCalculator {

    public long calculateVideoRevenue(long totalViewCount) {
        if (totalViewCount < 100_000) {
            return (long) Math.floor(totalViewCount * 1);
        } else if (totalViewCount < 500_000) {
            return (long) Math.floor(99_999 * 1 + (totalViewCount - 99_999) * 1.1);
        } else if (totalViewCount < 1_000_000) {
            return (long) Math.floor(99_999 * 1 + 400_000 * 1.1 + (totalViewCount - 499_999) * 1.3);
        } else {
            return (long) Math.floor(99_999 * 1 + 400_000 * 1.1 + 500_000 * 1.3 + (totalViewCount - 999_999) * 1.5);
        }
    }

    public long calculateAdRevenue(long totalAdViewCount) {
        if (totalAdViewCount < 100_000) {
            return (long) Math.floor(totalAdViewCount * 10);
        } else if (totalAdViewCount < 500_000) {
            return (long) Math.floor(99_999 * 10 + (totalAdViewCount - 99_999) * 12);
        } else if (totalAdViewCount < 1_000_000) {
            return (long) Math.floor(99_999 * 10 + 400_000 * 12 + (totalAdViewCount - 499_999) * 15);
        } else {
            return (long) Math.floor(99_999 * 10 + 400_000 * 12 + 500_000 * 15 + (totalAdViewCount - 999_999) * 20);
        }
    }

    public long calculateTotalRevenue(long totalViewCount, long totalAdViewCount) {
        return calculateVideoRevenue(totalViewCount) + calculateAdRevenue(totalAdViewCount);
    }

}
