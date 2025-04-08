## 资源包分发服务器的正确用法

如果想使用萌芽内置的 HTTP 服务器分发资源包 (`self_host`), 必须添加以下变量到服务端启动参数的 `-jar` 之前:

```
-Djava.net.preferIPv4Stack=true
```
