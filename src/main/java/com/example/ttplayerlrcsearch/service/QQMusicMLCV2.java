package com.example.ttplayerlrcsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.ttplayerlrcsearch.entity.ApiResponse;
import com.example.ttplayerlrcsearch.entity.BuffData;
import com.example.ttplayerlrcsearch.entity.TTPlayResult;
import com.example.ttplayerlrcsearch.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class QQMusicMLCV2 extends LRCDispose implements MusicLrcSearch {

    private String searchUrl = "https://c.y.qq.com/splcloud/fcgi-bin/smartbox_new.fcg?format=json&inCharset=utf-8&outCharset=utf-8&key=%s";
    private String downloadUrl = "https://c.y.qq.com/lyric/fcgi-bin/fcg_query_lyric_new.fcg?format=json&inCharset=utf-8&outCharset=utf-8&songmid=%s";

    @Override
    public String getSearchName() {
        return "QQ音乐v2";
    }

    @Override
    public void initialization() {}

    @Override
    public List<TTPlayResult> search(String artist, String title) {
        String searchText = String.format("%s %s",title,artist);
        List<TTPlayResult> result = null;
        //查询结果
        String sourse_result = getResult(searchText);
        if(StringUtil.isNull(sourse_result)){
            return null;
        }
        //解析结果
        result = parse(sourse_result);
        return result;
    }

    private String getResult(String text){
        GetRequest request = null;
        try {
            request = Unirest.get(String.format(searchUrl, URLEncoder.encode(text,"UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if(request==null)return null;
        HttpResponse<String> asString = null;
        try {
            long start = System.currentTimeMillis();
            asString = request.asString();
            log.debug("Api Use Time:{} ms",(System.currentTimeMillis() - start)/1000.0);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        if(asString==null)return null;
        String body = asString.getBody();
        if(StringUtil.isNull(body))return null;
        return body;
    }
    private List<TTPlayResult> parse(String sourseData){
        List<TTPlayResult> result = new ArrayList<>();
        if(StringUtil.isNull(sourseData))return result;
        JSONObject object = JSON.parseObject(sourseData);
        if (object!=null && !object.isEmpty() && object.getInteger("code")==0){
            JSONObject dataMap = object.getJSONObject("data");
            if(dataMap!=null){
                JSONObject songs = dataMap.getJSONObject("song");
                if (songs!=null){
//                    Integer count = songs.getInteger("count");
                    JSONArray itemlist = songs.getJSONArray("itemlist");
                    if (itemlist!=null){
                        for (int i = 0; i < itemlist.size(); i++) {
                            JSONObject songItem = itemlist.getJSONObject(i);
                            TTPlayResult ttPlayResult = new TTPlayResult();
                            ttPlayResult.id = BuffData.addId(songItem.getString("mid"),this);
                            ttPlayResult.artist = songItem.getString("singer");
                            ttPlayResult.title = "【"+this.getSearchName()+"】"+songItem.getString("name");
                            result.add(ttPlayResult);
                        }
                    }
                }
            }
        }else{
            log.error("接口返回了意外的数据\r\n{}",sourseData);
        }
        return result;
    }

    @Override
    public ApiResponse<String> download(String musicId) {
        ApiResponse result;

        GetRequest request = Unirest.get(String.format(downloadUrl,musicId))
                .header("referer", "https://y.qq.com/");
        HttpResponse<String> lrcResponse = null;
        try {
            lrcResponse = request.asString();
        } catch (UnirestException e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            return ApiResponse.returnFail(e.getMessage());
        }
        String body = lrcResponse.getBody();
        JSONObject lrcObj = JSON.parseObject(body);
        String sourseLyric = lrcObj.getString("lyric");
        if(StringUtil.isNull(sourseLyric)){
            log.error("该歌曲没有上传歌词！");
        }
        try {
            String souText = new String(Base64.getDecoder().decode(sourseLyric),"UTF-8");
            //xml/html转义处理
            souText = StringEscapeUtils.unescapeXml(souText);
            souText = StringEscapeUtils.unescapeHtml4(souText);

            String transText = null;
            String transLrc = lrcObj.getString("trans");
            if(StringUtil.notEmpty(transLrc)){
                transText = new String(Base64.getDecoder().decode(transLrc),"UTF-8");
                //xml/html转义处理
                transText = StringEscapeUtils.unescapeXml(transText);
                transText = StringEscapeUtils.unescapeHtml4(transText);
            }
            result = ApiResponse.returnOK().setDataNow(doTrans(souText,transText));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            String errMessage = String.format("歌词解析出现异常\r\n%s", sourseLyric);
            log.error(errMessage);
            result = ApiResponse.returnFail(errMessage);
        }
        return result;
    }
}
