package org.droidmate.analyzer.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.droidmate.analyzer.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * API identified during exploration
 */
public class Api {
    private static final Logger logger = LoggerFactory.getLogger(Api.class);

    private String className;
    private String methodName;
    private List<String> params;
    private String uri;

    public static Api build(JsonObject jsonObject) {
        String className = jsonObject.get("className").getAsString();
        String methodName = jsonObject.get("methodName").getAsString();
        List<String> paramList = new ArrayList<>();
        for (JsonElement param : jsonObject.get("paramList").getAsJsonArray())
            paramList.add(param.getAsString());

        return Api.build(className, methodName, paramList, "");
    }

    public static Api build(String className, String methodName, String paramStr, String uri) {
        List<String> paramList = new ArrayList<>();

        if (paramStr.contains(",")) {
            String[] paramArr = paramStr.split(",");
            paramList.addAll(Arrays.asList(paramArr));
        } else if (paramStr.length() > 0) {
            paramList.add(paramStr);
        }

        return Api.build(className, methodName, paramList, uri);
    }

    private static Api build(String className, String methodName, List<String> params, String uri) {
        return new Api(className, methodName, params, uri);
    }

    public static String getParamsFromMethodSignature(String methodName) {
        String pattern;
        if (methodName.contains("("))
            pattern = "\\(";
        else
            pattern = "<";
        String[] data = methodName.split(pattern);

        return data[1].replace(")", "");
    }

    public static String getMethodNameFromSignature(String methodSignature) {
        String params = Api.getParamsFromMethodSignature(methodSignature);

        return methodSignature
                .replace(params, "")
                .replace("(", "")
                .replace(")", "");
    }

    private Api(String className, String methodName, List<String> params, String uri) {
        this.className = className;
        this.methodName = methodName;
        this.params = params;
        this.uri = uri;

        if (this.params == null)
            this.params = new ArrayList<>();
    }

    public String getURI() {
        return this.uri;
    }

    public boolean hasRestriction() {
        return this.getRestriction() != null;
    }

    public Api getRestriction() {
        Api restriction = new ResourceManager().getRestriction(this);
        logger.debug(String.format("(%s) => getRestriction: %s", this.toString(), (restriction == null) + ""));

        return restriction;
    }

    public String getURIParamName() {
        for (int i = 0; i < this.params.size(); ++i) {
            String param = this.params.get(i);
            if (param.equals("android.net.Uri"))
                return String.format("p%d", i);
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s", this.className, this.methodName, this.getParamsStr());
    }

    private String getParamsStr() {
        return String.join(",", this.params);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Api))
            return false;

        Api otherApi = (Api) other;

        return this.className.equals(otherApi.className) &&
                this.methodName.equals(otherApi.methodName) &&
                this.params.equals(otherApi.params);
    }
}
