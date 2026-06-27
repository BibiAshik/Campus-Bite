package com.campusbite.dto.response;

import lombok.Data;

@Data
public class AdminDashboardStatsDTO {
    private long totalOrders;
    private double totalRevenue;
    private long totalPayments;
    private long pendingOrders;
}
