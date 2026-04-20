package io.github.timemachinelab.util;

import okhttp3.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP工具类，基于OkHttp封装
 * 
 * <p>
 * 提供HTTP请求的发送功能，支持GET、POST、PUT、DELETE等常用方法，
 * 同时支持代理配置和自定义请求头。
 * </p>
 * 
 * <pre>
 * // 简单GET请求
 * String response = HttpUtils.get("https://api.example.com/data");
 * 
 * // 带代理的POST请求
 * HttpUtils.setProxyConfig(new HttpUtils.ProxyConfig("127.0.0.1", 8080));
 * String response = HttpUtils.post("https://api.example.com/create", jsonBody);
 * 
 * // 清除代理配置
 * HttpUtils.clearProxyConfig();
 * </pre>
 * 
 * @author TimeMachineLab
 * @since 1.0
 */
public class HttpUtils {

    private static final int DEFAULT_TIMEOUT = 60;
    private static final String DEFAULT_CHARSET = "UTF-8";

    // 默认客户端和代理客户端
    private static final OkHttpClient DEFAULT_CLIENT;
    private static volatile OkHttpClient PROXY_CLIENT;

    // 代理配置
    private static volatile ProxyConfig proxyConfig;

    static {
        DEFAULT_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    /**
     * 代理配置类
     * 
     * <p>
     * 用于配置HTTP代理服务器信息，支持基本认证。
     * </p>
     * 
     * <pre>
     * // 简单代理配置
     * ProxyConfig config = new ProxyConfig("127.0.0.1", 8080);
     * 
     * // 带认证的代理配置
     * ProxyConfig config = new ProxyConfig("127.0.0.1", 8080, "username", "password");
     * </pre>
     * 
     * @since 1.0
     */
    public static class ProxyConfig {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final Proxy.Type type;

        /**
         * 创建简单代理配置（无认证）
         * 
         * @param host 代理服务器主机
         * @param port 代理服务器端口
         */
        public ProxyConfig(String host, int port) {
            this(host, port, null, null, Proxy.Type.HTTP);
        }

        /**
         * 创建带认证的代理配置
         * 
         * @param host     代理服务器主机
         * @param port     代理服务器端口
         * @param username 代理用户名
         * @param password 代理密码
         */
        public ProxyConfig(String host, int port, String username, String password) {
            this(host, port, username, password, Proxy.Type.HTTP);
        }

        /**
         * 创建完整的代理配置
         * 
         * @param host     代理服务器主机
         * @param port     代理服务器端口
         * @param username 代理用户名
         * @param password 代理密码
         * @param type     代理类型
         */
        public ProxyConfig(String host, int port, String username, String password, Proxy.Type type) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.type = type;
        }

        /**
         * 获取代理服务器主机
         * 
         * @return 代理服务器主机
         */
        public String getHost() {
            return host;
        }

        /**
         * 获取代理服务器端口
         * 
         * @return 代理服务器端口
         */
        public int getPort() {
            return port;
        }

        /**
         * 获取代理用户名
         * 
         * @return 代理用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 获取代理密码
         * 
         * @return 代理密码
         */
        public String getPassword() {
            return password;
        }

        /**
         * 获取代理类型
         * 
         * @return 代理类型
         */
        public Proxy.Type getType() {
            return type;
        }

        /**
         * 检查是否需要认证
         * 
         * @return 如果需要认证返回true
         */
        public boolean hasAuth() {
            return username != null && !username.isEmpty() && password != null;
        }
    }

    /**
     * 设置代理配置
     * 
     * <p>
     * 设置全局代理配置，之后的所有HTTP请求都会通过代理服务器发送。
     * </p>
     * 
     * <pre>
     * HttpUtils.setProxyConfig(new HttpUtils.ProxyConfig("127.0.0.1", 8080));
     * </pre>
     * 
     * @param config 代理配置，为null时清除代理配置
     */
    public static void setProxyConfig(ProxyConfig config) {
        proxyConfig = config;
        if (config != null) {
            PROXY_CLIENT = createProxyClient(config);
        } else {
            PROXY_CLIENT = null;
        }
    }

