package com.example.ttplayerlrcsearch.service;

import com.example.ttplayerlrcsearch.entity.ApiResponse;
import com.example.ttplayerlrcsearch.entity.TTPlayResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 接口数据来自：http://xmsj.org/
 */
//@Service
@Slf4j
public class XmsjMLC extends LRCDispose implements MusicLrcSearch {

    @Override
    public void initialization() {
        log.info("xmsj接口由 http://xmsj.org/ 提供");
    }

    @Override
    public String getSearchName() {
        return "xmsj";
    }

    @Override
    public List<TTPlayResult> search(String artist, String title) {
        return null;
    }

    @Override
    public ApiResponse<String> download(String musicId) {
        return null;
    }
}
