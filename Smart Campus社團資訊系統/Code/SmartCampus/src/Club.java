import java.sql.Date;

public class Club {
	private String clubCode; // 对应 club_code (VARCHAR(10))
	private String name; // 社团名称 (VARCHAR(100))
	private String type; // 类型 (VARCHAR(50))
	private Date date; // 成立日期 (DATE)
	private String expense; // 费用 (VARCHAR(50))
	private String contact; // 联系方式 (VARCHAR(50))
	private String description; // 描述 (TEXT)
	private int account; // 关联账户 (INT(20))

	// Getters & Setters
	public String getClubCode() {
		return clubCode;
	}

	public void setClubCode(String clubCode) {
		this.clubCode = clubCode;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getExpense() {
		return expense;
	}

	public void setExpense(String expense) {
		this.expense = expense;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getAccount() {
		return account;
	}

	public void setAccount(int account) {
		this.account = account;
	}
}

