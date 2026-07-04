package com.archiveplayer.security;

import com.archiveplayer.repositories.AccountRepository;
import com.archiveplayer.entities.Account;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JWTTokenGenerator tokenProvider;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(JWTTokenGenerator tokenProvider, AccountRepository accountRepository) {
        this.tokenProvider = tokenProvider;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        logger.info("Incoming request: {} {}", method, path);

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                if (tokenProvider.validateToken(jwt)) {
                    Long userId = tokenProvider.getUserIdFromJWT(jwt);
                    String tokenId = tokenProvider.getTokenIdFromJWT(jwt);

                    Optional<Account> accountOpt = accountRepository.findById(userId);
                    if (accountOpt.isPresent()) {
                        Account account = accountOpt.get();
                        String dbTokenId = account.getActiveSessionToken();

                        if (tokenId != null && tokenId.equals(dbTokenId)) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    account, null, Collections.emptyList()); // Reverted to Collections.emptyList()
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("Authentication successful for user {}. JWT TokenID={}, DB TokenID={}", 
                                    account.getAccountName(), tokenId, dbTokenId);
                        } else {
                            logger.warn("Session validation failed for user {}: JWT TokenID={}, DB TokenID={}. Request: {} {}", 
                                    account.getAccountName(), tokenId, dbTokenId, method, path);
                        }
                    } else {
                        logger.warn("User ID {} from JWT not found in database. Request: {} {}", userId, method, path);
                    }
                } else {
                    logger.warn("JWT validation failed for token: {}. Request: {} {}", jwt.substring(0, Math.min(jwt.length(), 10)) + "...", method, path);
                }
            } else {
                logger.debug("No JWT found in request headers for path: {}. Request: {} {}", path, method, path);
            }
        } catch (Exception ex) {
            logger.error("Error in JwtAuthenticationFilter for request {} {}: {}", method, path, ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}