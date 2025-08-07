package org.example.blogplarform.utils;

import io.jsonwebtoken.*;

import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtUtils {
    // 签名密钥（务必保密，生产环境建议存在配置文件或环境变量中）
    private static final String SECRET_KEY = "3ckfoNpFgVP9iVGkHqnTYe6jtKP0Vn9Ujk0t4AecL0I=";

    // Token 过期时间（单位：毫秒，下面是 7 天）
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;
    // 正确解码后传给 Keys
    private static final Key KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));

    /**
     * 生成 Token
     */
    public static String generateToken(Long userId, String userName,String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRE_TIME);

        return Jwts.builder()
                .setSubject(userId.toString()) // 用户ID
                .claim("adminName", userName)
                .claim("role", role)           // 用户角色
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(KEY)
                .compact();
    }


    /**
     * 从 token 中解析出 userId
     */
    public static Long getUserIdFromToken(String token) {
        try {
            System.out.println("原始 Token: " + token); // 调试输出

            // 确保去除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
                System.out.println("处理后 Token: " + token); // 调试输出
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userIdStr = claims.getSubject();
            System.out.println("Token 解析出的用户ID：" + userIdStr);
            return Long.parseLong(userIdStr);
        } catch (ExpiredJwtException e) {
            System.out.println("Token 已过期：" + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token 格式错误：" + e.getMessage());
        } catch (Exception e) {
            System.out.println("Token 解析失败：" + e.getClass().getName() + " - " + e.getMessage());
        }
        return null;
    }
    public String getAdminNameFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("adminName", String.class);
        } catch (Exception e) {
            System.out.println("Token解析失败: " + e.getMessage());
            return null;
        }
    }

    public static String getRoleFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class);
        } catch (Exception e) {
            System.out.println("Token解析失败: " + e.getMessage());
            return null;
        }
    }


}


