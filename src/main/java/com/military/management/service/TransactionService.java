package com.military.management.service;

import com.military.management.dto.TransactionDTO;
import com.military.management.entity.Base;
import com.military.management.entity.EquipmentType;
import com.military.management.entity.Transaction;
import com.military.management.entity.User;
import com.military.management.repository.BaseRepository;
import com.military.management.repository.EquipmentTypeRepository;
import com.military.management.repository.TransactionRepository;
import com.military.management.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final BaseRepository baseRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              EquipmentTypeRepository equipmentTypeRepository,
                              BaseRepository baseRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.baseRepository = baseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction recordPurchase(TransactionDTO dto, Long currentUserId) {
        Transaction transaction = createBaseTransaction(dto, currentUserId);
        transaction.setTransactionType("PURCHASE");

        Base toBase = baseRepository.findById(dto.getToBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Target base not found"));
        transaction.setToBase(toBase);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction recordTransfer(TransactionDTO dto, Long currentUserId) {
        Transaction transaction = createBaseTransaction(dto, currentUserId);
        transaction.setTransactionType("TRANSFER");

        Base fromBase = baseRepository.findById(dto.getFromBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Source base not found"));
        Base toBase = baseRepository.findById(dto.getToBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Destination base not found"));

        transaction.setFromBase(fromBase);
        transaction.setToBase(toBase);

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction recordAssignment(TransactionDTO dto, Long currentUserId) {
        Transaction transaction = createBaseTransaction(dto, currentUserId);
        transaction.setTransactionType("ASSIGNMENT");

        Base fromBase = baseRepository.findById(dto.getFromBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        transaction.setFromBase(fromBase);
        transaction.setPersonnelName(dto.getPersonnelName());

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction recordExpenditure(TransactionDTO dto, Long currentUserId) {
        Transaction transaction = createBaseTransaction(dto, currentUserId);
        transaction.setTransactionType("EXPENDITURE");

        Base fromBase = baseRepository.findById(dto.getFromBaseId())
                .orElseThrow(() -> new IllegalArgumentException("Base not found"));
        transaction.setFromBase(fromBase);

        return transactionRepository.save(transaction);
    }

    private Transaction createBaseTransaction(TransactionDTO dto, Long currentUserId) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(dto.getEquipmentTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Equipment type not found"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Transaction transaction = new Transaction();
        transaction.setEquipmentType(equipmentType);
        transaction.setQuantity(dto.getQuantity());
        transaction.setNotes(dto.getNotes());
        transaction.setCreatedBy(user);

        return transaction;
    }
}
