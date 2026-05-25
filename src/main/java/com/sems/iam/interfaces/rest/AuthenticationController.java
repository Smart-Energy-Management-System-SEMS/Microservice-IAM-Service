package com.sems.iam.interfaces.rest;

import com.sems.iam.application.internal.commandservices.AuthenticationCommandService;
import com.sems.iam.interfaces.rest.resources.*;
import com.sems.iam.interfaces.rest.transform.CommandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationCommandService authenticationCommandService;
    private final CommandMapper commandMapper;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.ok(authenticationCommandService.register(commandMapper.toCommand(request))); }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return ResponseEntity.ok(authenticationCommandService.login(commandMapper.toCommand(request))); }
}
