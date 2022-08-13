package com.example.ttplayerlrcsearch.ui;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@Component
@FXMLView(value = "/view/ControllerWindow.fxml", title = "日志输出")
public class ControllerWindow extends AbstractFxmlView implements Initializable {

    @FXML
    WebView web;

    public void onload(){
        //log.info("web[onload] = {}",web);
        if(web!=null){
            WebEngine engine = web.getEngine();
            engine.load("http://127.0.0.1:8098");
        }else{
            log.error("WebView 初始化失败！{}",web);
        }
    }
    /*
    public void exitService(){
        System.exit(0);
    }
    */
    /**
     * 初始化事件
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        onload();
    }
}