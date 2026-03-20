import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

	// 获取用户关注社团的最新活动（按日期倒序）
	public static List<Activity> getActivitiesForUser(String studentId) throws SQLException {
		List<Activity> activities = new ArrayList<>();
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT a.activities_code, a.name, a.type, a.description, a.place, a.date, a.image "
								+ "FROM activities a " + "JOIN follow f ON a.club_code = f.club_code "
								+ "WHERE f.student_id = ? " + "ORDER BY a.date DESC LIMIT 10" // 取最新10条
						)) {
			stmt.setString(1, studentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Activity activity = new Activity();
					activity.setActivitiesCode(rs.getString("activities_code"));
					activity.setName(rs.getString("name"));
					activity.setType(rs.getString("type"));
					activity.setDescription(rs.getString("description"));
					activity.setPlace(rs.getString("place"));
					activity.setDate(rs.getDate("date"));
					Blob imageBlob = rs.getBlob("image");
                    if (imageBlob != null) {
                        activity.setImage(imageBlob.getBytes(1, (int) imageBlob.length()));
                    }
					activities.add(activity);
				}
			}
		}
		return activities;
	}

	// 新增活动（供管理员发布活动时调用）
	public static void addActivity(Activity activity) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("INSERT INTO activities (name, type, description, place, date, club_code, image) "
								+ "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
			stmt.setString(1, activity.getName());
			stmt.setString(2, activity.getType());
			stmt.setString(3, activity.getDescription());
			stmt.setString(4, activity.getPlace());
			stmt.setDate(5, new java.sql.Date(activity.getDate().getTime()));
			stmt.setString(6, activity.getClubCode());
			if (activity.getImage() != null) {
                stmt.setBytes(7, activity.getImage());
            } else {
                stmt.setNull(7, java.sql.Types.BLOB);
            }
			stmt.executeUpdate();
		}
	}
}
