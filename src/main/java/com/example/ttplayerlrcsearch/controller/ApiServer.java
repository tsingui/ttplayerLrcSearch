package com.example.ttplayerlrcsearch.controller;

import com.alibaba.fastjson.JSON;
import com.example.ttplayerlrcsearch.entity.ApiResponse;
import com.example.ttplayerlrcsearch.entity.BuffData;
import com.example.ttplayerlrcsearch.entity.BuffDataEntity;
import com.example.ttplayerlrcsearch.service.FindAllMLC;
import com.example.ttplayerlrcsearch.service.MusicLrcSearch;
import com.example.ttplayerlrcsearch.util.StringUtil;
import com.example.ttplayerlrcsearch.util.TtplayTextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping("api")
@Slf4j
public class ApiServer {
    @Resource
    private FindAllMLC fac;

    @RequestMapping("search")
    @ResponseBody
    public String search(HttpServletRequest request){
        if(!isInitSuccess())return "引擎未初始成功！";

        String resultData = "";

//        System.out.println(showRequest(request));

        Map<String, String> parameter = getParameter(request);
        String title = parameter.get("Title");
        String artist = parameter.get("sh?Artist");

        String lrcId = parameter.get("dl?Id");
        if(StringUtil.isNull(lrcId)){
            //搜索模式
            List<Map<String, String>> data = searchLrc(artist, title);
            //生成返回数据
            resultData = serarchParse(data);
        }else{
            //下载模式
            log.info(JSON.toJSONString(parameter));
            BuffDataEntity entity = BuffData.findId(lrcId);
            if(entity==null){
                log.info("查询出现问题！");
            }else{
                MusicLrcSearch mlc = entity.getMlc();
                log.info("歌曲id：{},来源为：{}",entity.getSouId(), mlc.getSearchName());
                ApiResponse<String> downloadResponse = mlc.download(entity.getSouId());
                if(downloadResponse.isSuccess()){
                    resultData = downloadResponse.getData();
                    log.info("下载成功！歌词大小为 {}",StringUtil.formatSize(resultData.getBytes(StandardCharsets.UTF_8).length));
                }else{
                    log.info("下载失败!{}",downloadResponse.getMessage());
                }
            }
        }

        //测试数据，假数据
        //return makeFalseResult();
        return resultData;
    }
    //是否初始化
    private boolean isInitSuccess(){
        return fac!=null&&fac.getAllMLCServiceList().size()>0;
    }
    //批量查询每个接口
    public List<Map<String,String>> searchLrc(String artist, String title){
        List<Map<String,String>> result = new ArrayList<>();
        for (MusicLrcSearch mlc:fac.getAllMLCServiceList()){
            log.info("正在使用 {} 引擎查询...",mlc.getSearchName());
            long time = System.currentTimeMillis();
            List<Map<String, String>> searchResult = null;
            try {
                searchResult = mlc.search(artist, title);
            } catch (Exception e) {
//                e.printStackTrace();
                log.error(e.getMessage());
            }
            time = System.currentTimeMillis() - time;
            if(searchResult!=null){
                log.info("查询完成，结果数量为 {} ，耗时：{} ms",searchResult.size(),time/1000.0);
                result.addAll(searchResult);
            }else{
                log.info("查询出现异常，耗时：{} ms",time/1000.0);
            }
        }
        return result;
    }
    //格式化传入请求
    private String showRequest(HttpServletRequest request){
        StringBuffer sb = new StringBuffer();
//        Map<String,String> out = new HashMap<>();
        sb.append(String.format("Url:%s",request.getRequestURL() ));sb.append("\r\n");
//        out.put("Url",request.getRequestURL());
        sb.append(String.format("Method:%s",request.getMethod() ));sb.append("\r\n");
//        out.put("Method",request.getMethod());
        sb.append(String.format("ContentType:%s",request.getContentType() ));sb.append("\r\n");
//        out.put("ContentType",request.getContentType());

        Map<String,String> parameter = getParameter(request);
        sb.append(String.format("parameter:%s",JSON.toJSONString(parameter) ));sb.append("\r\n");
//        out.put("parameter:",JSON.toJSONString(parameter));


//        return JSON.toJSONString(out);
        return sb.toString();
    }
    //获取传入参数
    private Map<String,String> getParameter(HttpServletRequest request){
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String,String> parameter = new HashMap<>();
        while (parameterNames.hasMoreElements()){
            String name = parameterNames.nextElement();
            //获取原始参数
            String val = request.getParameter(name);
            try {
                //装配参数
                parameter.put(
                        name
                        //解析
                        ,TtplayTextUtil.decode(val)
                );
            } catch (Exception e) {
                //e.printStackTrace();
//                    log.error(e.getMessage());
                parameter.put(
                        name
                        ,val
                );
            }
        }
        return parameter;
    }

    /**
     * 模拟返回结果，因为未找到可用服务器，所以数据格式参照网络历史文章【https://www.cxyzjd.com/article/weixin_30404405/98108452】
     * @return
     */
    private String makeFalseResult(){
        String templet = "\t<lrc id='%s' artist='%s' title='%s'></lrc>\r\n";
        StringBuffer sb = new StringBuffer();
        /**
         * <?xml version="1.0" encoding="UTF-8"?>
         * <result>
         * 　　<lrc id="70437" artist="胡彦斌" title="男人 KTV"></lrc>
         * 　　<lrc id="204568" artist="胡彦斌" title="男人KTV"></lrc>
         * </result>
         */
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<result>\n");
        for (int i = 0; i < Math.random()*10+10; i++) {
            sb.append(
                    String.format(templet,Math.random(),"歌手测试"+i,"标题测试"+i)
            );
        }
        sb.append("</result>");

        log.info(sb.toString());
        return sb.toString();
    }

    private final String resultTemplet = "\t<lrc id='%s' artist='%s' title='%s'></lrc>\r\n";
    private String serarchParse(List<Map<String,String>> data){
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<result>\n");

        for (Map<String,String> song:data){
//            log.info(JSON.toJSONString(song));
            sb.append(
                    String.format(resultTemplet,song.get("id"),song.get("artist"),song.get("title"))
            );
        }
        sb.append("</result>");
//        log.info(sb.toString());
        return sb.toString();
    }
}