package com.orderservice.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.orderservice.service.UserSecurityVersionService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    private final UserSecurityVersionService userSecurityVersionService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserSecurityVersionService userSecurityVersionService
    ) {
        this.jwtService = jwtService;
        this.userSecurityVersionService
                = userSecurityVersionService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader
                = request.getHeader(HttpHeaders.AUTHORIZATION);

        /*
         * If no Bearer token is provided, continue to Spring
         * Security. SecurityConfig will decide whether the
         * endpoint is public or protected.
         */
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            Claims claims
                    = jwtService.extractAllClaims(token);

            String email = claims.getSubject();

            Long userId = extractLongClaim(
                    claims,
                    "userId"
            );

            String role = claims.get(
                    "role",
                    String.class
            );

            /*
             * JWTs created before security-version support
             * are treated as version 0.
             */
            long tokenSecurityVersion
                    = extractSecurityVersion(claims);

            if (email == null
                    || userId == null
                    || role == null) {

                writeUnauthorized(
                        response,
                        "INVALID_TOKEN",
                        "The authentication token is missing "
                        + "required claims."
                );

                return;
            }

            /*
             * This value comes from the Order Service database.
             * It is updated by the Kafka security-version event.
             */
            long currentSecurityVersion
                    = userSecurityVersionService
                            .getCurrentVersion(userId);

            /*
             * A mismatch means the password/security state
             * changed after this JWT was issued.
             */
            if (tokenSecurityVersion
                    != currentSecurityVersion) {

                writeUnauthorized(
                        response,
                        "TOKEN_REVOKED",
                        "This session is no longer valid. "
                        + "Please log in again."
                );

                return;
            }

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                String authority
                        = role.startsWith("ROLE_")
                        ? role
                        : "ROLE_" + role;

                /*
                 * OrderController expects AuthenticatedUser
                 * through @AuthenticationPrincipal.
                 */
                AuthenticatedUser authenticatedUser
                        = new AuthenticatedUser(
                                userId,
                                email,
                                role
                        );

                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(
                                authenticatedUser,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                authority
                                        )
                                )
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception exception) {
            writeUnauthorized(
                    response,
                    "INVALID_TOKEN",
                    "The authentication token is invalid "
                    + "or expired."
            );

            return;
        }

        /*
         * Keep this outside the try/catch. Otherwise, exceptions
         * from controllers could incorrectly become JWT errors.
         */
        filterChain.doFilter(request, response);
    }

    private Long extractLongClaim(
            Claims claims,
            String claimName
    ) {
        Number claimValue = claims.get(
                claimName,
                Number.class
        );

        return claimValue == null
                ? null
                : claimValue.longValue();
    }

    private long extractSecurityVersion(
            Claims claims
    ) {
        Number versionClaim = claims.get(
                "securityVersion",
                Number.class
        );

        return versionClaim == null
                ? 0L
                : versionClaim.longValue();
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String code,
            String message
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding("UTF-8");

        String responseBody = """
                {
                  "status": 401,
                  "code": "%s",
                  "message": "%s"
                }
                """.formatted(code, message);

        response.getWriter().write(responseBody);
    }
}
