package com.sems.iam.interfaces.rest.resources;
import java.util.*;
public record LoginResponse(String token, UUID userId, String emailAddress, List<String> roles) {}
