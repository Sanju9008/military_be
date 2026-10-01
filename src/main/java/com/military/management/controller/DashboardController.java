package com.military.management.controller;

import com.military.management.dto.DashboardMetricsDTO;
import com.military.management.entity.User;
import com.military.management.repository.UserRepository;
import com.military.management.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public DashboardController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COMMANDER')")
    public ResponseEntity<?> getMetrics(
            @RequestParam(required = false) Long baseId,
            @RequestParam(required = false) Long equipmentTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        // Commander scope check
        if ("COMMANDER".equals(currentUser.getRole())) {
            Long userBaseId = currentUser.getBase() != null ? currentUser.getBase().getId() : null;
            if (baseId == null) {
                baseId = userBaseId;
            } else if (!baseId.equals(userBaseId)) {
                log.warn("Commander {} attempted to view dashboard metrics for foreign baseId {}", currentUser.getUsername(), baseId);
                return ResponseEntity.status(403).body("Commanders can only view metrics for their assigned base");
            }
        }

        log.info("User {} requesting dashboard metrics for baseId {}, equipmentTypeId {} between {} and {}",
                currentUser.getUsername(), baseId, equipmentTypeId, startDate, endDate);

        DashboardMetricsDTO metrics = dashboardService.calculateMetrics(baseId, equipmentTypeId, startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
