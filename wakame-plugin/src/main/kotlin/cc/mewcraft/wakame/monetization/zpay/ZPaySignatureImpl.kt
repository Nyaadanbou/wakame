package cc.mewcraft.wakame.monetization.zpay

import java.security.MessageDigest

/**
 * [ZPaySignature] 的 MD5 实现.
 *
 * @param key 商户密钥 (PKEY)
 */
class ZPaySignatureImpl(
    private val key: String,
) : ZPaySignature {

    override fun generateSign(params: Map<String, String>): String {
        val sortedStr = params
            .filter { (k, v) -> k != "sign" && k != "sign_type" && v.isNotEmpty() }
            .toSortedMap()
            .entries
            .joinToString("&") { (k, v) -> "$k=$v" }

        // 拼接商户密钥后 MD5
        return md5(sortedStr + key)
    }

    override fun verifySign(params: Map<String, String>, sign: String): Boolean {
        return generateSign(params).equals(sign, ignoreCase = true)
    }

    private fun md5(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}

