package com.sqmusicplus.v3.plug.qq.hander;

import com.alibaba.fastjson2.JSONObject;
import com.sqmusicplus.v3.plug.qq.entity.QQMusicCookieInfo;
import com.sqmusicplus.v3.plug.qq.entity.QQSearchEntity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QQSearchClientTest {
    @Test void upstreamRejectionIsNotAnEmptyResult() {
        var error = assertThrows(IllegalStateException.class, () -> QQSearchClient.validate(
                "{\"code\":0,\"req\":{\"code\":2001,\"data\":{\"code\":0,\"body\":{\"song\":{\"list\":[]}}}}}", "0"));
        assertTrue(error.getMessage().contains("2001"));
        assertTrue(error.getMessage().contains("登录"));
    }
    @Test void emptyResultsAreOnlyAcceptedWithSuccessAndCorrectStructure() {
        for (String type : new String[]{"0", "1", "2"}) {
            String key = type.equals("0") ? "song" : type.equals("1") ? "singer" : "album";
            String success = "{\"code\":0,\"req\":{\"code\":0,\"data\":{\"code\":0,\"body\":{\"" + key + "\":{\"list\":[]}}}}}";
            assertNotNull(QQSearchClient.validate(success, type));
            assertThrows(IllegalStateException.class, () -> QQSearchClient.validate(success.replace("[]", "\"\""), type));
        }
        assertThrows(IllegalStateException.class, () -> QQSearchClient.validate("{\"code\":500}", "0"));
        assertThrows(IllegalStateException.class, () -> QQSearchClient.validate("<html>error</html>", "0"));
    }
    @Test void savedAccountIsUsedAndKeywordRemainsEscaped() {
        String keyword = "独角戏\"\\测试";
        String payload = new QQSearchEntity().searchRequestParam(keyword, "0", 0, 100);
        var login = new QQMusicCookieInfo().setMusicid("123").setMusickey("test-key").setLoginType(2L);
        var authenticated = JSONObject.parseObject(QQSearchClient.withLogin(payload, login));
        assertEquals("123", authenticated.getJSONObject("comm").getString("uin"));
        assertEquals("test-key", authenticated.getJSONObject("comm").getString("authst"));
        var params = authenticated.getJSONObject("req").getJSONObject("param");
        assertEquals(keyword, params.getString("query"));
        assertEquals(1, params.getIntValue("page_num"));
        assertEquals(50, params.getIntValue("num_per_page"));
        assertFalse(JSONObject.parseObject(QQSearchClient.withLogin(payload, null)).getJSONObject("comm").containsKey("authst"));
    }
}
