package com.sqmusicplus.v3.plug.qq.hander;

import com.alibaba.fastjson2.JSONObject;
import com.sqmusicplus.v3.plug.qq.entity.QQMusicCookieInfo;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Search-only client: bounded requests, no implicit cookies or silent error-to-empty conversion. */
final class QQSearchClient {
    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS).build();

    static String withLogin(String payload, QQMusicCookieInfo login) {
        JSONObject request = JSONObject.parseObject(payload);
        if (login != null && login.getMusicid() != null && login.getMusickey() != null
                && !login.getMusicid().isBlank() && !login.getMusickey().isBlank()) {
            JSONObject comm = request.getJSONObject("comm");
            comm.put("uin", login.getMusicid());
            comm.put("qq", login.getMusicid());
            comm.put("authst", login.getMusickey());
            comm.put("tmeLoginType", login.getLoginType() == null ? "2" : login.getLoginType().toString());
        }
        return request.toJSONString();
    }

    static JSONObject search(String url, String payload, String type) {
        Request request = new Request.Builder().url(url)
                .header("Accept", "application/json")
                .header("Referer", "https://y.qq.com/")
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), payload)).build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IllegalStateException("QQ 搜索接口请求失败（HTTP " + response.code() + "），请稍后重试");
            }
            return validate(response.body().string(), type);
        } catch (IOException e) {
            throw new IllegalStateException("QQ 搜索连接失败或超时，请检查网络后重试", e);
        }
    }

    static JSONObject validate(String response, String type) {
        JSONObject result;
        try {
            result = JSONObject.parseObject(response);
        } catch (RuntimeException e) {
            throw new IllegalStateException("QQ 搜索接口返回了非 JSON 数据，请稍后重试");
        }
        if (result == null) throw new IllegalStateException("QQ 搜索接口返回为空");
        checkCode(result, "接口");
        JSONObject req = result.getJSONObject("req");
        if (req == null) throw new IllegalStateException("QQ 搜索响应缺少 req");
        checkCode(req, "搜索");
        JSONObject data = req.getJSONObject("data");
        if (data == null) throw new IllegalStateException("QQ 搜索响应缺少 data");
        checkCode(data, "数据");
        JSONObject body = data.getJSONObject("body");
        String key = switch (type) { case "0" -> "song"; case "1" -> "singer"; case "2" -> "album";
            default -> throw new IllegalArgumentException("不支持的 QQ 搜索类型"); };
        JSONObject section = body == null ? null : body.getJSONObject(key);
        if (section == null || !(section.get("list") instanceof com.alibaba.fastjson2.JSONArray)) {
            throw new IllegalStateException("QQ 搜索返回的数据结构异常，请稍后重试");
        }
        return result;
    }

    private static void checkCode(JSONObject value, String stage) {
        Integer code = value.getInteger("code");
        if (code == null) throw new IllegalStateException("QQ 搜索" + stage + "响应缺少状态码");
        if (code == 2001) {
            throw new IllegalStateException("QQ 搜索被上游拒绝（2001），请在设置中登录或重新登录 QQ 音乐后重试");
        }
        if (code != 0) throw new IllegalStateException("QQ 搜索" + stage + "异常（" + code + "），请稍后重试");
    }
}
