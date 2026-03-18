# ZPAY 支付 API 文档

> 基于 ZPAY 官方开发文档整理，兼容**易支付**接口规范。

## 目录

- [基本信息](#基本信息)
- [1. 页面跳转支付](#1-页面跳转支付)
- [2. API 接口支付](#2-api-接口支付)
- [3. 查询单个订单](#3-查询单个订单)
- [4. 提交订单退款](#4-提交订单退款)
- [5. 支付结果通知（回调）](#5-支付结果通知回调)
- [6. MD5 签名算法](#6-md5-签名算法)
- [附录：Java 签名示例代码](#附录java-签名示例代码)

---

## 基本信息

|项目|值|
|-|-|
|接口基地址|`https://zpayz.cn/`|
|签名方式|MD5|
|支持支付方式|支付宝 (`alipay`)、微信支付 (`wxpay`)|

> **提示**：如果你的网站已经集成了 **易支付接口**，可以直接使用该 API 信息，无需另外开发。

---

## 1. 页面跳转支付

适用于用户在前端浏览器直接发起支付，通过 form 表单跳转或拼接 URL 跳转至收银台。

### 请求

|项目|值|
|-|-|
|URL|`https://zpayz.cn/submit.php`|
|Method|`POST` 或 `GET`（推荐 POST，不易被劫持或屏蔽）|

### 请求参数

|参数|名称|类型|必填|描述|范例|
|-|-|-|-|-|-|
|`pid`|商户唯一标识|String|✅|一串字母数字组合|`201901151314084206659771`|
|`type`|支付方式|String|✅|`alipay`(支付宝) / `wxpay`(微信支付)|`alipay`|
|`out_trade_no`|商户订单号|Num|✅|每个商品不可重复，最多 32 位|`201911914837526544601`|
|`notify_url`|异步通知页面|String|✅|交易信息回调页面，**不支持带参数**|`http://www.aaa.com/bbb.php`|
|`return_url`|跳转页面|String|✅|交易完成后浏览器跳转，**不支持带参数**|`http://www.aaa.com/ccc.php`|
|`name`|商品名称|String|✅|需体现出具体售卖的商品，否则容易被封|`iPhone17苹果手机`|
|`money`|订单金额|String|✅|最多保留两位小数|`5.67`|
|`sign`|签名|String|✅|MD5 签名，参考 [签名算法](#6-md5-签名算法)|`28f9583617d9caf66834292b6ab1cc89`|
|`sign_type`|签名方法|String|✅|固定值 `MD5`|`MD5`|
|`cid`|支付渠道 ID|String|❌|支持填写多个，用 `,` 隔开；不填则随机调用|`1234`|
|`param`|附加内容|String|❌|会通过 `notify_url` 原样返回|`金色 256G`|

### 响应

- **成功**：直接跳转到付款页面（收银台），访问该 URL 即可进行付款。
- **失败**：返回 JSON

```json
{
  "code": "error",
  "msg": "具体的错误信息"
}
```

### 用法举例

```
https://zpayz.cn/submit.php?name=iphone+xs+Max+一台&money=0.03&out_trade_no=201911914837526544601&notify_url=http://www.aaa.com/notify_url.php&pid=201901151314084206659771&param=金色+256G&return_url=http://www.baidu.com&sign=28f9583617d9caf66834292b6ab1cc89&sign_type=MD5&type=alipay
```

---

## 2. API 接口支付

适用于服务端对接，获取支付链接或二维码后自行展示给用户。

### 请求

|项目|值|
|-|-|
|URL|`https://zpayz.cn/mapi.php`|
|Method|`POST`（form-data）|

### 请求参数

|参数|名称|类型|必填|描述|范例|
|-|-|-|-|-|-|
|`pid`|商户 ID|String|✅||`1001`|
|`type`|支付方式|String|✅|`alipay`(支付宝) / `wxpay`(微信支付)|`alipay`|
|`out_trade_no`|商户订单号|String|✅|每个商品不可重复，最多 32 位|`20160806151343349`|
|`notify_url`|异步通知地址|String|✅|服务器异步通知地址|`http://www.pay.com/notify_url.php`|
|`name`|商品名称|String|✅|需体现出具体售卖的商品，否则容易被封|`iPhone17苹果手机`|
|`money`|商品金额|String|✅|单位：元，最大 2 位小数|`1.00`|
|`clientip`|用户 IP 地址|String|✅|用户发起支付的 IP 地址|`192.168.1.100`|
|`sign`|签名字符串|String|✅|签名算法参考 [签名算法](#6-md5-签名算法)|`202cb962ac59075b964b07152d234b70`|
|`sign_type`|签名类型|String|✅|固定值 `MD5`|`MD5`|
|`cid`|支付渠道 ID|String|❌|支持填写多个，用 `,` 隔开；不填则随机调用|`1234`|
|`device`|设备类型|String|❌|根据用户 UA 判断设备类型，默认 `pc`|`pc`|
|`param`|业务扩展参数|String|❌|支付后原样返回|（留空）|

### 成功响应

|字段名|变量名|类型|示例值|描述|
|-|-|-|-|-|
|返回状态码|`code`|Int|`1`|`1` 为成功，其他值为失败|
|返回信息|`msg`|String||失败时返回原因|
|订单号|`trade_no`|String|`20160806151343349`|支付订单号|
|ZPAY 内部订单号|`O_id`|String|`123456`|ZPAY 内部订单号|
|支付跳转 URL|`payurl`|String|`https://xxx.cn/pay/wxpay/202010903/`|若返回该字段，则直接跳转到该 URL 支付|
|二维码链接|`qrcode`|String|`https://xxx.cn/pay/wxpay/202010903/`|若返回该字段，则根据该 URL 生成二维码|
|二维码图片|`img`|String|`https://zpayz.cn/qrcode/123.jpg`|付款二维码的图片地址|

### 失败响应

```json
{
  "code": "error",
  "msg": "具体的错误信息"
}
```

---

## 3. 查询单个订单

### 请求

|项目|值|
|-|-|
|URL|`https://zpayz.cn/api.php?act=order&pid={商户ID}&key={商户密钥}&out_trade_no={商户订单号}`|
|Method|`GET`|

### 请求参数

|参数|名称|类型|必填|描述|范例|
|-|-|-|-|-|-|
|`act`|操作类型|String|✅|固定值 `order`|`order`|
|`pid`|商户 ID|String|✅||`20220715225121`|
|`key`|商户密钥|String|✅||`89unJUB8HZ54Hj7x4nUj56HN4nUzUJ8i`|
|`trade_no`|系统订单号|String|二选一|ZPAY 系统订单号|`20160806151343312`|
|`out_trade_no`|商户订单号|String|二选一|商户系统内部订单号|`20160806151343349`|

> `trade_no` 和 `out_trade_no` 至少填写一个。

### 成功响应

|字段名|变量名|类型|示例值|描述|
|-|-|-|-|-|
|返回状态码|`code`|Int|`1`|`1` 为成功，其他值为失败|
|返回信息|`msg`|String|`查询订单号成功！`||
|易支付订单号|`trade_no`|String|`2016080622555342651`|易支付订单号|
|商户订单号|`out_trade_no`|String|`20160806151343349`|商户系统内部的订单号|
|支付方式|`type`|String|`alipay`|`alipay` / `wxpay`|
|商户 ID|`pid`|String|`20220715225121`|发起支付的商户 ID|
|创建订单时间|`addtime`|String|`2016-08-06 22:55:52`||
|完成交易时间|`endtime`|String|`2016-08-06 22:55:52`||
|商品名称|`name`|String|`VIP会员`||
|商品金额|`money`|String|`1.00`||
|支付状态|`status`|Int|`0`|`1` 为支付成功，`0` 为未支付|
|业务扩展参数|`param`|String||默认留空|
|支付者账号|`buyer`|String||默认留空|

---

## 4. 提交订单退款

### 请求

|项目|值|
|-|-|
|URL|`https://zpayz.cn/api.php?act=refund`|
|Method|`POST`|

### 请求参数

|参数|名称|类型|必填|描述|范例|
|-|-|-|-|-|-|
|`pid`|商户 ID|String|✅||`20220715225121`|
|`key`|商户密钥|String|✅||`89unJUB8HZ54Hj7x4nUj56HN4nUzUJ8i`|
|`trade_no`|易支付订单号|String|二选一|ZPAY 系统订单号|`20160806151343349021`|
|`out_trade_no`|商户订单号|String|二选一|订单支付时传入的商户订单号|`20160806151343349`|
|`money`|退款金额|String|✅|大多数通道需要与原订单金额一致|`1.50`|

> `trade_no` 和 `out_trade_no` 至少填写一个。

### 响应

|字段名|变量名|类型|示例值|描述|
|-|-|-|-|-|
|返回状态码|`code`|Int|`1`|`1` 为成功，其他值为失败|
|返回信息|`msg`|String|`退款成功`||

---

## 5. 支付结果通知（回调）

支付完成后，ZPAY 会向商户的 `notify_url`（异步通知）和 `return_url`（页面跳转通知）发送支付结果。

### 请求方式

`GET`

### 通知参数

|参数|名称|类型|描述|范例|
|-|-|-|-|-|
|`pid`|商户 ID|Int||`201901151314084206659771`|
|`trade_no`|易支付订单号|String|易支付订单号|`2019011922001418111011411195`|
|`out_trade_no`|商户订单号|Num|商户系统内部的订单号|`201901191324552185692680`|
|`type`|支付方式|String|`alipay` / `wxpay`|`alipay`|
|`name`|商品名称|String|商品名称不超过 100 字|`iphone`|
|`money`|订单金额|String|最多保留两位小数|`5.67`|
|`trade_status`|支付状态|String|只有 `TRADE_SUCCESS` 表示成功|`TRADE_SUCCESS`|
|`param`|业务扩展参数|String|会通过 `notify_url` 原样返回|`金色 256G`|
|`sign`|签名|String|用于验证接收信息的正确性|`ef6e3c5c6ff45018e8c82fd66fb056dc`|
|`sign_type`|签名类型|String|默认为 `MD5`|`MD5`|

### 验证方式

使用 [签名算法](#6-md5-签名算法) 生成签名，比对参数中传入的 `sign` 是否一致。一致则说明是官方发送的真实信息。

### ⚠️ 注意事项

1. 收到回调信息后 **必须返回纯字符串 `success`**，否则平台将判定回调未成功通知到商户。
2. 同样的通知可能会**多次发送**，商户系统必须能够正确处理重复通知。
3. 建议先检查业务数据状态，判断通知是否已处理，未处理再进行业务处理；处理前使用 **数据锁** 做并发控制，避免函数重入导致数据混乱。
4. **必须做签名验证**，并校验返回的订单金额是否与商户侧订单金额一致，防止 "假通知" 造成资金损失。
5. 重试策略：如果平台未收到 `success` 或超过 5 秒未应答，将按以下间隔重试通知：
   - `0s → 15s → 15s → 30s → 180s → 1800s → 1800s → 1800s → 1800s → 3600s`
   - 不保证最终一定通知成功。

---

## 6. MD5 签名算法

### 签名步骤

1. **排序**：将所有参数按参数名 ASCII 码从小到大排序（a-z）。`sign`、`sign_type` 以及 **空值参数** 不参与签名。
2. **拼接**：将排序后的参数拼接成 URL 键值对格式，例如 `a=b&c=d&e=f`。参数值 **不要进行 URL 编码**。
3. **加密**：将拼接好的字符串末尾拼接商户密钥 `KEY`，然后进行 MD5 加密（结果为 **小写**）。

```
sign = md5( "a=b&c=d&e=f" + KEY )
```

> 其中 `+` 为字符串拼接操作，不是字面字符。

### 签名示例（页面跳转支付）

参与签名的参数（排序后）：

```
money=5.67&name=iPhone17苹果手机&notify_url=http://www.aaa.com/bbb.php&out_trade_no=201911914837526544601&pid=201901151314084206659771&return_url=http://www.aaa.com/ccc.php&type=alipay
```

拼接密钥后 MD5：

```
sign = md5("money=5.67&name=iPhone17苹果手机&notify_url=http://www.aaa.com/bbb.php&out_trade_no=201911914837526544601&pid=201901151314084206659771&return_url=http://www.aaa.com/ccc.php&type=alipay" + KEY)
```

---

## 附录：Java 签名示例代码

```java
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ZPaySignExample {

    public static void main(String[] args) {
        String url = "https://z-pay.cn";       // 支付地址
        String pid = "";                        // 商户 ID
        String type = "";                       // 支付类型 (alipay / wxpay)
        String outTradeNo = "";                 // 商户订单号
        String notifyUrl = "";                  // 异步通知地址
        String returnUrl = "";                  // 跳转地址
        String name = "";                       // 商品名称
        String money = "";                      // 价格
        String signType = "MD5";                // 签名类型
        String key = "";                        // 商户密钥

        // 1. 参数存入 Map
        Map<String, String> sign = new HashMap<>();
        sign.put("pid", pid);
        sign.put("type", type);
        sign.put("out_trade_no", outTradeNo);
        sign.put("notify_url", notifyUrl);
        sign.put("return_url", returnUrl);
        sign.put("name", name);
        sign.put("money", money);

        // 2. 根据 key 升序排序
        sign = sortByKey(sign);

        // 3. 遍历 Map 拼接成字符串
        StringBuilder signStr = new StringBuilder();
        for (Map.Entry<String, String> m : sign.entrySet()) {
            signStr.append(m.getKey()).append("=").append(m.getValue()).append("&");
        }

        // 4. 去掉最后一个 &
        signStr.deleteCharAt(signStr.length() - 1);

        // 5. 拼接商户密钥
        signStr.append(key);

        // 6. MD5 加密
        String signValue = DigestUtils.md5DigestAsHex(signStr.toString().getBytes());

        // 7. 附上 sign 和 sign_type
        sign.put("sign_type", signType);
        sign.put("sign", signValue);

        // 8. 生成 form 表单提交
        System.out.println("<form id='paying' action='" + url + "/submit.php' method='post'>");
        for (Map.Entry<String, String> m : sign.entrySet()) {
            System.out.println("<input type='hidden' name='" + m.getKey() + "' value='" + m.getValue() + "'/>");
        }
        System.out.println("<input type='submit' value='正在跳转'>");
        System.out.println("</form>");
        System.out.println("<script>document.forms['paying'].submit();</script>");
    }

    /**
     * 按 key 升序排序 Map
     */
    public static <K extends Comparable<? super K>, V> Map<K, V> sortByKey(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }
}
```

---

## API 接口一览

|接口|URL|方法|用途|
|-|-|-|-|
|页面跳转支付|`https://zpayz.cn/submit.php`|POST / GET|前端跳转收银台支付|
|API 接口支付|`https://zpayz.cn/mapi.php`|POST|服务端获取支付链接|
|查询订单|`https://zpayz.cn/api.php?act=order`|GET|查询单个订单详情|
|订单退款|`https://zpayz.cn/api.php?act=refund`|POST|提交订单退款|
