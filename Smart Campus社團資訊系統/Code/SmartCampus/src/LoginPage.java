import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPage extends JDialog {
	private JTextField accountField;
	private JPasswordField passwordField;
	private JButton confirmButton;
	private String userRole;
	private MyApp parent;
	
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public LoginPage(MyApp parent) {
		super(parent, "登入", true);
		this.parent = parent;
		setSize(600, 350);
		setLocationRelativeTo(parent);
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(20, 20, 10, 20);

		JLabel titleLabel = new JLabel("登入", SwingConstants.CENTER);
		titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 30));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		add(titleLabel, gbc);

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(new JLabel("學號:"), gbc);
		gbc.gridx = 1;
		accountField = new JTextField(25);
		add(accountField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		add(new JLabel("密碼:"), gbc);
		gbc.gridx = 1;
		passwordField = new JPasswordField(25);
		add(passwordField, gbc);

		confirmButton = new JButton("確認");
		confirmButton.setBackground(Color.BLUE);
		confirmButton.setForeground(Color.WHITE);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 2;
		add(confirmButton, gbc);

		confirmButton.addActionListener(e -> {
			String studentId = accountField.getText();
			String password = new String(passwordField.getPassword());

			if (authenticateUser(studentId, password)) {
				parent.setCurrentUserId(studentId);
				dispose(); // 登入成功後關閉登入視窗
			} else {
				JOptionPane.showMessageDialog(this, "登入失敗，請檢查學號或密碼");
			}
		});
	}

	private boolean authenticateUser(String studentId, String password) {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn
						.prepareStatement("SELECT * FROM users WHERE student_id = ? AND password = ?")) {

			stmt.setString(1, studentId);
			stmt.setString(2, password);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String role = rs.getString("role");
				JOptionPane.showMessageDialog(this, "登入成功，身分：" + role);
				userRole = role;

				parent.updateIdentity(role);
				// 根據身分開啟不同的功能或頁面
				if (role.equals("學生")) {
					// 開啟學生主頁
					System.out.println("學生登入成功");
				} else if (role.equals("管理員")) {
					// 開啟管理員主頁
					System.out.println("管理員登入成功");
				}

				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public String getUserRole() {
		return userRole;
	}
}

