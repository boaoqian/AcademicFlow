import java.io.IOException;
import java.sql.SQLException;

public class MainCMD {
    public static void main(String[] args) throws IOException, SQLException {
        Paper a = new Paper("deferrtb","dfrt,vret,12321 -",null,null,null,null,null);
        System.out.println(Utils.db_url);
        Utils.Insert(a);
    }
}
