package autoservice.controller;

import autoservice.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для управления аутентификацией")
public class AuthController {

    @GetMapping("/current-user")
    @Operation(summary = "Получить информацию о текущем пользователе")
    public Map<String, Object> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Попытка получить информацию о неаутентифицированном пользователе");
            throw new AuthenticationException("Пользователь не аутентифицирован");
        }

        String username = authentication.getName();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Запрос информации о текущем пользователе: {}", username);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("roles", roles);
        userInfo.put("authenticated", authentication.isAuthenticated());

        return userInfo;
    }

    @GetMapping("/check")
    @Operation(summary = "Проверить аутентификацию")
    public Map<String, String> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !"anonymousUser".equals(authentication.getName());

        if (isAuthenticated) {
            log.info("Пользователь {} успешно аутентифицирован", authentication.getName());
            return Map.of(
                    "status", "authenticated",
                    "message", "Пользователь " + authentication.getName() + " аутентифицирован",
                    "username", authentication.getName()
            );
        }


        log.debug("Проверка аутентификации: пользователь не аутентифицирован");
        return Map.of("status", "anonymous", "message", "Требуется аутентификация");
    }
}