import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            + "cited_count INTEGER"
            + ");";
    // SQL 插入数据语句
    static String insertSQL = "INSERT INTO paperinfo(uid, title, year, author, abstract, cited_uid, relation_uid, pdf," +
            " relation_url, cited_url, cited_count) "
            + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static void CreateTable() {
        try (Connection conn = DriverManager.getConnection(db_url)) {
            // 创建表
            conn.createStatement().execute(createTableSQL);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insert(int uid, String title, int year, String author, String abstract_text,
                              String cited_url, String relation_url, String cited_uid, String relation_uid,
                              String pdf, int cited_count
    ) throws SQLException {
        // 插入一条数据
        if (checkPaperExists(uid)){
            delete(uid);
        }
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
                pstmt.setInt(11, cited_count);
                // 执行插入操作
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println("错误：" + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insert(Paper paper) throws SQLException {
        insert(paper.get_uid(), paper.getTitle(), paper.getYear(), paper.getAuthor(),
                paper.getAbstract(), paper.getCited_url(), paper.getRelation_url(),
                paper.getCited_uid(), paper.getRelation_uid(), paper.getPdf_url(), paper.getCited_count());
    }

    public static void main(String[] args) throws SQLException {
        Paper a = new Paper("deferrtb", "dfrt,vret,12321 -", null, null, null, null, null);
        System.out.println(a.getCited_uid().getClass());
        Utils.insert(a);
    }

    public static void update(int uid, String columnName, String newValue) throws SQLException {
        String sql = null;
        if (!checkPaperExists(uid)) {
            System.out.println(uid+" not exists");
            return;
        }
        sql = "UPDATE papers SET " + columnName + " = ? WHERE uid = ?";

        try (Connection conn = DriverManager.getConnection(db_url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newValue);
            pstmt.setInt(2, uid);

            int affectedRows = pstmt.executeUpdate();
        }
    }
    public static void batchInsert(List<Paper> papers) throws SQLException {
        for (Paper paper : papers) {
            Utils.insert(paper);
        }
    }

    public static Paper getPaper(int uid) throws SQLException {
        String sql = "SELECT * FROM paperinfo WHERE uid = ?";
        try (Connection conn = DriverManager.getConnection(db_url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, uid);
            try (ResultSet rs = pstmt.executeQuery()) {
                return createPaperFromRS(rs);
            }
        }
    }

    public static Paper createPaperFromRS(ResultSet rs) throws SQLException {
        return new Paper(
                rs.getString("title"),
                rs.getInt("year"),
                rs.getString("author"),
                rs.getString("relation_url"),
                rs.getString("cited_url"),
                rs.getString("pdf"),
                rs.getInt("cited_count"),
                rs.getString("abstract"),// 数据库中是 pdf
                rs.getString("relation_uid"),
                rs.getString("cited_uid")
        );
    }

    public static ArrayList<Paper> getPaperList() throws SQLException {
        String sql = "SELECT * FROM paperinfo";
        try (Connection conn = DriverManager.getConnection(db_url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                ArrayList<Paper> paperList = new ArrayList<>();
                while (rs.next()) {
                    paperList.add(createPaperFromRS(rs));
                }
                return paperList;
            }
        }
    }
    public static void delete(int uid) throws SQLException {
        String sql = "DELETE FROM paperinfo WHERE uid = ?";
        try (Connection conn = DriverManager.getConnection(db_url);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, uid);
            int affectedRows = pstmt.executeUpdate();
        }
    }
    public static void clean() throws SQLException {
        String sql = "DELETE FROM paperinfo";
        try (Connection conn = DriverManager.getConnection(db_url);){
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int affectedRows = pstmt.executeUpdate();
        }
    }
    // 方法 2: 使用 EXISTS
    public static boolean checkPaperExists(int uid) {
        String sql = "SELECT EXISTS(SELECT 1 FROM paperinfo WHERE uid = ?)";

        try (Connection conn = DriverManager.getConnection(db_url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, uid);
            ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
