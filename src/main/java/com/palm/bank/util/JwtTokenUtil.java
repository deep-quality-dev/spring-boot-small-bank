package com.palm.bank.util;

import com.palm.bank.security.SecurityConstants;
import io.jsonwebtoken.Jwts;

import javax.servlet.http.HttpServletRequest;

public class JwtTokenUtil {

    /**
     * Get the username from jwt token
     *
     * @param request
     * @return
     */
    public static String getUsername(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        if (token == null) {
            return null;
        }

        token = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        String username = Jwts.parser()
                .setSigningKey(SecurityConstants.getTokenSecret())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return username;
    }
}
