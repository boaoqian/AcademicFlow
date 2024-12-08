package org.qba.RSGraph;

import javafx.animation.*;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

import java.util.*;
import static javafx.scene.Cursor.*;

public class RelationshipGraph {
    private final Pane root;
    private final Scene scene;
    private final Map<Integer, GraphNode> nodes = new HashMap<>();
    private final List<GraphEdge> edges = new ArrayList<>();
    private final Random random = new Random();
    private AnimationTimer forceTimer;
    private final DoubleBinding centerXBinding ;
    private final DoubleBinding centerYBinding ;
    private double centerF_r = 0.001;
    private double edgeL = 200;
    private double nodeF = 1000000;
    // 使用时间戳控制
    private long lastUpdate = 0;
    private final long FRAME_INTERVAL = 1_000_000_000L / 60; // 60 FPS (1秒=1_000_000_000纳秒)

    public RelationshipGraph() {
        root = new Pane();
        root.getStyleClass().add("relationship-graph-root");
        scene = new Scene(root);
        centerXBinding = root.widthProperty().divide(2);
        centerYBinding = root.heightProperty().divide(2);
        initializeGraph("root");
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
            return 30.0;
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
    public void initializeGraph(String rootlabel) {
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
        forceTimer.start();
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

        GraphNode(Vertex vertex,double x, double y) {
            circle = new Circle(x, y, vertex.getsize()+5);
            circle.getStyleClass().add("vertex");
            text = new Text(vertex.getLabel());
            text.getStyleClass().add("vertex-text");
            text.setMouseTransparent(true);  // 使文本不接收鼠标事件
            text.setFocusTraversable(false); // 禁止焦点遍历
            text.xProperty().bind(circle.centerXProperty()
                    .subtract(text.layoutBoundsProperty().getValue().getWidth() / 2));
            text.yProperty().bind(circle.centerYProperty()
                    .add(text.layoutBoundsProperty().getValue().getHeight() / 4));
            /*外面
            text.xProperty().bind(circle.centerXProperty()
                    .subtract(text.layoutBoundsProperty().getValue().getWidth() / 2));
            text.yProperty().bind(circle.centerYProperty()
                    .add(circle.radiusProperty())
                    .add(15));

             */
            // 添加鼠标事件处理
            circle.setOnMousePressed(e -> {
                dragBaseX = e.getSceneX() - circle.getCenterX();
                dragBaseY = e.getSceneY() - circle.getCenterY();
                isDragging = true;
                // 提高被拖拽节点的层级
                circle.toFront();
            });

            circle.setOnMouseDragged(e -> {
                if (isDragging) {
                    // 获取当前缩放比例
                    // 获取当前的缩放变换
                    Scale scale = getScaleTransform(root);
                    double currentScale = scale.getX();

                    // 将场景坐标转换为父容器的本地坐标
                    Point2D localPoint = root.sceneToLocal(e.getSceneX(), e.getSceneY());

                    // 应用缩放补偿
                    circle.setCenterX(localPoint.getX() - dragBaseX/currentScale);
                    circle.setCenterY(localPoint.getY() - dragBaseY/currentScale);
                    // 限制在视图范围内
                    circle.setCenterX(Math.max(50, Math.min(root.getWidth(), circle.getCenterX())));
                    circle.setCenterY(Math.max(50, Math.min(root.getHeight(), circle.getCenterY())));
                }
            });

            circle.setOnMouseReleased(e -> {
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
            this(vertex,0,0);
        }
    }
    public void addNode(Vertex vertex) {
//        stopAnimation();
        GraphNode node = new GraphNode(vertex);
        nodes.put(vertex.getId(), node);
        root.getChildren().addAll(node.circle,node.text);
//        startAnimation();
    }
    public void addEdge(GraphEdge edge) {
//        stopAnimation();
        edges.add(edge);
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

        GraphEdge(GraphNode source, GraphNode target) {
            this.source = source;
            this.target = target;
            line = new Line();
            line.getStyleClass().add("edge");
            line.startXProperty().bind(source.circle.centerXProperty());
            line.startYProperty().bind(source.circle.centerYProperty());
            line.endXProperty().bind(target.circle.centerXProperty());
            line.endYProperty().bind(target.circle.centerYProperty());
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
        edges.add(edge);
        root.getChildren().addFirst(edge.line);
    }

    private void updateForces() {
        // 简单的力导向算法
        for (GraphNode node : nodes.values()) {
            //向心力
            double dx = centerXBinding.get() - node.circle.getCenterX();
            double dy = centerYBinding.get() - node.circle.getCenterY();
            if(Math.abs(dy)>600){
                dy*=10;
            }
            if(Math.abs(dx)>600){
                dx*=10;
            }
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
                    dx=random.nextDouble();
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
        for (GraphEdge edge : edges) {
            double dx = edge.target.circle.getCenterX() - edge.source.circle.getCenterX();
            double dy = edge.target.circle.getCenterY() - edge.source.circle.getCenterY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < 0.1) {  // 设置最小距离阈值
                distance = 0.1;
                dy+=random.nextDouble();
            }
            double force = (distance - edgeL) ;
            dx *= force / distance;
            dy *= force / distance;

            if(edge.source.isDragging){
                edge.target.dx -= 10*dx;
                edge.target.dy -= 10*dy;
            }
            else if(edge.target.isDragging){
                edge.source.dx += 5*dx;
                edge.source.dy += 5*dy;
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
            node.circle.setCenterX(node.circle.getCenterX() + node.dx * 0.1);
            node.circle.setCenterY(node.circle.getCenterY() + node.dy * 0.1);

            // 限制在视图范围内
            node.circle.setCenterX(Math.max(50, Math.min(root.getWidth(), node.circle.getCenterX())));
            node.circle.setCenterY(Math.max(50, Math.min(root.getHeight(), node.circle.getCenterY())));
        }
    }

}

