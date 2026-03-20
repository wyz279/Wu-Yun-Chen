import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;

public class FollowDAO {

	// 添加关注
	public static void followClub(String studentId, String clubCode) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
		     PreparedStatement stmt = conn.prepareStatement(
		         "INSERT INTO follow (student_id, club_code, follow_date) VALUES (?, ?, CURRENT_DATE)")) {
		    stmt.setString(1, studentId);
		    stmt.setString(2, clubCode);
		    stmt.executeUpdate();
		}
	}

	// 取消关注
	public static void unfollowClub(String studentId, String clubCode) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("DELETE FROM follow WHERE student_id = ? AND club_code = ?")) {
			stmt.setString(1, studentId);
			stmt.setString(2, clubCode);
			stmt.executeUpdate();
		}
	}

	// 检查用户是否已关注某个社团
	public static boolean isFollowing(String studentId, String clubCode) throws SQLException {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("SELECT id FROM follow WHERE student_id = ? AND club_code = ?")) {
			stmt.setString(1, studentId);
			stmt.setString(2, clubCode);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next(); // 如果存在记录，返回 true
			}
		}
	}

	// 获取用户所有关注的社团
	public static List<Club> getFollowedClubs(String studentId) throws SQLException {
		List<Club> clubs = new ArrayList<>();
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT c.club_code, c.name, c.type " + "FROM clubs c "
						+ "JOIN follow f ON c.club_code = f.club_code " + "WHERE f.student_id = ?")) {
			stmt.setString(1, studentId);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					Club club = new Club();
					club.setClubCode(rs.getString("club_code"));
					club.setName(rs.getString("name"));
					club.setType(rs.getString("type"));
					clubs.add(club);
				}
			}
		}
		return clubs;
	}
	
	//關注變化
	public static Map<LocalDate, Integer> getCumulativeFollowCountLast7Days(String clubCode) throws SQLException {
	    Map<LocalDate, Integer> result = new TreeMap<>(); 
	    String sql = "SELECT follow_date FROM follow WHERE club_code = ?";

	    List<LocalDate> followDates = new ArrayList<>();

	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setString(1, clubCode);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                followDates.add(rs.getDate("follow_date").toLocalDate());
	            }
	        }
	    }

	    LocalDate today = LocalDate.now();
	    for (int i = 6; i >= 0; i--) {
	        LocalDate targetDate = today.minusDays(i);
	        int count = 0;
	        for (LocalDate d : followDates) {
	            if (!d.isAfter(targetDate)) {
	                count++;
	            }
	        }
	        result.put(targetDate, count);
	    }

	    return result;
	}
}

