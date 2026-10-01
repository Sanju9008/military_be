package com.military.management.controller;

import com.military.management.dto.TransactionDTO;
import com.military.management.entity.Transaction;
import com.military.management.entity.User;
import com.military.management.repository.TransactionRepository;
import com.military.management.repository.UserRepository;
import com.military.management.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService,
                                 TransactionRepository transactionRepository,
                                 UserRepository userRepository) {
        this.transactionService = transactionService;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long equipmentTypeId) {

        List<Transaction> transactions = transactionRepository.findAll();

        if (type != null && !type.isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getTransactionType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        if (equipmentTypeId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getEquipmentType() != null && t.getEquipmentType().getId().equals(equipmentTypeId))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMANDER') or hasRole('LOGISTICS')")
    public ResponseEntity<?> purchase(@RequestBody TransactionDTO dto, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (!hasAccessToBase(currentUser, dto.getToBaseId())) {
            log.warn("Access denied for user {} attempting purchase for baseId {}", currentUser.getUsername(), dto.getToBaseId());
            return ResponseEntity.status(403).body("Access denied for specified base");
        }

        log.info("User {} recording PURCHASE transaction for equipmentTypeId {} with quantity {}",
                currentUser.getUsername(), dto.getEquipmentTypeId(), dto.getQuantity());

        Transaction result = transactionService.recordPurchase(dto, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMANDER') or hasRole('LOGISTICS')")
    public ResponseEntity<?> transfer(@RequestBody TransactionDTO dto, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (!hasAccessToBase(currentUser, dto.getFromBaseId())) {
            log.warn("Access denied for user {} attempting transfer from baseId {}", currentUser.getUsername(), dto.getFromBaseId());
            return ResponseEntity.status(403).body("Access denied for source base");
        }

        log.info("User {} recording TRANSFER transaction from baseId {} to baseId {} with quantity {}",
                currentUser.getUsername(), dto.getFromBaseId(), dto.getToBaseId(), dto.getQuantity());

        Transaction result = transactionService.recordTransfer(dto, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/assignment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMANDER')")
    public ResponseEntity<?> assignment(@RequestBody TransactionDTO dto, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (!hasAccessToBase(currentUser, dto.getFromBaseId())) {
            log.warn("Access denied for user {} attempting assignment at baseId {}", currentUser.getUsername(), dto.getFromBaseId());
            return ResponseEntity.status(403).body("Access denied for specified base");
        }

        log.info("User {} recording ASSIGNMENT transaction to personnel {} at baseId {}",
                currentUser.getUsername(), dto.getPersonnelName(), dto.getFromBaseId());

        Transaction result = transactionService.recordAssignment(dto, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/expenditure")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMANDER')")
    public ResponseEntity<?> expenditure(@RequestBody TransactionDTO dto, Authentication authentication) {
        User currentUser = getCurrentUser(authentication);

        if (!hasAccessToBase(currentUser, dto.getFromBaseId())) {
            log.warn("Access denied for user {} attempting expenditure at baseId {}", currentUser.getUsername(), dto.getFromBaseId());
            return ResponseEntity.status(403).body("Access denied for specified base");
        }

        log.info("User {} recording EXPENDITURE transaction at baseId {} for quantity {}",
                currentUser.getUsername(), dto.getFromBaseId(), dto.getQuantity());

        Transaction result = transactionService.recordExpenditure(dto, currentUser.getId());
        return ResponseEntity.ok(result);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    private boolean hasAccessToBase(User user, Long baseId) {
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        return user.getBase() != null && user.getBase().getId().equals(baseId);
    }
}
