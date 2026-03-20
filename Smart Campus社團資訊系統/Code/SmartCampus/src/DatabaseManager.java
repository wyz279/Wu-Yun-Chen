import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class DatabaseManager {
    private static final String SERVER  = "jdbc:mysql://140.119.19.73:3315/";
    private static final String DATABASE = "MG06";
    private static final String URL = SERVER + DATABASE + "?useSSL=false";
    private static final String USERNAME = "MG06";
    private static final String PASSWORD = "AeOhRb";
    private static Connection connection = null;

    // 獲取資料庫連線
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("資料庫連線成功");
            }
        } catch (SQLException e) {
            System.err.println("資料庫連線失敗：" + e.getMessage());
        }
        return connection;
    }
    
    public static List<Activity> getUpcomingActivities() {
        List<Activity> activities = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE date >= CURDATE() ORDER BY date ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Activity activity = new Activity();
                activity.setActivitiesCode(rs.getString("activities_code"));
                activity.setName(rs.getString("name"));
                activity.setType(rs.getString("type"));
                activity.setDescription(rs.getString("description"));
                activity.setPlace(rs.getString("place"));
                activity.setDate(rs.getDate("date"));
                activity.setAccount(rs.getInt("account"));
                activity.setClubCode(rs.getString("club_code"));
                Blob imageBlob = rs.getBlob("image");
                if (imageBlob != null) {
                    activity.setImage(imageBlob.getBytes(1, (int) imageBlob.length()));
                }
                activities.add(activity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载活動失敗: " + e.getMessage(), "數據庫錯誤", JOptionPane.ERROR_MESSAGE);
        }
        return activities;
    }
  
}

