package com.example.SpringMate.Auth.Service;


import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Auth.DTO.SessionResolveRequestDto;
import com.example.SpringMate.Auth.DTO.SessionResolveResponseDto;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Exception.TooManyRequestsException;
import com.example.SpringMate.Auth.jwt.JwtTokenProvider;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Shared.Exception.UnauthorizedException;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Entity.VerificationCode;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.Auth.Repository.VerificationCodeRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.OTPType;
import com.example.SpringMate.User.Service.CoreUserService;
import com.example.SpringMate.Util.ResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SessionRepository sessionRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CoreUserService coreUserService;
    private final ResponseMapper responseMapper;
    private final OtpNotificationDispatcher otpNotificationDispatcher;
    private final SessionManagementHelper sessionManagementHelper;
    private final AuthCacheService authCacheService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;

    @Transactional
    public void logoutUser(String sessionId) {
        if (sessionId == null) {
            log.warn("action=LOGOUT_SESSION reason=NULL_SESSION_ID");
            throw new BadRequestException("Invalid token");
        }
        Optional<Session> sessionOpt = sessionRepository.findBySessionId(sessionId);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            sessionRepository.delete(session);
            sessionManagementHelper.updateSessionLogoutTime(session.getSessionId());

            SecurityContextHolder.clearContext();
            log.info("action=LOGOUT_SESSION result=SUCCESS");
        } else {
            log.info("action=LOGOUT_SESSION result=SESSION_NOT_FOUND");
        }
    }

    public AuthDetailsResponseDto authDetails(UUID uuid) {
        return new AuthDetailsResponseDto(responseMapper
                .mapUser(coreUserService.getUserOrThrowByUUID(uuid)), true);

    }

    /**
     * Returns silently on non-existent users to prevent email enumeration attacks.
     * Rate limited to 60 seconds to prevent abuse.
     */
    @Transactional
    public void generateAndSendOTP(String email, OTPType type) {
        if (type == OTPType.LOGIN) {
            User user = coreUserService.getUserByEmail(email);
            if (user == null) {
                log.info("action=LOGIN_OTP_REQUEST result=USER_NOT_FOUND");
                return;
            }

            if (isRateLimited(user, OTPType.LOGIN)) {
                log.warn("action=LOGIN_OTP_REQUEST result=RATE_LIMITED");
                throw new TooManyRequestsException("Please wait before requesting another OTP.");
            }
            verificationCodeRepository.deleteByUserAndType(user, OTPType.LOGIN.name());

            String otp = generateAndSaveOTP(user, OTPType.LOGIN);

            otpNotificationDispatcher.dispatchEmail(
                    email,
                    Constants.EmailHeaders.LOGIN,
                    otp
            );

            log.info("action=LOGIN_OTP_REQUEST result=SENT");
            return;
        }
        if (type == OTPType.EMAIL_VERIFICATION) {
            User user = coreUserService.getUserByEmail(email);
            if (user == null) {
                log.info("action=EMAIL_VERIFICATION_OTP_REQUEST result=USER_NOT_FOUND");
                return;
            }
            if (user.isEmailVerified()) {
                log.info("action=EMAIL_VERIFICATION_OTP_REQUEST result=EMAIL_ALREADY_VERIFIED");
                return;
            }
            if (isRateLimited(user, OTPType.EMAIL_VERIFICATION)) {
                log.warn("action=EMAIL_VERIFICATION_OTP_REQUEST result=RATE_LIMITED");
                throw new TooManyRequestsException("Please wait before requesting another OTP.");
            }
            verificationCodeRepository.deleteByUserAndType(user, OTPType.EMAIL_VERIFICATION.name());
            String otp = generateAndSaveOTP(user, OTPType.EMAIL_VERIFICATION);
            otpNotificationDispatcher.dispatchEmail(email, Constants.EmailHeaders.EMAIL_VERIFICATION, otp);
            log.info("action=EMAIL_VERIFICATION_OTP_REQUEST result=SENT");
            return;
        }
        log.warn("action=OTP_REQUEST reason=INVALID_TYPE");
        throw new BadRequestException("Ambiguous request type");
    }

    public boolean isRateLimited(User user, OTPType type) {
        Optional<VerificationCode> codeOptional = verificationCodeRepository
                .findTopByUserAndTypeOrderByCreatedAtDesc(user, type.name());
        if (codeOptional.isPresent()) {
            LocalDateTime lastSentAt = codeOptional.get().getCreatedAt();
            return ChronoUnit.SECONDS.between(lastSentAt, LocalDateTime.now()) < 60;
        }
        return false;
    }

    public String generateAndSaveOTP(User user, OTPType type) {
        String otp = authHelper.generateOTP();
        VerificationCode code = VerificationCode.builder()
                .code(otp).type(type.name())
                .user(user).build();
        verificationCodeRepository.save(code);
        return otp;
    }

    /**
     * Caches authenticated user session to avoid database lookups on each request.
     */
    @Transactional
    public OtpLoginResponseDto
    verifyOtp(OtpLoginRequestDto loginDTO, HttpServletRequest request, HttpServletResponse response) {
        log.info("action=OTP_VERIFY_ATTEMPT");
        if (loginDTO.getType() == OTPType.LOGIN) {
            User user = coreUserService.getUserByEmail(loginDTO.getEmail());

            if (user != null) {
                Optional<VerificationCode> codeOptional = verificationCodeRepository
                        .findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
                if (codeOptional.isPresent()) {
                    VerificationCode codeObj = codeOptional.get();
                    if (codeObj.getExpiresAt().isAfter(LocalDateTime.now()) &&
                            codeObj.getCode().equals(loginDTO.getOtp())) {
                        Session session = sessionManagementHelper.createSession(user, request);
                        if (session != null) {
                            authCacheService.cacheAuthenticatedUser(session.getSessionId(), user, session.getExpiresAt());
                            verificationCodeRepository.deleteByUserAndType(user, OTPType.LOGIN.name());
                            log.info(
                                    "action=OTP_VERIFY_SUCCESS user=[UUID {}]",
                                    user.getUuid()
                            );
                            String authToken = jwtTokenProvider.generateToken(session.getSessionId());
                            authHelper.injectAuthCookie(response, authToken);
                            return OtpLoginResponseDto.builder()
                                    .userUuid(user.getUuid())
                                    .authenticated(true)
                                    .build();
                        }
                    }
                }
            }
            log.warn("action=OTP_VERIFY_FAILED");
            throw new UnauthorizedException("OTP verification failed");
        }
        log.warn("action=OTP_VERIFY reason=INVALID_TYPE");
        throw new BadRequestException("Ambiguous request type");
    }

    /**
     * Verifies the email verification OTP: validates the code, then marks the user's email as verified.
     * Does not create a session or log the user in.
     */
    @Transactional
    public void verifyEmailVerificationOtp(String email, String otp) {
        log.info("action=EMAIL_VERIFICATION_OTP_VERIFY_ATTEMPT");
        User user = coreUserService.getUserByEmail(email);
        if (user == null) {
            log.warn("action=EMAIL_VERIFICATION_OTP_VERIFY result=USER_NOT_FOUND");
            throw new UnauthorizedException("Verification failed");
        }
        Optional<VerificationCode> codeOptional = verificationCodeRepository
                .findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.EMAIL_VERIFICATION.name());
        if (codeOptional.isEmpty()) {
            log.warn("action=EMAIL_VERIFICATION_OTP_VERIFY result=NO_CODE");
            throw new UnauthorizedException("Verification failed");
        }
        VerificationCode code = codeOptional.get();
        if (code.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("action=EMAIL_VERIFICATION_OTP_VERIFY result=EXPIRED");
            throw new UnauthorizedException("Verification code expired");
        }
        if (!code.getCode().equals(otp)) {
            log.warn("action=EMAIL_VERIFICATION_OTP_VERIFY result=INVALID_CODE");
            throw new UnauthorizedException("Verification failed");
        }
        verificationCodeRepository.deleteByUserAndType(user, OTPType.EMAIL_VERIFICATION.name());
        coreUserService.markEmailVerified(user);
        log.info("action=EMAIL_VERIFICATION_OTP_VERIFY result=SUCCESS userUuid={}", user.getUuid());
    }

    /**
     * Resolves a session by sessionId and returns the associated user UUID.
     * Used by external services (e.g., chat service) to validate sessions.
     */
    public SessionResolveResponseDto resolveSession(SessionResolveRequestDto requestDto) {
        log.info("action=RESOLVE_SESSION sessionId={}", requestDto.getSessionId());

        if (requestDto.getSessionId() == null || requestDto.getSessionId().isBlank()) {
            log.warn("action=RESOLVE_SESSION reason=INVALID_SESSION_ID");
            throw new BadRequestException("Session ID is required");
        }

        Optional<Session> sessionOpt = sessionRepository.findBySessionId(requestDto.getSessionId());
        if (sessionOpt.isEmpty()) {
            log.warn("action=RESOLVE_SESSION reason=SESSION_NOT_FOUND");
            throw new UnauthorizedException("Invalid session");
        }

        Session session = sessionOpt.get();
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("action=RESOLVE_SESSION reason=SESSION_EXPIRED");
            throw new UnauthorizedException("Session expired");
        }

        UUID userUuid = session.getUser().getUuid();
        log.info("action=RESOLVE_SESSION result=SUCCESS userUuid={}", userUuid);
        return SessionResolveResponseDto.builder()
                .userUuid(userUuid)
                .build();
    }
}
