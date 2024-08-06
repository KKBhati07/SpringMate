package com.example.SpringMate.Config;
import com.example.SpringMate.Entity.Session;
import com.example.SpringMate.Repositoy.SessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


//TODO:: DEBUG SESSION CREATION ISSUE,
//org.springframework.dao.IncorrectResultSizeDataAccessException: Query did not return a unique result: 3 results were returned
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionRepository sessionRepository;
    SessionAuthenticationFilter(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String sessionId = request.getHeader("sessionId");
        System.out.println(">>> Session Id"+ sessionId);
        if (sessionId != null) {
           Session session = sessionRepository.findBySessionID(sessionId);
           if (session != null) {
               Authentication authentication = new UsernamePasswordAuthenticationToken(session.getUser(),
                       null,session.getUser().getAuthorities());
               SecurityContextHolder.getContext().setAuthentication(authentication);
           }
        }
        filterChain.doFilter(request, response);
    }
}
