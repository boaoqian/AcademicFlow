package org.qba.RSGraph;

import javafx.animation.*;
import javafx.beans.binding.DoubleBinding;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import org.qba.academicflow.Server;

import java.util.*;
import static javafx.scene.Cursor.*;

public class RelationshipGraph {
    private Server.NodeEventBus eventBus;
    private final Pane root;
    private final Scene scene;
    private final Map<Integer, GraphNode> nodes = new HashMap<>();
    private final Map<Integer,GraphEdge> edges = new HashMap<>();
    private final Random random = new Random();
    private AnimationTimer forceTimer;
    private final DoubleBinding centerXBinding ;
    private final DoubleBinding centerYBinding ;
    private double centerF_r = 0.05;
    private double edgeL = 100;
    private double nodeF = 500000;
    // 使用时间戳控制
    private long lastUpdate = 0;
    private final long FRAME_INTERVAL = 1_000_000_000L / 60; // 60 FPS (1秒=1_000_000_000纳秒)

    public RelationshipGraph() {
        root = new Pane();
        root.getStyleClass().add("relationship-graph-root");
        scene = new Scene(root);
        eventBus = Server.NodeEventBus.getInstance();
        centerXBinding = root.widthProperty().divide(2);
        centerYBinding = root.heightProperty().divide(2);
        initializeGraph();
    }
    public static class plainVertex extends Vertex{
        private final int id;
        private final String label;
        public plainVertex(int id, String label) {
            this.id = id;
            this.label = label;
        }
        @Override
        public int getId() {
            return id;
        }

        @Override
        public String getLabel() {
            return label;
        }
        @Override
        public Double getsize() {
            return 15.0;
        }
    }

    // 获取根面板
    public Pane getRoot() {
        return root;
    }

    public Scene getScene() {
        return scene;
    }

    // 获取指定ID的节点
    public GraphNode getNode(int id) {
        return nodes.get(id);
    }

