import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.hd.wauxv.plugin.api.callback.PluginCallBack;

Map windowMap = new HashMap();
String api = "https://open.bigmodel.cn/api/paas/v4/chat/completions/";
String key="xxxxxx"; 
String model="glm-4-flash-250414";
String robotName="亦心"; //机器人的微信昵称，当群聊里有人艾特这个名称时，才会触发机器人的回复。

void addSystemMsg(String content, String room) {
    if (!windowMap.containsKey(room)) {
       windowMap.put(room, new ArrayList());
   }
    Map msgMap = new HashMap();
    msgMap.put("role", "system");
    msgMap.put("content", content);
    windowMap.get(room).add(msgMap);
}

void addUserMsg(String content, String room) {
    if (!windowMap.containsKey(room)) {
        windowMap.put(room, new ArrayList());
    }
    Map msgMap = new HashMap();
    msgMap.put("role", "user");
    msgMap.put("content", content);
    windowMap.get(room).add(msgMap);
}

Map getOpenAiParam(String content, String room) {
    Map paramMap = new HashMap();
    paramMap.put("model", model);
    addUserMsg(content,room);
    paramMap.put("messages", windowMap.get(room));
    paramMap.put("temperature", 0.7);
    return paramMap;
}

Map getOpenAiHeader(String key) {
    Map headerMap = new HashMap();
    headerMap.put("Content-Type", "application/json");
    headerMap.put("Authorization", "Bearer " + key);
    return headerMap;
}

void sendOpenAiResp(String talker, String content) {
    post(api,
            getOpenAiParam(content, talker),
            getOpenAiHeader(key),
            new PluginCallBack.HttpCallback() {
                public void onSuccess(int code, String content) {
                    JSONObject jsonObj = new JSONObject(content);
                    JSONArray choices = jsonObj.getJSONArray("choices");
                    JSONObject fristJsonObj = choices.getJSONObject(0);
                    JSONObject msgJsonObj = fristJsonObj.getJSONObject("message");
                    String msgContent = msgJsonObj.getString("content");
                    addSystemMsg(msgContent,talker);
                    sendText(talker, "AI回复:" + msgContent);
                }

                public void onError(Exception e) {
                    sendText(talker, "[OpenAi Api]请求异常:" + e.getMessage());
                }
            }
    );
}

void onHandleMsg(Object msgInfoBean) {
    if (msgInfoBean.isSend()) return;
    if (msgInfoBean.isText()) {
        if (msgInfoBean.getTalker().contains("@chatroom") && msgInfoBean.getContent().contains("@"+robotName)) {
            sendOpenAiResp(msgInfoBean.getTalker(), msgInfoBean.getContent().replace("@"+robotName, ""));
        } else if (!msgInfoBean.getTalker().contains("@chatroom")) {
            sendOpenAiResp(msgInfoBean.getTalker(), msgInfoBean.getContent());
        }
    }
}