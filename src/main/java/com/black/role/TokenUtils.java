package com.black.role;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.black.core.json.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TokenUtils {


    public static Date getExpireAt(String token) {
        if (token == null){
            return null;
        }
        return JWT.decode(token).getExpiresAt();
    }


    public static boolean isExpire(Date time){
        return System.currentTimeMillis() > time.getTime();
    }

    public static Map<String, Object> parseToken(String token){
        if (token == null)
            return null;
        DecodedJWT decode = JWT.decode(token);
        Map<String, Claim> claims = decode.getClaims();
        Map<String, Object> resultMap = new HashMap<>();
        claims.forEach((k, c) -> resultMap.put(k, c.as(Object.class)));
        return resultMap;
    }

    public static Date createDate(int unit, int val){
        Calendar calendar = Calendar.getInstance();
        calendar.add(unit, val);
        return calendar.getTime();
    }

    public static String createToken(Map<String, Object> param, @NotNull String secretKey){
        return createToken(param, null, secretKey);
    }

    public static String createToken(Map<String, Object> param, Date expiresAt, @NotNull String secretKey){
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
                }else {
                    if (v != null){
                        builder.withClaim(k, v.toString());
                    }
                }
            });
        }
        if (expiresAt != null){
            builder.withExpiresAt(expiresAt);
        }
        return builder.sign(Algorithm.HMAC256(secretKey));
    }

}
