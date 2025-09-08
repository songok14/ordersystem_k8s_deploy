package com.beyond.ordersystem;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class JwtTokenFilter implements GlobalFilter {
    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;
    private Key secret_at_key;

    private static final List<String> ALLOWED_PATH = List.of(
            "/member/create",
            "/member/dologin",
            "/member/refresh-at",
            "/product/list"
    );

    private static final List<String> ADMIN_ONLY_PATH = List.of(
            "/member/list",
            "/product/create"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // token 검증
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("bearerToken: " + bearerToken);

        String urlPath = exchange.getRequest().getURI().getRawPath();
        System.out.println("UrlPath: " + urlPath);

        // 인증이 필요 없는 경로는 필터 통과
        if (ALLOWED_PATH.contains((urlPath))) {
            return chain.filter(exchange);
        }

        try {
            if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                throw new IllegalArgumentException("토큰이 없거나 형식이 잘못 되었습니다.");
            }

            String token = bearerToken.substring(7);
            // token 검증 및 payload 추출

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKeyAt)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String email = claims.getSubject();
            String role = claims.get("role", String.class);

            // admin 권한이 필요한 api 검증
            if (ADMIN_ONLY_PATH.contains(urlPath) && !role.equals("ADMIN")){
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            // header에 email, role 등 payload 값 세팅
            // X를 붙이는 것은 custom header라는 것을 의미하는 관례적 키워드
            // 추후 서비스 모듈에서 RequestGeader 어노테이션을 통해 아해 헤더를 꺼낼 수 있음
            ServerWebExchange serverWebExchange = exchange.mutate()
                    .request(r -> r
                            .header("X-User-Email", email)
                            .header("X-User-Role", role))
                    .build();
            return chain.filter(serverWebExchange);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // 추가적인 body 메시지는 필요 시 별도 설정 필요
            return exchange.getResponse().setComplete();
        }
    }
}
