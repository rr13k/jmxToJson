package com.company.jmxToJSON;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONAware;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.dom4j.*;
import org.dom4j.io.SAXReader;



import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class jmxParser {
    String _file;
    String _outFileType;


    jmxParser(String file, String outFileType ){
        _file = file;
        _outFileType = outFileType;
    }

    public String getFileSpliText(File file) {
        String AbsolutePath = file.getAbsolutePath();
        if ((AbsolutePath != null) && (AbsolutePath.length() > 0)) {
            int dot = AbsolutePath.lastIndexOf('.');
            if ((dot >-1) && (dot < (AbsolutePath.length()))) {
                return AbsolutePath.substring(0, dot);
            }
        }
        return AbsolutePath;
    }

    public void Start() throws Exception {
        File jmxFile = new File(_file);
        if (jmxFile.exists()) {
            String filePath = getFileSpliText(jmxFile);
            String output_file = filePath + "." + _outFileType;
            System.out.println("alter satrt!");
            String grain = makeOutfile(jmxFile);
            File file = new File(output_file);
            Writer out = new FileWriter(file);
            out.write(grain);
            out.close();
        } else {
            throw new Exception("not find file in Path!");
        }
    }

    public String makeOutfile(File jmxFile){
        SAXReader reader = new SAXReader();
        try {
            Document document = reader.read(jmxFile);
            Element root = document.getRootElement();
            JSONArray  jsonFile =  new JSONArray();
            JSONObject config =  createConfig(root);
            jsonFile.add(config);
            List<Element> threadGroups = root.selectNodes("//ThreadGroup");
            for( Element threadGroup : threadGroups) {
                Element httpGroup = (Element) threadGroup.selectNodes("following::hashTree[1]").get(0);
                Element defaultHeader = httpGroup.element("HeaderManager");
                Element defaultRequest = httpGroup.element("ConfigTestElement");
                List<Element>  HTTPSamplers = httpGroup.selectNodes(".//HTTPSamplerProxy");
                for( Element httpSampler : HTTPSamplers){
                    httpJson _httpJson = new httpJson();
                    _httpJson.name = httpSampler.attributeValue("testname");
                    _httpJson.variables = new JSONObject();
                    _httpJson.request.json = getRequestJson(httpSampler);
                    _httpJson.request.method = GetChildElementByAttribute(httpSampler,"stringProp[@name=\"HTTPSampler.method\"]").getText();
                    _httpJson.request.url = getUrl(httpSampler,defaultRequest,_httpJson);

                    Element HeaderManagers = GetChildElementByAttribute(httpSampler,"following::hashTree[1]/HeaderManager");
                    if(HeaderManagers == null) HeaderManagers = defaultHeader;
                    if(HeaderManagers != null){
                        List<Element>  Headers = HeaderManagers.element("collectionProp").elements("elementProp");
                        for(Element header : Headers){
                            String key = GetChildElementByAttribute(header,"stringProp[@name=\"Header.name\"]").getText();
                            String value = GetChildElementByAttribute(header,"stringProp[@name=\"Header.value\"]").getText();
                            _httpJson.request.headers.put(key,value);
                        }
                    }
                    JSONObject res = new JSONObject();
                    res.put("test",_httpJson);
                    jsonFile.add(res);
                }
            }
            return JSON.toJSONString(jsonFile, SerializerFeature.PrettyFormat);
        } catch (DocumentException e) {
            e.printStackTrace();
            return "程序错误，你可能使用的非标准的JMX文件格式,请检查!";
        }
    }

    private JSONObject getRequestJson(Element httpSampler) {
        Element elementProp =  httpSampler.element("elementProp");
        Attribute argumentType = elementProp.attribute("guiclass");
        Element collectionProp =  elementProp.element("collectionProp");
        List<Element>  arguments =  collectionProp.elements("elementProp");
        JSONObject data = new JSONObject();

        for(Element argument : arguments){
            if(argumentType == null){
                String value =  argument.selectSingleNode("stringProp[@name='Argument.value']").getText();
                data  = (JSONObject) JSON.parse(value);
            }else {
                String name =   argument.selectSingleNode("stringProp[@name='Argument.name']").getText();
                String value =  argument.selectSingleNode("stringProp[@name='Argument.value']").getText();
                data.put(name,value);
            }
        }
        return data;
    }

    private String getUrl(Element httpSampler, Element defaultRequest, httpJson httpjson) {
        String httpType = (httpSampler.selectSingleNode("stringProp[@name=\"HTTPSampler.protocol\"]")).getText();
        String domain = (httpSampler.selectSingleNode("stringProp[@name=\"HTTPSampler.domain\"]")).getText();
        String port = (httpSampler.selectSingleNode("stringProp[@name=\"HTTPSampler.port\"]")).getText();
        String path = (httpSampler.selectSingleNode("stringProp[@name=\"HTTPSampler.path\"]")).getText();

        if(httpType.equals("") && defaultRequest != null){
            httpType = (defaultRequest.selectSingleNode("stringProp[@name=\"HTTPSampler.protocol\"]")).getText();
        }

        if(domain.equals("") && defaultRequest != null){
            domain = (defaultRequest.selectSingleNode("stringProp[@name=\"HTTPSampler.domain\"]")).getText();
        }

        if(port.equals("") && defaultRequest != null){
            port = (defaultRequest.selectSingleNode("stringProp[@name=\"HTTPSampler.port\"]")).getText();
        }

        if(path.equals("") && defaultRequest != null){
            path = (defaultRequest.selectSingleNode("stringProp[@name=\"HTTPSampler.path\"]")).getText();
        }
        httpType = httpType.equals("") ? "" : httpType + "://";
        port = port.equals("") ? "" : ":" + port;
        String url= httpType + domain + port + path;

        if(httpjson.request.method.equals("GET") && !httpjson.request.json.isEmpty()){
            String paramesStr = jsonToParames(httpjson.request.json);
            paramesStr = url.indexOf("?") == -1 ? "?" +  paramesStr : "&" + paramesStr;
            url  = url + paramesStr;
            httpjson.request.json = new JSONObject();
        }
        return  url;
    }

    private String jsonToParames(JSONObject json) {
        String str = JSON.toJSONString(json);
        str = str.replace("\"","");
        str = str.replace(":","=");
        str = str.replace(",","&");
        str = str.replace("{","");
        str = str.replace("}","");
        return  str;
    }

    public Element  GetChildElementByAttribute(Element dom ,String xpath){
        List<Element> list =  dom.selectNodes(xpath);
        Element data = list.size() == 0  ? null : list.get(0);
        return  data;
    }

    public JSONObject createConfig(Element root){
        JSONObject config = new JSONObject();
        String testname = ((Element) root.selectSingleNode("//TestPlan")).attributeValue("testname");
        config.put("name",testname);
        config.put("variables",new JSONObject());
        JSONObject res = new JSONObject();
        res.put("config",config);
        return  res;
    }

}
