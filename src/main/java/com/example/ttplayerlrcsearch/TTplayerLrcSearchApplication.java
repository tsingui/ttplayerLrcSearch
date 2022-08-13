package com.example.ttplayerlrcsearch;

import com.alibaba.fastjson.JSON;
import com.example.ttplayerlrcsearch.ui.ControllerWindow;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;

@EnableScheduling // 开启定时任务功能
@SpringBootApplication
public class TTplayerLrcSearchApplication extends AbstractJavaFxApplicationSupport {

    @Resource
    ControllerWindow window;

    //默认启动GUI模式
    private static boolean runGui = true;

//    public static void main(String[] args) {
//        SpringApplication.run(TTplayerLrcSearchApplication.class, args);
//    }

    public static void main(String[] args) {
        System.out.println("启动参数"+JSON.toJSONString(args));
        if(args!=null){
            for (int i = 0; i < args.length; i++) {
                if("-noGUI".equals(args[i])){
                    runGui = false;
                }
            }
        }
        if(runGui){
            launch(TTplayerLrcSearchApplication.class, ControllerWindow.class, args);
        }else{
            launch(TTplayerLrcSearchApplication.class, args);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        if(stage!=null && runGui){
            //窗口标题
            stage.setTitle("日志输出");
            super.start(stage);
        }
    }
}