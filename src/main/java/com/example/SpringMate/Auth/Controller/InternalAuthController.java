package com.example.SpringMate.Auth.Controller;

import com.example.SpringMate.Auth.Cache.CachedAuthentication;
import com.example.SpringMate.Auth.DTO.SessionResolveRequestDto;
import com.example.SpringMate.Auth.DTO.SessionResolveResponseDto;
import com.example.SpringMate.Auth.Service.SessionValidationService;
import com.example.SpringMate.Shared.Exception.ForbiddenException;
import com.example.SpringMate.Shared.Urls;
import com.example.SpringMate.Util.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(Urls.Internal.Auth.BASE)
public class InternalAuthController {

    private final SessionValidationService validationService;

    @Value("${app.internal.service.key}")
    private String serviceKey;

    /**
     * Called by chat service.
     */
    @PostMapping(Urls.Internal.Auth.RESOLVE_SESSION)
    public ResponseEntity<Response<SessionResolveResponseDto>> resolve(
            @RequestHeader("X-SERVICE-KEY") String key,
            @RequestBody SessionResolveRequestDto request
    ) {
        log.info("INTERNAL_RESOLVE_SESSION_REQUEST");

        if (!serviceKey.equals(key)) {
            log.warn("INTERNAL_RESOLVE_SESSION_FORBIDDEN");
            throw new ForbiddenException("Invalid service key");
        }

        CachedAuthentication auth =
                validationService.validate(request.getSessionId());

        return ResponseEntity.ok(
                Response.success(
                        SessionResolveResponseDto.builder()
                                .userUuid(auth.getUserUuid())
                                .build(),
                        "Session resolved"
                )
        );
    }
}
