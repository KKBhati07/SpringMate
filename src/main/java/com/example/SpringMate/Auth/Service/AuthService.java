package com.example.SpringMate.Auth.Service;


import com.example.SpringMate.Auth.DTO.OTPRequestDTO;
import com.example.SpringMate.Auth.DTO.OtpLoginDTO;
import com.example.SpringMate.Admin.Entity.Session;
import com.example.SpringMate.User.Entity.User;
import com.example.SpringMate.Admin.Entity.VerificationCode;
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
    public ResponseEntity<Response> logoutUser(String sessionId) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            Optional<Session> sessionOpt = sessionRepository.findBySessionID(sessionId);
            if (sessionOpt.isPresent()) {
                Session session = sessionOpt.get();
                sessionRepository.delete(session);
                sessionLogRepository.updateLogoutTime(session.getSessionID(), LocalDateTime.now());

                SecurityContextHolder.clearContext();
                responseMap.put("status", HttpStatus.OK.value());
                return ResponseEntity.ok(new Response(responseMap, "Logged out successfully"));
            }
            responseMap.put("status", 400);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(responseMap, "Bad Request"));

        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("status", 500);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Something went wrong"));
        }
    }

    public ResponseEntity<Response> authDetails(User authenticateUser) {
        Map<String, Object> responseMap = new HashMap<>();
        try {
            responseMap.put("status", HttpStatus.OK.value());
            responseMap.put("is_authenticated", true);
            responseMap.put("user_details", responseMapper
                    .mapUser(authenticateUser));
            return ResponseEntity.ok(new Response(responseMap, "Data fetched successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Internal server Error"));

        }

    }

    @Transactional
    public ResponseEntity<Response> generateAndSendOTP(OTPRequestDTO loginDTO) {
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
                                    .body(new Response(responseMap,
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

                return ResponseEntity.ok(new Response(responseMap, "OTP sent successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(new Response(responseMap, "Ambiguous request type"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Internal server Error"));
        }


    }

    @Transactional
    public ResponseEntity<Response> verifyOtp(OtpLoginDTO loginDTO, HttpServletRequest request) {
        Map<String, Object> responseMap = new HashMap<>();
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
                                responseMap.put("sessionId", sessionId);
                                responseMap.put("authenticated", true);
                                responseMap.put("user_details", user);
                                return ResponseEntity.ok(new Response(responseMap, "Logged in successfully!"));
                            }
                        }
                    }
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN.value())
                        .body(new Response(responseMap, "OTP verification failed"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                    .body(new Response(responseMap, "Ambiguous request type"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .body(new Response(responseMap, "Internal server Error"));
        }
    }
}