    /**
     * 清除代理配置
     * 
     * <p>
     * 清除当前设置的代理配置，之后的HTTP请求将直接发送。
     * </p>
     * 
     * <pre>
     * HttpUtils.clearProxyConfig();
     * </pre>
     */
    public static void clearProxyConfig() {
        proxyConfig = null;
        PROXY_CLIENT = null;
    }

    /**
     * 获取当前代理配置
     * 
     * @return 当前代理配置，如果没有设置返回null
     */
    public static ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /**
     * 创建代理客户端
     * 
     * <p>
     * 根据代理配置创建OkHttpClient实例。
     * </p>
     * 
     * @param config 代理配置
     * @return 配置了代理的OkHttpClient
     */
    private static OkHttpClient createProxyClient(ProxyConfig config) {
        Proxy proxy = new Proxy(config.getType(), new InetSocketAddress(config.getHost(), config.getPort()));

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .followRedirects(true)
                .proxy(proxy);

        // 如果代理需要认证
        if (config.hasAuth()) {
            builder.proxyAuthenticator((route, response) -> {
                String credential = Credentials.basic(config.getUsername(), config.getPassword());
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
        }

        return builder.build();
    }

    /**
     * 获取当前使用的客户端
     * 
     * <p>
     * 根据是否配置了代理返回相应的OkHttpClient。
     * </p>
     * 
     * @return 当前使用的OkHttpClient
     */
    private static OkHttpClient getCurrentClient() {
        return PROXY_CLIENT != null ? PROXY_CLIENT : DEFAULT_CLIENT;
    }

    /**
     * 发送GET请求
     * 
     * <p>
     * 发送简单的GET请求到指定URL。
     * </p>
     * 
     * <pre>
     * String response = HttpUtils.get("https://api.example.com/data");
     * </pre>
     * 
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * 发送GET请求
     * 
     * <p>
     * 发送带自定义请求头的GET请求。
     * </p>
     * 
     * <pre>
     * Map<String, String> headers = new HashMap<>();
     * headers.put("Authorization", "Bearer token123");
     * String response = HttpUtils.get("https://api.example.com/data", headers);
     * </pre>
     * 
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String get(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        Request request = requestBuilder.build();
        try (Response response = getCurrentClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                throw new IOException("HTTP请求失败，状态码: " + response.code());
            }
        }
    }

    /**
     * 发送POST请求
     * 
     * <p>
     * 发送JSON格式的POST请求。
     * </p>
     * 
     * <pre>
     * String jsonBody = "{\"name\":\"test\",\"age\":20}";
     * String response = HttpUtils.post("https://api.example.com/create", jsonBody);
     * </pre>
     * 
     * @param url  请求URL
     * @param body 请求体
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String post(String url, String body) throws IOException {
        return post(url, body, "application/json", null);
    }

    /**
     * 发送POST请求
     * 
     * <p>
     * 发送指定内容类型的POST请求。
     * </p>
     * 
     * <pre>
     * String xmlBody = "<user><name>test</name></user>";
     * String response = HttpUtils.post("https://api.example.com/create", xmlBody, "application/xml");
     * </pre>
     * 
     * @param url         请求URL
     * @param body        请求体
     * @param contentType 内容类型
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String post(String url, String body, String contentType) throws IOException {
        return post(url, body, contentType, null);
    }

    /**
     * 发送POST请求
     * 
     * <p>
     * 发送完整的POST请求，支持自定义内容类型和请求头。
     * </p>
     * 
     * <pre>
     * Map<String, String> headers = new HashMap<>();
     * headers.put("Authorization", "Bearer token123");
     * headers.put("X-Custom-Header", "value");
     * String response = HttpUtils.post("https://api.example.com/create", jsonBody, "application/json", headers);
     * </pre>
     * 
     * @param url         请求URL
     * @param body        请求体
     * @param contentType 内容类型
     * @param headers     请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String post(String url, String body, String contentType, Map<String, String> headers)
            throws IOException {
        RequestBody requestBody = body != null
                ? RequestBody.create(body, MediaType.parse(contentType != null ? contentType : "application/json"))
                : RequestBody.create("", MediaType.parse("application/json"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);

        if (contentType != null) {
            requestBuilder.header("Content-Type", contentType);
        }

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        Request request = requestBuilder.build();
        try (Response response = getCurrentClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                throw new IOException("HTTP请求失败，状态码: " + response.code());
            }
        }
    }

    /**
     * 发送PUT请求
     * 
     * <p>
     * 发送JSON格式的PUT请求。
     * </p>
     * 
     * <pre>
     * String jsonBody = "{\"name\":\"updated\"}";
     * String response = HttpUtils.put("https://api.example.com/update/1", jsonBody);
     * </pre>
     * 
     * @param url  请求URL
     * @param body 请求体
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String put(String url, String body) throws IOException {
        return put(url, body, "application/json", null);
    }

    /**
     * 发送PUT请求
     * 
     * <p>
     * 发送完整的PUT请求，支持自定义内容类型和请求头。
     * </p>
     * 
     * <pre>
     * Map<String, String> headers = new HashMap<>();
     * headers.put("Authorization", "Bearer token123");
     * String response = HttpUtils.put("https://api.example.com/update/1", jsonBody, "application/json", headers);
     * </pre>
     * 
     * @param url         请求URL
     * @param body        请求体
     * @param contentType 内容类型
     * @param headers     请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String put(String url, String body, String contentType, Map<String, String> headers)
            throws IOException {
        RequestBody requestBody = body != null
                ? RequestBody.create(body, MediaType.parse(contentType != null ? contentType : "application/json"))
                : RequestBody.create("", MediaType.parse("application/json"));

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .put(requestBody);

        if (contentType != null) {
            requestBuilder.header("Content-Type", contentType);
        }

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        Request request = requestBuilder.build();
        try (Response response = getCurrentClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                throw new IOException("HTTP请求失败，状态码: " + response.code());
            }
        }
    }

    /**
     * 发送DELETE请求
     * 
     * <p>
     * 发送简单的DELETE请求。
     * </p>
     * 
     * <pre>
     * String response = HttpUtils.delete("https://api.example.com/delete/1");
     * </pre>
     * 
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String delete(String url) throws IOException {
        return delete(url, null);
    }

    /**
     * 发送DELETE请求
     * 
     * <p>
     * 发送带自定义请求头的DELETE请求。
     * </p>
     * 
     * <pre>
     * Map<String, String> headers = new HashMap<>();
     * headers.put("Authorization", "Bearer token123");
     * String response = HttpUtils.delete("https://api.example.com/delete/1", headers);
     * </pre>
     * 
     * @param url     请求URL
     * @param headers 请求头
     * @return 响应内容
     * @throws IOException 如果请求失败
     */
    public static String delete(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .delete();

        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }

        Request request = requestBuilder.build();
        try (Response response = getCurrentClient().newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body() != null ? response.body().string() : "";
            } else {
                throw new IOException("HTTP请求失败，状态码: " + response.code());
            }
        }
    }

    /**
     * 获取默认的OkHttpClient实例
     * 
     * <p>
     * 获取默认的OkHttpClient实例，用于自定义请求。
     * </p>
     * 
     * @return OkHttpClient实例
     */
    public static OkHttpClient getDefaultClient() {
        return DEFAULT_CLIENT;
    }

    /**
     * 创建新的OkHttpClient构建器
     * 
     * <p>
     * 创建新的OkHttpClient构建器，用于自定义客户端配置。
     * </p>
     * 
     * @return OkHttpClient.Builder实例
     */
    public static OkHttpClient.Builder newClientBuilder() {
        return new OkHttpClient.Builder();
    }
}