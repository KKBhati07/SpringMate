package com.example.SpringMate.Auth.Service;


import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.Auth.Exception.TooManyRequestsException;
import com.example.SpringMate.Config.JwtTokenProvider;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Shared.Exception.UnauthorizedException;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Entity.VerificationCode;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionLogRepository;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SessionRepository sessionRepository;
    private final SessionLogRepository sessionLogRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final CoreUserService coreUserService;
    private final ResponseMapper responseMapper;
    private final OtpNotificationDispatcher otpNotificationDispatcher;
    private final SessionManagementHelper sessionManagementHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthHelper authHelper;

    @Transactional
    public Map<String, Boolean> logoutUser(String sessionId) {
        if(sessionId == null) throw new BadRequestException("Invalid token");
        Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);
        if (sessionOpt.isPresent()) {
            Session session = sessionOpt.get();
            sessionRepository.delete(session);
            sessionLogRepository.updateLogoutTime(session.getSessionID(), LocalDateTime.now());

            SecurityContextHolder.clearContext();
            return Map.of("logged_out",true);
        }
        throw new BadRequestException("Invalid request");
    }

    public AuthDetailsResponseDto authDetails(User authenticateUser) {
        return new AuthDetailsResponseDto(responseMapper
                .mapUser(authenticateUser), true);

    }

    @Transactional
    public void generateAndSendOTP(OtpRequestDto loginDTO) throws MessagingException {
        Map<String, Object> responseMap = new HashMap<>();
        if (loginDTO.getType() == OTPType.LOGIN) {
            User user = coreUserService.getUserByEmail(loginDTO.getEmail());

            if (user != null) {
                Optional<VerificationCode> codeOptional =
                        verificationCodeRepository.
                                findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
                if (codeOptional.isPresent()) {
                    LocalDateTime lastSentAt = codeOptional.get().getCreatedAt();
                    if (ChronoUnit.SECONDS.between(lastSentAt, LocalDateTime.now()) < 60) {
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
            }

            return;
        }
        throw new BadRequestException("Ambiguous request type");


    }

    @Transactional
    public OtpLoginResponseDto
    verifyOtp(OtpLoginRequestDto loginDTO, HttpServletRequest request) {
        if (loginDTO.getType() == OTPType.LOGIN) {
            User user = coreUserService.getUserByEmail(loginDTO.getEmail());

            if (user != null) {
                Optional<VerificationCode> codeOptional = verificationCodeRepository
                        .findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
                if (codeOptional.isPresent()) {
                    VerificationCode codeObj = codeOptional.get();
                    if (codeObj.getExpiresAt().isAfter(LocalDateTime.now()) &&
                            codeObj.getCode().equals(loginDTO.getOtp())) {
                        String sessionId = sessionManagementHelper.createSession(user, request);
                        if (sessionId != null) {
                            verificationCodeRepository.deleteByUserAndType(user, OTPType.LOGIN.name());
                            String authToken = jwtTokenProvider.generateToken(sessionId);
                            return OtpLoginResponseDto.builder()
                                    .authenticated(true).authToken(authToken)
                                    .userDetails(responseMapper.mapUser(user))
                                    .build();
                        }
                    }
                }
            }
            throw new UnauthorizedException("OTP verification failed");
        }
        throw new BadRequestException("Ambiguous request type");
    }
}
