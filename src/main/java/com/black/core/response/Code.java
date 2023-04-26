package com.black.core.response;

import lombok.AllArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
public class Code implements Serializable {

    Integer code;

    public Integer value(){return code;}

    /** 成功 */
    public static final Code SUCCESS = new Code(200);

    /** token失效 */
    public static final Code TOKEN_INVALID = new Code(401);

    /** 用户不存在 */
    public static final Code USER_NOT_EXIST = new Code(405);

    /** 密码错误 */
    public static final Code PASSWORD_ERROR = new Code(407);

    /** 必需参数存在空值 */
    public static final Code NULL_WARM = new Code(408);

    /** 用户名已存在，注册用户的时候发生 */
    public static final Code REGISTER_AlREADY_EXISTS = new Code(409);

    /** 账号不存在 */
    public static final Code ILLEGAL_ACCOUNT = new Code(411);

    /** 上传文件失败 */
    public static final Code UPLOAD_FILE_FAIL = new Code(302);

    /** 操作失败,系统内部原因 */
    public static final Code HANDLER_FAIL = new Code(500);
}
