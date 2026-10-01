package com.military.management.service;

import com.military.management.dto.DashboardMetricsDTO;
import com.military.management.entity.Transaction;
import com.military.management.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public DashboardMetricsDTO calculateMetrics(Long baseId, Long equipmentTypeId, LocalDateTime startDate, LocalDateTime endDate) {
        // 1. Calculate Opening Balance (all transactions before startDate)
        List<Transaction> priorTransactions = transactionRepository.findTransactionsBeforeDate(startDate);
        int openingBalance = calculateNetBalance(priorTransactions, baseId, equipmentTypeId);

        // 2. Fetch transactions within date range
        List<Transaction> rangeTransactions = transactionRepository.findTransactionsInDateRange(startDate, endDate);

        int purchases = 0;
        int transfersIn = 0;
        int transfersOut = 0;
        int assigned = 0;
        int expended = 0;

        for (Transaction t : rangeTransactions) {
            // Filter by equipment type if specified
            if (equipmentTypeId != null && !t.getEquipmentType().getId().equals(equipmentTypeId)) {
                continue;
            }

            String type = t.getTransactionType();
            int qty = t.getQuantity() != null ? t.getQuantity() : 0;

            if ("PURCHASE".equals(type)) {
                if (baseId == null || (t.getToBase() != null && t.getToBase().getId().equals(baseId))) {
                    purchases += qty;
                }
            } else if ("TRANSFER".equals(type)) {
                if (baseId != null) {
                    if (t.getToBase() != null && t.getToBase().getId().equals(baseId)) {
                        transfersIn += qty;
                    }
                    if (t.getFromBase() != null && t.getFromBase().getId().equals(baseId)) {
                        transfersOut += qty;
                    }
                } else {
                    // Overall system level transfer counts
                    transfersIn += qty;
                }
            } else if ("ASSIGNMENT".equals(type)) {
                if (baseId == null || (t.getFromBase() != null && t.getFromBase().getId().equals(baseId))) {
                    assigned += qty;
                }
            } else if ("EXPENDITURE".equals(type)) {
                if (baseId == null || (t.getFromBase() != null && t.getFromBase().getId().equals(baseId))) {
                    expended += qty;
                }
            }
        }

        // 3. Calculate Closing Balance
        int closingBalance = openingBalance + purchases + transfersIn - transfersOut - assigned - expended;

        return DashboardMetricsDTO.builder()
                .baseId(baseId)
                .equipmentTypeId(equipmentTypeId)
                .openingBalance(openingBalance)
                .purchases(purchases)
                .transfersIn(transfersIn)
                .transfersOut(transfersOut)
                .assigned(assigned)
                .expended(expended)
                .closingBalance(closingBalance)
                .build();
    }

    private int calculateNetBalance(List<Transaction> transactions, Long baseId, Long equipmentTypeId) {
        int balance = 0;

        for (Transaction t : transactions) {
            if (equipmentTypeId != null && !t.getEquipmentType().getId().equals(equipmentTypeId)) {
                continue;
            }

            String type = t.getTransactionType();
            int qty = t.getQuantity() != null ? t.getQuantity() : 0;

            if ("PURCHASE".equals(type)) {
                if (baseId == null || (t.getToBase() != null && t.getToBase().getId().equals(baseId))) {
                    balance += qty;
                }
            } else if ("TRANSFER".equals(type)) {
                if (baseId != null) {
                    if (t.getToBase() != null && t.getToBase().getId().equals(baseId)) {
                        balance += qty;
                    }
                    if (t.getFromBase() != null && t.getFromBase().getId().equals(baseId)) {
                        balance -= qty;
                    }
                }
            } else if ("ASSIGNMENT".equals(type) || "EXPENDITURE".equals(type)) {
                if (baseId == null || (t.getFromBase() != null && t.getFromBase().getId().equals(baseId))) {
                    balance -= qty;
                }
            }
        }

        return balance;
    }
}
