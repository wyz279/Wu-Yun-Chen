import java.sql.Date;

public class Activity {
	private String activitiesCode; // 活动唯一代码 (INT(10))
	private String name; // 活动名称 (VARCHAR(100))
	private String type; // 活动类型 (VARCHAR(50))
	private String description; // 活动描述 (TEXT)
	private String place; // 地点 (VARCHAR(100))
	private Date date; // 日期 (DATE)
	private int account; // 关联账户 (INT(20))
	private String clubCode; // 关联社团代码 (VARCHAR(10))
	private byte[] image;
	
	// Getters & Setters
	public String getActivitiesCode() {
		return activitiesCode;
	}

	public void setActivitiesCode(String activitiesCode) {
		this.activitiesCode = activitiesCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
		this.account = account;
	}

	public String getClubCode() {
		return clubCode;
	}

	public void setClubCode(String clubCode) {
		this.clubCode = clubCode;
	}
	
	public byte[] getImage() {
	        return image;
	}

	public void setImage(byte[] image) {
	        this.image = image;
	}
}

