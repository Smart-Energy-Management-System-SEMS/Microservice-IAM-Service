package com.sems.iam.interfaces.rest;

import com.sems.iam.application.internal.commandservices.AuthenticationCommandService;
import com.sems.iam.interfaces.rest.resources.*;
import com.sems.iam.interfaces.rest.transform.CommandMapper;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationCommandService authenticationCommandService;
    private final CommandMapper commandMapper;

    @Value("${security.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${security.oauth2.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${security.oauth2.google.scopes:openid,email,profile}")
    private String googleScopes;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.ok(authenticationCommandService.register(commandMapper.toCommand(request))); }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return ResponseEntity.ok(authenticationCommandService.login(commandMapper.toCommand(request))); }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationCommandService.loginWithGoogle(request.idToken()));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<LoginResponse> googleCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(authenticationCommandService.loginWithGoogleAuthorizationCode(code));
    }

    @GetMapping("/google/url")
    public ResponseEntity<Map<String, String>> googleAuthUrl() {
        String scope = URLEncoder.encode(googleScopes.replace(",", " "), StandardCharsets.UTF_8);
        String redirectUri = URLEncoder.encode(googleRedirectUri, StandardCharsets.UTF_8);
        String clientId = URLEncoder.encode(googleClientId, StandardCharsets.UTF_8);
        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=" + scope
                + "&access_type=offline"
                + "&prompt=consent";
        return ResponseEntity.ok(Map.of("authorizationUrl", url));
    }
}
