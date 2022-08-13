package com.example.ttplayerlrcsearch.service;

import com.example.ttplayerlrcsearch.entity.ApiResponse;
import com.example.ttplayerlrcsearch.entity.TTPlayResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 定义查询接口，只要实现了该接口，就会被自动调用
 */
@Service
public interface MusicLrcSearch {
    public String getSearchName();

    // 初始化方法，可以定义接口说明，来源等信息
    public void initialization();

    //查询歌曲
    //public List<Map<String,String>> search(String artist,String title);
    public List<TTPlayResult> search(String artist, String title);
    //根据歌曲id下载歌词
    public ApiResponse<String> download(String musicId);
}