    // 初始化图形
    public void initializeGraph() {
        // 力导向布局动画
        forceTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= FRAME_INTERVAL) {
                    updateForces();
                    lastUpdate = now;
                }
            }
        };
    }

    // 启动动画
    public void startAnimation() {
        forceTimer.start();
    }

    // 停止动画
    public void stopAnimation() {
        forceTimer.stop();
    }


    public class GraphNode {
        Circle circle;
        Text text;
        double dx, dy;
        // 添加拖拽相关的变量
        private double dragBaseX, dragBaseY;
        private boolean isDragging = false;
        private int id;

        GraphNode(Vertex vertex, double x, double y, boolean is_root) {
            id = vertex.getId();
            circle = new Circle(x, y, vertex.getsize()+1);
            if (is_root) {
                circle.getStyleClass().add("vertex-root");
            }else{
                circle.getStyleClass().addAll("vertex","base");
            }
            String label = vertex.getLabel();
            text = new Text(label);
            text.getStyleClass().add("vertex-text");
            text.setMouseTransparent(true);  // 使文本不接收鼠标事件
            text.setFocusTraversable(false); // 禁止焦点遍历
//            text.xProperty().bind(circle.centerXProperty());
//            text.yProperty().bind(circle.centerYProperty());
//            text.setTranslateX(-text.getLayoutBounds().getWidth() / 2);
//            text.setTranslateY(text.getLayoutBounds().getHeight() / 2);
            //*外面
            text.xProperty().bind(circle.centerXProperty()
                    .subtract(text.layoutBoundsProperty().getValue().getWidth() / 2));
            text.yProperty().bind(circle.centerYProperty()
                    .add(circle.radiusProperty())
                    .add(15));

             //*/
            // 添加鼠标事件处理
            circle.setOnMousePressed(e -> {
                // 获取当前缩放比例
                Scale scale = getScaleTransform(root);
                double currentScale = scale.getX();

                // 将场景坐标转换为本地坐标
                Point2D localPoint = root.sceneToLocal(e.getSceneX(), e.getSceneY());
                Point2D circleLocal = root.sceneToLocal(
                        circle.localToScene(circle.getCenterX(), circle.getCenterY())
                );

                // 计算基准点时考虑缩放因素
                dragBaseX = localPoint.getX() - circleLocal.getX();
                dragBaseY = localPoint.getY() - circleLocal.getY();

                isDragging = true;
                circle.toFront();
                eventBus.publish(new Server.NodeEvent(Server.NodeEvent.PRESSED, vertex.getId()));
            });

            circle.setOnMouseDragged(e -> {
                if (isDragging) {
                    // 获取当前缩放比例
                    Scale scale = getScaleTransform(root);
                    double currentScale = scale.getX();

                    // 将场景坐标转换为本地坐标
                    Point2D localPoint = root.sceneToLocal(e.getSceneX(), e.getSceneY());

                    // 计算新位置
                    double newX = Math.max(50, Math.min(root.getWidth()-50,
                            localPoint.getX() - dragBaseX));
                    double newY = Math.max(50, Math.min(root.getHeight()-50,
                            localPoint.getY() - dragBaseY));

                    circle.setCenterX(newX);
                    circle.setCenterY(newY);
                }
            });
            circle.setOnMouseReleased(e -> {
                eventBus.publish(new Server.NodeEvent(Server.NodeEvent.RELEASED, vertex.getId()));
                isDragging = false;
                text.toFront();
            });

            // 添加鼠标悬停效果
            circle.setOnMouseEntered(e -> {;
                circle.setCursor(HAND);
            });

            circle.setOnMouseExited(e -> {
                circle.setCursor(DEFAULT);
            });
        }
        GraphNode(Vertex vertex){
            this(vertex,centerXBinding.get()+random.nextDouble(-10,10),centerYBinding.get()+random.nextDouble(-10,10),false);
        }
        GraphNode(Vertex vertex, boolean is_root){
            this(vertex,centerXBinding.get(),centerYBinding.get(),is_root);
        }
    }
    public void reset_style(){
        if(!nodes.isEmpty()){
            nodes.values().forEach(node -> {
                node.circle.setOpacity(1);
                node.text.setOpacity(1);
                node.circle.getStyleClass().remove("select");
            });
        }
    }
    public void filted_vertex(Set<Integer> uids){

        if(!nodes.isEmpty()){
            nodes.keySet().stream().filter(uid->!uids.contains(uid)).forEach(uid -> {
                nodes.get(uid).circle.setOpacity(0.3);
                nodes.get(uid).text.setOpacity(0.3);});
            nodes.keySet().stream().filter(uids::contains).forEach(uid->{
                nodes.get(uid).circle.getStyleClass().add("select");
            });
        }

    }
    public void addRoot(Vertex vertex) {
        if (nodes.get(vertex.getId())!=null) {
            return;
        }
        GraphNode node = new GraphNode(vertex,true);
        nodes.put(vertex.getId(), node);
        root.getChildren().addAll(node.circle,node.text);
    }
    public void addNode(Vertex vertex) {
        if (nodes.containsKey(vertex.getId())) {
            return;
        }
//        stopAnimation();
        GraphNode node = new GraphNode(vertex);
        nodes.put(vertex.getId(), node);
        root.getChildren().addAll(node.circle,node.text);
//        startAnimation();
    }
    public void addNodes(List<? extends Vertex> vertices) {
        stopAnimation();
        for(var v : vertices) {
            addNode(v);

        }
        startAnimation();
    }
    public void addEdges(int from, List<Integer> to) {
        stopAnimation();
        for(var t : to) {
            addEdge(from, t);
        }
        startAnimation();
    }
    public void addEdge(GraphEdge edge) {
//        stopAnimation();
        if(edges.containsKey(edge.hashCode())){
            return;
        }
        edges.put(edge.hashCode(), edge);
        root.getChildren().addAll(edge.line);
        edge.line.toBack();
//        startAnimation();
    }
    public void addEdge(int srcId,int dscId){
        GraphEdge e = new GraphEdge(getNode(srcId),getNode(dscId));
        addEdge(e);
    }

    private Scale getScaleTransform(Pane node) {
        // 查找是否已存在缩放变换
        for (Transform transform : node.getTransforms()) {
            if (transform instanceof Scale) {
                return (Scale) transform;
            }
        }
        Scale scale = new Scale(1.0, 1.0);
        node.getTransforms().add(scale);
        return scale;
    }

    public class GraphEdge {
        Line line;
        GraphNode source;
        GraphNode target;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            GraphEdge graphEdge = (GraphEdge) o;
            return Objects.equals(source, graphEdge.source) && Objects.equals(target, graphEdge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source)+Objects.hash(target);
        }

        GraphEdge(GraphNode source, GraphNode target) {
            this.source = source;
            this.target = target;
            line = new Line();
            line.setMouseTransparent(true);  // 使文本不接收鼠标事件
            line.setFocusTraversable(false); // 禁止焦点遍历
            line.getStyleClass().add("edge");
            line.startXProperty().bind(source.circle.centerXProperty());
            line.startYProperty().bind(source.circle.centerYProperty());
            line.endXProperty().bind(target.circle.centerXProperty());
            line.endYProperty().bind(target.circle.centerYProperty());
            // 监听line的parent属性
            eventBus.subscribe(event -> {
                if (event.getEventType() == Server.NodeEvent.PRESSED) {
                    if(event.getNodeid() == source.id) {
                        target.circle.getStyleClass().remove("base");
                        target.circle.getStyleClass().add("link");
                    }
                    else if(event.getNodeid() == target.id) {
                        source.circle.getStyleClass().remove("base");
                        source.circle.getStyleClass().add("link");
                    }
                }
                else if (event.getEventType() == Server.NodeEvent.RELEASED) {
                    if(event.getNodeid() == target.id||event.getNodeid() == source.id) {
                        target.circle.getStyleClass().remove("link");
                        source.circle.getStyleClass().remove("link");
                        target.circle.getStyleClass().add("base");
                        source.circle.getStyleClass().add("base");
                    }
                }
            });
        }
    }



    public void addRandomNode() {
        Integer[] existingNodes = nodes.keySet().toArray(new Integer[0]);
        if(existingNodes.length == 0) {
            addNode(new plainVertex(0, "root"));
            return;
        }
        int sourceId = existingNodes[random.nextInt(existingNodes.length)];
        int newId = nodes.size();

        // 在随机位置创建新节点
        double x = 50;
        double y = 50;
        addNode(new plainVertex(newId, ""+newId));
        // 创建连接
        GraphEdge edge = new GraphEdge(nodes.get(sourceId), nodes.get(newId));
        edges.put(edge.hashCode(), edge);
        root.getChildren().addFirst(edge.line);
    }

    private void updateForces() {
        // 简单的力导向算法
        for (GraphNode node : nodes.values()) {
            //向心力
            double dx = centerXBinding.get() - node.circle.getCenterX();
            double dy = centerYBinding.get() - node.circle.getCenterY();

            // 计算力的分量
            if (!node.isDragging){
//                System.out.println(centerXBinding.get();
                node.dx = dx*centerF_r;
                node.dy = dy*centerF_r;
            }else {
                node.dx = 0;
                node.dy = 0;
            }
//            System.out.println(node.circle.getCenterX()+" "+node.circle.getCenterY());
        }

        // 节点间的斥力
        for (GraphNode n1 : nodes.values()) {
            for (GraphNode n2 : nodes.values()) {
                if (n1 == n2) continue;

                double dx = n1.circle.getCenterX() - n2.circle.getCenterX();
                double dy = n1.circle.getCenterY() - n2.circle.getCenterY();
                double distance = Math.sqrt(dx * dx + dy * dy);
                if(distance==0){
                    dx=random.nextDouble()*5;
                    dy=random.nextDouble()*5;
                }
                if (distance < 1) distance = 1;

                double force = nodeF / (distance * distance);
                if (n1.isDragging){
                    force*=0.1;
                }
                n1.dx += dx * force / distance;
                n1.dy += dy * force / distance;
            }
        }

        // 边的弹力
        for (GraphEdge edge : edges.values()) {
            double dx = edge.target.circle.getCenterX() - edge.source.circle.getCenterX();
            double dy = edge.target.circle.getCenterY() - edge.source.circle.getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < 0.1) {  // 设置最小距离阈值
                distance = 0.1;
                dy+=random.nextDouble()*10;
                dx+=random.nextDouble()*10;
            }
            double force = (distance - edgeL);
            dx *= force / distance;
            dy *= force / distance;

            if(edge.source.isDragging){
                edge.target.dx -= 2*dx;
                edge.target.dy -= 2*dy;
            }
            else if(edge.target.isDragging){
                edge.source.dx += 2*dx;
                edge.source.dy += 2*dy;
            }
            else {
                edge.source.dx += dx;
                edge.source.dy += dy;
                edge.target.dx -= dx;
                edge.target.dy -= dy;
            }
        }

        // 应用力的效果
        for (GraphNode node : nodes.values()) {
            if(node.isDragging){
                continue;
            }
            node.circle.setCenterX(node.circle.getCenterX() + Math.max(-100,Math.min(node.dx * 0.1,100)));
            node.circle.setCenterY(node.circle.getCenterY() + Math.max(-100,Math.min(node.dy * 0.1,100)));

            // 限制在视图范围内
            double padding = 50+random.nextDouble();
            node.circle.setCenterX(Math.max(padding*random.nextDouble(), Math.min(root.getWidth()-padding, node.circle.getCenterX())));
            node.circle.setCenterY(Math.max(padding, Math.min(root.getHeight()-padding, node.circle.getCenterY())));
        }
    }



}

