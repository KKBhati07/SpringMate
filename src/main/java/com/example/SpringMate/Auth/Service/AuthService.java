package com.example.SpringMate.Auth.Service;


import com.example.SpringMate.Auth.DTO.AuthDetailsResponseDto;
import com.example.SpringMate.Auth.DTO.OtpLoginResponseDto;
import com.example.SpringMate.Auth.DTO.OtpRequestDto;
import com.example.SpringMate.Auth.DTO.OtpLoginRequestDto;
import com.example.SpringMate.Auth.Entity.Session;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Auth.Entity.VerificationCode;
import com.example.SpringMate.Auth.Helper.AuthHelper;
import com.example.SpringMate.Auth.Helper.SessionManagementHelper;
import com.example.SpringMate.Auth.Repository.SessionLogRepository;
import com.example.SpringMate.Auth.Repository.SessionRepository;
import com.example.SpringMate.User.Repository.UserRepository;
import com.example.SpringMate.Auth.Repository.VerificationCodeRepository;
import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.OTPType;
import com.example.SpringMate.Util.Response;
import com.example.SpringMate.Util.ResponseMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final ResponseMapper responseMapper;
    private final OtpNotificationDispatcher otpNotificationDispatcher;
    private final SessionManagementHelper sessionManagementHelper;
    private final AuthHelper authHelper;

    @Transactional
    public ResponseEntity<Response<Map<String,Boolean>>> logoutUser(String sessionId) {
        Map<String, Boolean> responseMap = new HashMap<>();
        try {
            Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                sessionRepository.delete(session);
                sessionLogRepository.updateLogoutTime(session.getSessionID(), LocalDateTime.now());

                SecurityContextHolder.clearContext();
                responseMap.put("logged_out",true);
                return ResponseEntity.ok(new Response<>(responseMap, "Logged out successfully"));
            }
            responseMap.put("logged_out", false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>(responseMap, "Bad Request"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>(responseMap, "Something went wrong"));
        }
    }

    public ResponseEntity<Response<AuthDetailsResponseDto>> authDetails(User authenticateUser) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            AuthDetailsResponseDto authDetails = new AuthDetailsResponseDto(responseMapper
                    .mapUser(authenticateUser),true);
            return ResponseEntity.ok(new Response<>(authDetails, "Data fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>(null, "Internal server Error"));

        }

    }

    @Transactional
    public ResponseEntity<Response<Object>> generateAndSendOTP(OtpRequestDto loginDTO) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            if (loginDTO.getType() == OTPType.LOGIN) {
                Optional<User> userExists = userRepository.findByEmail(loginDTO.getEmail());

                if (userExists.isPresent()) {
                    User user = userExists.get();
                    Optional<VerificationCode> codeOptional =
                            verificationCodeRepository.
                                    findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
                    if (codeOptional.isPresent()) {
                        LocalDateTime lastSentAt = codeOptional.get().getCreatedAt();
                        if (ChronoUnit.SECONDS.between(lastSentAt, LocalDateTime.now()) < 60) {
                            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                                    .body(new Response<>(null,
                                            "Please wait before requesting another OTP."));
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

                return ResponseEntity.ok(new Response<>(null, "OTP sent successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(new Response<>(null, "Ambiguous request type"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>(responseMap, "Internal server Error"));
        }


    }

    @Transactional
    public ResponseEntity<Response<OtpLoginResponseDto>>
    verifyOtp(OtpLoginRequestDto loginDTO, HttpServletRequest request) {
        try {
            if (loginDTO.getType() == OTPType.LOGIN) {
                Optional<User> userExists = userRepository.findByEmail(loginDTO.getEmail());

                if (userExists.isPresent()) {
                    User user = userExists.get();
                    Optional<VerificationCode> codeOptional = verificationCodeRepository
                            .findTopByUserAndTypeOrderByCreatedAtDesc(user, OTPType.LOGIN.name());
                    if (codeOptional.isPresent()) {
                        VerificationCode codeObj = codeOptional.get();
                        if (codeObj.getExpiresAt().isAfter(LocalDateTime.now()) &&
                                codeObj.getCode().equals(loginDTO.getOtp())) {
                            String sessionId = sessionManagementHelper.createSession(user, request);
                            if (sessionId != null) {
                                verificationCodeRepository.deleteByUserAndType(user, OTPType.LOGIN.name());
                                OtpLoginResponseDto response = OtpLoginResponseDto.builder()
                                        .authenticated(true).sessionId(sessionId)
                                        .userDetails(responseMapper.mapUser(user))
                                        .build();
                                return ResponseEntity.ok(new Response<>(response, "Logged in successfully!"));
                            }
                        }
                    }
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                        .body(new Response<>(null, "OTP verification failed"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(new Response<>(null, "Ambiguous request type"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response<>(null, "Internal server Error"));
        }
    }
}
