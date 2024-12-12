package org.qba.academicflow;

import javafx.event.*;
import org.qba.backend.api.GoogleAPI;
import org.qba.backend.paper.Paper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static volatile Server instance;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private  final GoogleAPI api = new GoogleAPI();
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
}
