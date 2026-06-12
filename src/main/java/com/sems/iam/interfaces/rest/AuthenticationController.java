package com.sems.iam.interfaces.rest;

import com.sems.iam.application.internal.commandservices.AuthenticationCommandService;
import com.sems.iam.interfaces.rest.resources.*;
import com.sems.iam.interfaces.rest.transform.CommandMapper;
import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthenticationController is the entry point of the REST API for auth. In the
 * interfaces layer, a controller's only job is to handle HTTP: read the request,
 * delegate to the application service, and return a response. It must contain no
 * business logic.
 *
 *  - @RestController makes every method return JSON directly.
 *  - @RequestMapping("/api/v1/auth") is the common path prefix; each method adds
 *    its own sub-path below it.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    // The controller depends on the application service (to do the work) and the
    // mapper (to turn request DTOs into domain commands).
    private final AuthenticationCommandService authenticationCommandService;
    private final CommandMapper commandMapper;

    public AuthenticationController(
            AuthenticationCommandService authenticationCommandService,
            CommandMapper commandMapper) {
        this.authenticationCommandService = authenticationCommandService;
        this.commandMapper = commandMapper;
    }

    @Value("${security.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${security.oauth2.google.redirect-uri:}")
    private String googleRedirectUri;

    @Value("${security.oauth2.google.scopes:openid,email,profile}")
    private String googleScopes;

    // POST /register. @Valid triggers the validation rules on the request body
    // (@RequestBody binds the incoming JSON to the RegisterRequest object). The
    // mapper converts it to a command, and ResponseEntity.ok wraps the result
    // with HTTP 200.
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) { return ResponseEntity.ok(authenticationCommandService.register(commandMapper.toCommand(request))); }

    // POST /login - same pattern as register, but for existing users.
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) { return ResponseEntity.ok(authenticationCommandService.login(commandMapper.toCommand(request))); }

    // POST /google - sign in with a Google ID token sent by the frontend.
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authenticationCommandService.loginWithGoogle(request.idToken()));
    }

    // GET /google/callback - the URL Google redirects to after the user
    // approves. @RequestParam reads the "code" from the query string.
    @GetMapping("/google/callback")
    public ResponseEntity<LoginResponse> googleCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(authenticationCommandService.loginWithGoogleAuthorizationCode(code));
    }

    /**
     * GET /google/url - builds the Google consent screen URL the frontend should
     * send the user to. Every value placed into the URL is percent-encoded with
     * URLEncoder so that special characters do not break the query string.
     */
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
