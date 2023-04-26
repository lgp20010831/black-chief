package com.black.core.servlet;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.black.core.json.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenValidator implements TokenResolver{

    public final String TOKEN_REQUEST_HEADER;

    public final String TOKEN_PREFIX;

    public static final String DEFAULT_TOKEN_REQUEST_HEADER = "authorization";

    public static final String DEFAULT_TOKEN_PREFIX = "Bearer ";

    private ValidatorToken validatorToken;

    public TokenValidator(){
        this(null, null);
    }

    public TokenValidator(String token_request_header, String token_prefix) {
        TOKEN_REQUEST_HEADER = token_request_header == null ? DEFAULT_TOKEN_REQUEST_HEADER : token_request_header;
        TOKEN_PREFIX = token_prefix == null ? DEFAULT_TOKEN_PREFIX : token_prefix;
    }

    @Override
    public boolean isCapableOfVerification() {
        return validatorToken != null;
    }

    public Map<String, Object> parseToken(String token){
        if (token == null)
            return null;
        DecodedJWT decode = JWT.decode(token);
        Map<String, Claim> claims = decode.getClaims();
        Map<String, Object> resultMap = new HashMap<>();
        claims.forEach((k, c) -> resultMap.put(k, c.as(Object.class)));
        return resultMap;
    }

    @Override
    public String validatorToken(TokenResolver tokenResolver, HttpServletRequest request) throws NoTokenException, TokenExpirationException {
        if (validatorToken != null){
            StringBuffer url = request.getRequestURL();
            return validatorToken.validatorToken(tokenResolver, request, new String(url), request.getServletPath());
        }
        return null;
    }

    public Claim tokenAs(String token, String key){
        if (token == null)
            return null;
        DecodedJWT decode = JWT.decode(token);
        return decode.getClaim(key);
    }

    public String getToken(HttpServletRequest servletRequest) throws NoTokenException{
        if (servletRequest == null)
            throw new NoTokenException("原因为请求实例为空");
        String header = servletRequest.getHeader(TOKEN_REQUEST_HEADER);
        if (header == null || !header.contains(TOKEN_PREFIX))
            throw new NoTokenException("原因可能为 header 中不存在" + TOKEN_REQUEST_HEADER + "; 或值前缀中不包含:" + TOKEN_PREFIX);
        return header.substring(header.indexOf(TOKEN_PREFIX) + TOKEN_PREFIX.length());
    }


    public void setValidatorToken(ValidatorToken validatorToken) {
        this.validatorToken = validatorToken;
    }


    public String createToken(Map<String, Object> param, @NotNull String secretKey){
        return createToken(param, null, secretKey);
    }

    public String createToken(Map<String, Object> param, Date expiresAt, @NotNull String secretKey){
        JWTCreator.Builder builder = JWT.create();
        if (param != null){
            param.forEach((k, v) ->{
                if (v instanceof String){
                    builder.withClaim(k, (String) v);
                }else if (v instanceof Boolean){
                    builder.withClaim(k, (Boolean) v);
                }else if (v instanceof Integer){
                    builder.withClaim(k, (Integer) v);
                }else if (v instanceof Long){
                    builder.withClaim(k, (Long) v);
                }else if (v instanceof Double){
                    builder.withClaim(k, (Double) v);
                }else if (v instanceof Date){
                    builder.withClaim(k, (Date) v);
                }
            });
        }
        if (expiresAt != null){
            builder.withExpiresAt(expiresAt);
        }
        return builder.sign(Algorithm.HMAC256(secretKey));
    }

    @Override
    public Date getExpireAt(String token) {
        if (token == null){
            return null;
        }
        return JWT.decode(token).getExpiresAt();
    }
}
