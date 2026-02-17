package com.cognizant.smartpay.controller;

import com.cognizant.smartpay.service.BiometricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/validation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ValidationController {

    private final BiometricService biometricService;

    @PostMapping("/check-user")
    public ResponseEntity<?> checkUser(@RequestBody Map<String, String> request) {
        String type = request.get("type");
        String value = request.get("value");

        boolean exists = biometricService.checkUserExists(type, value);

        return ResponseEntity.ok(Collections.singletonMap("exists", exists));
    }
}