package ai.nextbillion.navigation.demo.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author qiuyu
 * @Date 2024/2/2
 **/
public class ErrorMessageUtils {

    public static String getErrorMessage(String errorBody) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(errorBody);
            String errorMsg = jsonObj.getString("msg");
            int errorCode = jsonObj.getInt("status");
            return errorCode + ":" + errorMsg;
        } catch (JSONException e) {
            return "";
        }
    }

}