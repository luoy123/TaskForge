package com.zhq.taskforge.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;

import java.util.HashMap;

/**
 * 用来生成和解析token的
 */

public class JwtUtil {

    private static final String SECRET = "my-very-long-and-random-secret-key-2026";
    private static final long EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; //7天

    private static final JWTSigner SIGNER = JWTSignerUtil.hs256(SECRET.getBytes());

    //playload只是通过base64进行编码，不是进行加密，所以不能放密码和敏感的数据。

    /**
     * 生成token
     * @param userId
     * @param userName
     * @return
     */
    public static String generateToken(Long userId,String userName){
        HashMap<String, Object> playload = new HashMap<>();
        playload.put("userId",userId);
        playload.put("userName",userName);
        playload.put("iat",System.currentTimeMillis());//签发时间
        playload.put("exp",System.currentTimeMillis() + EXPIRE_TIME);//token到期时间

        return JWTUtil.createToken(playload,SIGNER);
    }

    /**
     * 获取用户名
     */
    public static String getUserName(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String userName = (String)jwt.getPayload("userName");
        return userName;
    }

    /**
     * 获取用户Id
     */
    public static Long getUserId(String token){
        Long userId = (Long)JWTUtil.parseToken(token).getPayload("userId");
        return userId;
    }

    /**
     *  校验token
     */
    public static boolean validateToken(String token){
        if(StrUtil.isEmpty(token)) return false;
        try {
            JWT jwt = JWTUtil.parseToken(token).setSigner(SIGNER);
            if (!jwt.verify()) {
                //签名有问题
                return false;
            }
            Object expObj = jwt.getPayload("exp");
            if (expObj == null) {
                //没有过期时间
                return false;
            }
            long exp;
            if (expObj instanceof Number number) {
                exp = number.longValue();
            } else {
                exp = Long.parseLong(expObj.toString());
            }
            return exp > System.currentTimeMillis();
        }catch(Exception e){
            return false;
        }
    }

}
