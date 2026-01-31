package com.example.SpringMate.Auth.Service;


import com.example.SpringMate.Auth.Cache.AuthCacheService;
import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
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
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
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
    public void generateAndSendOTP(OtpRequestDto loginDTO) throws MessagingException {
        Map<String, Object> responseMap = new HashMap<>();
        if (loginDTO.getType() == OTPType.LOGIN) {
            User user = coreUserService.getUserByEmail(loginDTO.getEmail());
            if (user == null) {
                log.info("action=OTP_REQUEST result=USER_NOT_FOUND");
                return;
            }

            Optional<VerificationCode> codeOptional =
                    verificationCodeRepository.
                            findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
            if (codeOptional.isPresent()) {
                LocalDateTime lastSentAt = codeOptional.get().getCreatedAt();
                if (ChronoUnit.SECONDS.between(lastSentAt, LocalDateTime.now()) < 60) {
                    log.warn("action=OTP_REQUEST result=RATE_LIMITED");
                    throw new TooManyRequestsException("Please wait before requesting another OTP.");
                }
            }
            verificationCodeRepository.deleteByUserAndType(user, OTPType.LOGIN.name());

            String otp = authHelper.generateOTP();
            VerificationCode code = VerificationCode.builder()
                    .code(otp).type(OTPType.LOGIN.name())
                    .user(user).build();
            verificationCodeRepository.save(code);

            otpNotificationDispatcher.dispatchEmail(
                    loginDTO.getEmail(),
                    Constants.EmailHeaders.LOGIN,
                    otp
            );

            log.info("action=OTP_REQUEST result=SENT");
            return;
        }
        log.warn("action=OTP_REQUEST reason=INVALID_TYPE");
        throw new BadRequestException("Ambiguous request type");


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
}
