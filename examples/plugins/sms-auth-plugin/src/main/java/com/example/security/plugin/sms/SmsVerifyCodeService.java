package com.example.security.plugin.sms;

/**
 * 短信验证码服务接口。
 * <p>
 * 定义验证码的生成、发送和验证逻辑。
 * 实际项目中需要实现此接口对接短信服务商（如阿里云短信、腾讯云短信等）。
 * </p>
 *
 * <p>实现示例：</p>
 * <pre>{@code
 * @Service
 * public class AliyunSmsVerifyCodeService implements SmsVerifyCodeService {
 *
 *     private final RedisTemplate<String, String> redisTemplate;
 *
 *     @Override
 *     public boolean sendVerifyCode(String phoneNumber) {
 *         String code = generateCode();
 *         // 调用阿里云短信 API 发送验证码
 *         boolean sent = aliyunSmsClient.send(phoneNumber, code);
 *         if (sent) {
 *             // 存储验证码到 Redis，设置过期时间
 *             redisTemplate.opsForValue().set(
 *                 "sms:code:" + phoneNumber, code,
 *                 getExpireSeconds(), TimeUnit.SECONDS);
 *         }
 *         return sent;
 *     }
 *
 *     @Override
 *     public boolean verifyCode(String phoneNumber, String verifyCode) {
 *         String key = "sms:code:" + phoneNumber;
 *         String storedCode = redisTemplate.opsForValue().get(key);
 *         if (verifyCode.equals(storedCode)) {
 *             redisTemplate.delete(key);  // 验证成功后删除
 *             return true;
 *         }
 *         return false;
 *     }
 * }
 * }</pre>
 *
 * @author Example Team
 * @since 1.0.0
 */
public interface SmsVerifyCodeService {

    /**
     * 发送验证码到指定手机号。
     * <p>
     * 实现应包含以下逻辑：
     * <ul>
     *     <li>生成随机验证码</li>
     *     <li>调用短信服务商 API 发送</li>
     *     <li>存储验证码（如 Redis）用于后续验证</li>
     *     <li>限制发送频率防止滥用</li>
     * </ul>
     * </p>
     *
     * @param phoneNumber 手机号（11位中国大陆手机号）
     * @return 是否发送成功
     * @throws IllegalArgumentException 如果手机号格式不正确
     */
    boolean sendVerifyCode(String phoneNumber);

    /**
     * 验证验证码是否正确。
     * <p>
     * 实现应包含以下逻辑：
     * <ul>
     *     <li>从存储中获取验证码</li>
     *     <li>比对验证码是否正确</li>
     *     <li>验证成功后删除验证码（一次性使用）</li>
     *     <li>限制验证尝试次数</li>
     * </ul>
     * </p>
     *
     * @param phoneNumber 手机号
     * @param verifyCode  用户输入的验证码
     * @return 验证是否通过
     */
    boolean verifyCode(String phoneNumber, String verifyCode);

    /**
     * 获取验证码有效期（秒）。
     * <p>
     * 默认 5 分钟，可根据业务需求调整。
     * </p>
     *
     * @return 有效期（秒）
     */
    default int getExpireSeconds() {
        return 300;  // 默认 5 分钟
    }

    /**
     * 获取验证码长度。
     *
     * @return 验证码长度，默认 6 位
     */
    default int getCodeLength() {
        return 6;
    }

    /**
     * 获取最大验证尝试次数。
     *
     * @return 最大尝试次数，默认 5 次
     */
    default int getMaxAttempts() {
        return 5;
    }
}
