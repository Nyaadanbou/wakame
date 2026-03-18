package cc.mewcraft.wakame.monetization.zpay

/**
 * Z-PAY 签名工具接口.
 *
 * 负责按照 Z-PAY 的 MD5 签名算法生成和验证签名.
 *
 * ### 签名规则
 * 1. 将所有参数按参数名 ASCII 码从小到大排序 (a-z).
 *    `sign`, `sign_type`, 以及空值参数不参与签名.
 * 2. 将排序后的参数拼接成 URL 键值对格式: `a=b&c=d&e=f`.
 *    参数值不进行 URL 编码.
 * 3. 将拼接好的字符串末尾直接拼接商户密钥 KEY, 然后 MD5 加密 (小写).
 *    `sign = md5("a=b&c=d&e=f" + KEY)`
 */
interface ZPaySignature {

    /**
     * 根据参数生成签名.
     *
     * @param params 参与签名的参数 (不含 sign, sign_type)
     * @return MD5 签名字符串 (小写)
     */
    fun generateSign(params: Map<String, String>): String

    /**
     * 验证签名是否正确.
     *
     * @param params 参与签名的参数 (不含 sign, sign_type)
     * @param sign 待验证的签名
     * @return 签名是否匹配
     */
    fun verifySign(params: Map<String, String>, sign: String): Boolean
}
