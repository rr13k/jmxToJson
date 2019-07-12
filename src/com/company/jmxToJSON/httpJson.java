package com.company.jmxToJSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.List;

public class httpJson {

    httpJson(){
        this.request = new Request();
        this.request.headers = new JSONObject();
        this.request.json = new JSONObject();
        this.extract = new JSONArray();
    }

    String name;
    Request request;
    List<Eq> validate;
    JSONArray extract;
    JSONObject variables;

    public class Request {
        String url;
        String method;
        JSONObject headers;
        JSONObject json;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public JSONObject getHeaders() {
            return headers;
        }

        public void setHeaders(JSONObject headers) {
            this.headers = headers;
        }

        public JSONObject getJson() {
            return json;
        }

        public void setJson(JSONObject json) {
            this.json = json;
        }
    }

    class Eq{
         JSONArray eq;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public List<Eq> getValidate() {
        return validate;
    }

    public void setValidate(List<Eq> validate) {
        this.validate = validate;
    }

    public JSONArray getExtract() {
        return extract;
    }

    public void setExtract(JSONArray extract) {
        this.extract = extract;
    }

    public JSONObject getVariables() {
        return variables;
    }

    public void setVariables(JSONObject variables) {
        this.variables = variables;
    }
}
