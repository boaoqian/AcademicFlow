import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Utils {
    static String db_url = "jdbc:sqlite:dbs/paperinfo.sqlite";
    // SQL 创建表语句（如果表不存在）
    static String createTableSQL = "CREATE TABLE IF NOT EXISTS paperinfo ("
            + "uid INTEGER PRIMARY KEY, "
            + "title TEXT, "
            + "year INTEGER, "
            + "author TEXT, "
            + "abstract TEXT, "
            + "cited_uid TEXT, "
            + "relation_uid TEXT, "
            + "pdf TEXT, "
            + "relation_url TEXT, "
            + "cited_url TEXT"
            + ");";
    // SQL 插入数据语句
    static String insertSQL = "INSERT INTO paperinfo(uid, title, year, author, abstract, cited_uid, relation_uid, pdf," +
            " relation_url, cited_url) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void CreateTable() {
        try (Connection conn = DriverManager.getConnection(db_url)) {
            // 创建表
            conn.createStatement().execute(createTableSQL);
            System.out.println("表已创建或已存在。");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void Insert(int uid,String title,int year, String author, String abstract_text,
                              String cited_url, String relation_url, String cited_uid, String relation_uid,
                              String pdf
    ) throws SQLException {
        // 插入一条数据
        try (Connection conn = DriverManager.getConnection(db_url)) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                // 设置字段值
                pstmt.setInt(1, uid); // uid
                pstmt.setString(2, title); // title
                pstmt.setInt(3, year); // year
                pstmt.setString(4, author); // author
                pstmt.setString(5, abstract_text); // abstract
                pstmt.setString(6, cited_uid); // cited_uid
                pstmt.setString(7, relation_uid); // relation_uid
                pstmt.setString(8, pdf); // pdf
                pstmt.setString(9, relation_url); // relation_url
                pstmt.setString(10, cited_url); // cited_url
                // 执行插入操作
                pstmt.executeUpdate();
                System.out.println("数据插入成功！");
            } catch (SQLException e) {
                System.out.println("错误：" + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static void Insert(Paper paper) throws SQLException {
        Insert(paper.get_uid(),paper.getTitle(),paper.getYear(),paper.getAuthor(),
                paper.getAbstract(), paper.getCited_url(), paper.getRelation_url(),
                paper.getCited_uid(),paper.getRelation_uid(),paper.getPdf_url());
    }
    public static void main(String[] args) throws SQLException {
        Paper a = new Paper("deferrtb","dfrt,vret,12321 -",null,null,null,null,null);
        System.out.println(a.getCited_uid().getClass());
        Utils.Insert(a);
    }
}
