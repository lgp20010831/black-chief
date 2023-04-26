package com.black.core.servlet;

import com.black.core.json.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Map;

public interface TokenResolver {

    /** 是否具有验证的能力 */
    boolean isCapableOfVerification();

    /**
     * 解析 token 字符串,将里面的信息读成map
     * @param token token 字符串, 如果 token 为空,返回空
     * @return 返回 map
     */
    Map<String, Object> parseToken(String token);

    /***
     * 验证 token
     * @param tokenResolver 有效的 token 处理器
     * @throws NoTokenException 如果 token 字符串为空,会抛出异常
     * @throws TokenExpirationException 如果 token 存在过期时间,可能会跑出过期异常
     */
    String validatorToken(TokenResolver tokenResolver, HttpServletRequest request) throws NoTokenException, TokenExpirationException;

    /***
     * 解析出 token 里的指定 key 对应的值
     * @param token 有效 token,如果为空,则返回空
     * @param key key
     * @return value
     */
    Object tokenAs(String token, String key);

    /***
     * 解析请求实例中的 token
     * @param servletRequest spring mvc 请求实例
     * @return 返回 token 字符串
     * @throws NoTokenException 当没有 token 时抛出的异常
     */
    String getToken(HttpServletRequest servletRequest) throws NoTokenException;

    /** 处理 token 是否有效的逻辑处理 */
    void setValidatorToken(ValidatorToken validatorToken);

    /***
     * 创建一个 token
     * @param param 要加入到 token 里的值
     * @param secretKey 填充过期时间
     * @return 返回 token 字符串
     */
    String createToken(Map<String, Object> param, @NotNull String secretKey);

    /***
     * 创建一个 token
     * @param param 要加入到 token 里的值
     * @param expiresAt 填充过期时间
     * @param secretKey 密钥, 不能为空
     * @return 返回 token 字符串
     */
    String createToken(Map<String, Object> param, Date expiresAt, @NotNull String secretKey);

    /** 过期过期时间 */
    Date getExpireAt(String token);
}
