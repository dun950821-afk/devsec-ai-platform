package com.guoshun.devsecai.security;

import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil, UserMapper userMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userMapper = userMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        log.info("JwtAuthenticationFilter - Path: {}, Has Token: {}", request.getRequestURI(), StringUtils.hasText(token));
        
        if (StringUtils.hasText(token) && jwtTokenUtil.validateToken(token)) {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            log.info("JwtAuthenticationFilter - Token Valid, Username: {}", username);
            
            User user = userMapper.selectByUsername(username);
            log.info("JwtAuthenticationFilter - User found: {}", user != null ? user.getUsername() : "null");
            
            if (user != null && user.getStatus() == 1) {
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        user, 
                        null, 
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()))
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("JwtAuthenticationFilter - Authentication set for user: {}", username);
            }
        } else {
            log.info("JwtAuthenticationFilter - Token validation failed or empty token");
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
