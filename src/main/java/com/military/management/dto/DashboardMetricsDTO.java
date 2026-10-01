package com.military.management.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardMetricsDTO {
    private Long equipmentTypeId;
    private Long baseId;
    private int openingBalance;
    private int purchases;
    private int transfersIn;
    private int transfersOut;
    private int assigned;
    private int expended;
    private int closingBalance;
}
