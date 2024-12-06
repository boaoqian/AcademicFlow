package org.qba.academicflow;

import javafx.event.*;
import org.qba.backend.api.GoogleAPI;
import org.qba.backend.paper.Paper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static volatile Server instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private  final GoogleAPI api = new GoogleAPI();
    private Server() {}
    //静态方法
    public static void log(String message) {
        System.out.println(message);
    }
    // 公共静态方法提供全局访问点
    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                if (instance == null) {
                    instance = new Server();
                }
            }
        }
        return instance;
    }
    public  ExecutorService getExecutor() {
        return executor;
    }
    public  GoogleAPI getApi() {
        return api;
    }
    public  void close(){
        executor.shutdown();
        api.close();
    }
    // 自定义事件类
    public static class PaperEvent extends Event {
        public static final EventType<PaperEvent> GETPDF =
                new EventType<>(Event.ANY, "GETPDF");
        public static final EventType<PaperEvent> GRAPH_BUILT =
                new EventType<>(Event.ANY, "GRAPH_BUILT");

        private final Paper paper;

        public PaperEvent(EventType<PaperEvent> eventType, Paper paper) {
            super(eventType);
            this.paper = paper;
        }

        public Paper getPaper() {
            return paper;
        }
    }


}
