package com.alibaba.arthas.tunnel.server.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.TextCodec;
import io.jsonwebtoken.lang.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt token utils
 * 
 * @author zzj
 * @version 2019-07-23
 */
@Slf4j
public class JwtUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /***
     * 解析 token 信息
     * @param jwtToken jwtToken
     * @param secret 签名密钥
     * @return claims对象
     */
    public static Claims getClaimsFromToken(String jwtToken, String secret) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    //签名的key
                    .setSigningKey(secret)
                    // 签名token
                    .parseClaimsJws(jwtToken)
                    .getBody();
        } catch (JwtException e) {
            log.warn("jwt 解析异常， {}", e.getMessage());
        }
        return claims;
    }

    /***
     * 解析 token 信息。注意此方法对压缩的jwttoken无效
     * @param jwtToken jwttoken
     * @return claims对象
     */
    public static Claims getClaimsFromToken(String jwtToken) {
        if (jwtToken == null) {
            return null;
        }

        String base64UrlEncodedPayload = null;
        int delimiterCount = 0;

        StringBuilder sb = new StringBuilder(128);

        for (char c : jwtToken.toCharArray()) {

            if (c == JwtParser.SEPARATOR_CHAR) {

                CharSequence tokenSeq = Strings.clean(sb);
                String token = tokenSeq!=null?tokenSeq.toString():null;

                if (delimiterCount == 1) {
                    base64UrlEncodedPayload = token;
                    break;
                }

                delimiterCount++;
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        if (base64UrlEncodedPayload == null) {
            return null;
        }
        String payload = TextCodec.BASE64URL.decodeToString(base64UrlEncodedPayload);
        //likely to be json, parse it:
        if (payload.charAt(0) == '{' && payload.charAt(payload.length() - 1) == '}') {
            Map<String, Object> claimsMap = readValue(payload);
            return new DefaultClaims(claimsMap);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readValue(String val) {
        try {
            return objectMapper.readValue(val, Map.class);
        } catch (IOException e) {
            throw new MalformedJwtException("Unable to read JSON value: " + val, e);
        }
    }

    /**
     * 签发 jwttoken
     * @param subject subject
     * @param issuer  issuer
     * @param claims  主体信息
     * @param secret  加密密钥
     * @param expiration 过期时间，单位秒
     * @return String jwtToken
     */
    public static String signJwtToken (String subject,
                                       String issuer,
                                       HashMap<String, Object> claims,
                                       String secret,
                                       Long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * 判断jwttoken是否过期
     * @param token jwt
     * @return true or false
     */
    public static boolean isJwtTokenExpired(String token) {
        if (token == null) {
            log.warn("token值null");
            return true;
        }

        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration().getTime() <= System.currentTimeMillis();
    }
}
