package org.qba.academicflow;

import javafx.event.*;
import org.qba.backend.api.GoogleAPI;
import org.qba.backend.paper.Paper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static volatile Server instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private  GoogleAPI api = new GoogleAPI();
    private Server() {    }
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
    public void restartApi() {
        api.close();
        api = new GoogleAPI();
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
    public  static class NodeEvent extends Event {
        private final int nodeid;
        public static final EventType<NodeEvent> PRESSED = new EventType<>(Event.ANY, "PRESSED");
        public static final EventType<NodeEvent> RELEASED = new EventType<>(Event.ANY, "RELEASED");
        public NodeEvent(EventType<? extends Event> eventType, int nodeid) {
            super(eventType);
            this.nodeid = nodeid;
        }
        public int getNodeid() {
            return nodeid;
        }
    }
    // 创建一个简单的事件总线
    public static class NodeEventBus {
        private static final NodeEventBus instance = new NodeEventBus();
        private static final List<EventHandler<NodeEvent>> handlers = new ArrayList<>();
        private NodeEventBus() {}
        public synchronized static NodeEventBus getInstance() {
            if (instance == null) {
                return new NodeEventBus();
            }
            return instance;
        }

        public void subscribe(EventHandler<NodeEvent> handler) {
            handlers.add(handler);
        }

        public void publish(NodeEvent event) {
            handlers.forEach(handler -> handler.handle(event));
        }
    }
    public static Set<String> load_history() {
        String history_path = "PaperData/history.list";
        Path path = Path.of(history_path);
        if(Files.exists(path)) {
            try{
                return new HashSet<>(Files.readAllLines(path));
            }catch (Exception e){
                log(e.toString());
                log("read history file failed");
                return new HashSet<>();
            }
        }else {
            try {
                Files.createDirectories(Path.of("./PaperData"));
                Files.createFile(Path.of("./PaperData/history.list"));
            }catch (Exception e){
                log(e.toString());
                log("create history file failed");
            }
            return new HashSet<>();
        }
    }
    public static void add_history(String history) {
        if(history == null||history.length()<=2) {
            return;
        }
        String history_path = "PaperData/history.list";
        Set<String> history_list = load_history();
        history_list.add(history);
        try{
            Files.write(Path.of(history_path),new ArrayList<>(history_list).subList(0,Math.min(history_list.size(),100)));
        }catch (Exception e){
            log(e.toString());
            log("write history file failed");
            return;
        }
    }
}
