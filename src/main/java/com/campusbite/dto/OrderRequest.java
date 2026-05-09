package com.campusbite.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String studentName;
    private String rollNumber;
    private String pickupTime;
    private List<OrderItemRequest> items;
}
