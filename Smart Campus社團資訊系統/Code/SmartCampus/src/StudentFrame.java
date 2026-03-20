import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.awt.*;
import javax.swing.*;
import java.util.List;

public class StudentFrame extends JFrame {
	private MyApp mainApp;
	private JButton faq, passwordChange, backToHome;
	private JTextArea clubIn, notify;
	private JScrollPane clubScroll, noScroll;
	private JPanel mainPanel, clubPanel, notifyPanel;
	private JPanel leftPanel, middlePanel, rightPanel;
	private Font btnFond = new Font("微軟正黑體", Font.PLAIN, 16);
	private Font inFond = new Font("微軟正黑體", Font.PLAIN, 14);

	public StudentFrame(MyApp mainApp) {
		this.mainApp = mainApp;
		if (mainApp.getCurrentUserId() == null) {
	        JOptionPane.showMessageDialog(null, "請先登入！");
	        mainApp.showHome();
	        dispose();
	        return;
	    }
		setSize(1000, 700);
		setTitle("Student Frame");
		createTextFeild();
		createButton();
		createPanel();
		add(mainPanel);
		loadFollowedClubs();
		loadNotifications();
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private void createTextFeild() {// 設置ok
		clubPanel = new JPanel();
		clubPanel.setLayout(new BoxLayout(clubPanel, BoxLayout.Y_AXIS));
		clubScroll = new JScrollPane(clubPanel);
		clubScroll.setPreferredSize(new Dimension(400, 605));

		notify = new JTextArea(35, 40);
		notify.setEditable(false);
		notify.setLineWrap(true);                
		notify.setWrapStyleWord(true);
		notify.setFont(inFond);                    
	    notify.setMargin(new Insets(5, 5, 5, 5));   
		noScroll = new JScrollPane(notify);
		noScroll.setPreferredSize(new Dimension(380, 580)); 
		
	}

	private void loadFollowedClubs() {
		clubPanel.removeAll();
		String studentId = mainApp.getCurrentUserId();
		try {
			List<Club> clubs = FollowDAO.getFollowedClubs(studentId);
			for (Club club : clubs) {
				JPanel entryPanel = new JPanel(new BorderLayout());
				entryPanel.add(new JLabel(club.getName() + " (" + club.getType() + ")"), BorderLayout.CENTER);

				JButton unfollowBtn = new JButton("取消關注");
				unfollowBtn.addActionListener(e -> {
					try {
						FollowDAO.unfollowClub(studentId, club.getClubCode());
						loadFollowedClubs(); // 刷新列表
					} catch (SQLException ex) {
						JOptionPane.showMessageDialog(this, "取消失败：" + ex.getMessage());
					}
				});
				entryPanel.add(unfollowBtn, BorderLayout.EAST);
				clubPanel.add(entryPanel);
				clubPanel.add(Box.createVerticalStrut(5));
			}
			clubPanel.revalidate();
			clubPanel.repaint();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this, "加載失敗：" + e.getMessage());
		}
	}

	private void loadNotifications() {
		String studentId = mainApp.getCurrentUserId();
		try {
			List<Activity> activities = ActivityDAO.getActivitiesForUser(studentId);
			StringBuilder sb = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

			for (Activity activity : activities) {
				sb.append("★ ").append(activity.getName()).append("\n   時間: ").append(sdf.format(activity.getDate()))
						.append("\n   地點: ").append(activity.getPlace()).append("\n   描述: ")
						.append(activity.getDescription()).append("\n\n");
			}

			if (sb.length() == 0) {
				sb.append("暫無新通知");
			}

			notify.setText(sb.toString());
		} catch (SQLException e) {
			notify.setText("通知加載失敗：" + e.getMessage());
		}
	}

	private void createButton() {
		faq = new JButton("常見問題");
		faq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String faqText = "常見問題\n" + "Q1: 推薦系統是什麼?\n" + "A1: 以學生身分登入後，可以點擊推薦系統按鈕，回答完問題後會向用戶推薦適合的社團。\n"
						+ "Q2: 在訪客模式下可以做什麼?\n" + "A2: 若用戶未登入，使用訪客模式時，可以在主頁瀏覽活動及社團資訊，也可以使用查詢功能，\n"
						+ "   但是無法使用推薦系統和管理中心，系統也不會記錄用戶的使用痕跡。";
				JOptionPane.showMessageDialog(null, faqText, "常見問題", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		passwordChange = new JButton("變更密碼");
		passwordChange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				SwingUtilities.invokeLater(() -> {
					JDialog dialog = new JDialog();
					dialog.setTitle("變更密碼");
					dialog.setSize(600, 350);
					dialog.setLocationRelativeTo(null);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setLayout(new GridBagLayout());

					GridBagConstraints gbc = new GridBagConstraints();
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.insets = new Insets(20, 20, 10, 20);

					JLabel titleLabel = new JLabel("變更密碼", SwingConstants.CENTER);
					titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 25));
					gbc.gridx = 0;
					gbc.gridy = 0;
					gbc.gridwidth = 2;
					dialog.add(titleLabel, gbc);

					gbc.gridwidth = 1;
					gbc.gridx = 0;
					gbc.gridy = 1;
					dialog.add(new JLabel("請輸入舊密碼:"), gbc);

					gbc.gridx = 1;
					gbc.gridy = 1;
					JTextField password1Field = new JTextField(25);
					dialog.add(password1Field, gbc);

					gbc.gridx = 0;
					gbc.gridy = 2;
					dialog.add(new JLabel("請輸入新密碼:"), gbc);

					gbc.gridx = 1;
					gbc.gridy = 2;
					JPasswordField password2Field = new JPasswordField(25);
					dialog.add(password2Field, gbc);

					JButton confirmButton = new JButton("確認");
					confirmButton.setBackground(Color.BLUE);
					confirmButton.setForeground(Color.WHITE);
					gbc.gridx = 0;
					gbc.gridy = 3;
					gbc.gridwidth = 2;
					dialog.add(confirmButton, gbc);

					confirmButton.addActionListener(e -> {
						String userId = mainApp.getCurrentUserId();
						String oldPassword = new String(password1Field.getText());
						String newPassword = new String(password2Field.getText());

						if (userPasswordChange(userId, oldPassword, newPassword)) {
							JOptionPane.showMessageDialog(dialog, "密碼更改成功");
							dialog.dispose();
						} else {
							JOptionPane.showMessageDialog(dialog, "密碼更改失敗，請重新確認舊密碼");
						}
					});

					dialog.setVisible(true);
				});
			}
		});
		backToHome = new JButton("回首頁");
		backToHome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				mainApp.showHome();
				dispose();
			}
		});
		JButton[] rButtons = { faq, passwordChange, backToHome };
		for (JButton rButton : rButtons) {
			rButton.setFont(btnFond);
			rButton.setFocusPainted(false);
			rButton.setPreferredSize(new Dimension(150, 40));
		}
	}

	private void createPanel() {
		clubPanel = new JPanel();
		clubPanel.setPreferredSize(new Dimension(400, 605));
		clubPanel.setBorder(BorderFactory.createTitledBorder("關注的社團"));
		clubPanel.add(clubScroll);
		notifyPanel = new JPanel(new BorderLayout());
		notifyPanel.setPreferredSize(new Dimension(400, 605));
		notifyPanel.setBorder(BorderFactory.createTitledBorder("通知、公告"));
		notifyPanel.add(noScroll, BorderLayout.CENTER);
		leftPanel = new JPanel();
		leftPanel.add(clubPanel);
		middlePanel = new JPanel();
		middlePanel.add(notifyPanel);
		rightPanel = new JPanel();
		rightPanel.setPreferredSize(new Dimension(160, 600));
		rightPanel.add(faq);
		rightPanel.add(passwordChange);
		rightPanel.add(backToHome);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(leftPanel, BorderLayout.WEST);
		mainPanel.add(middlePanel, BorderLayout.CENTER);
		mainPanel.add(rightPanel, BorderLayout.EAST);
	}

	private boolean userPasswordChange(String studentId, String oldPassword, String newPassword) {
		try (Connection conn = DatabaseManager.getConnection();
				PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE student_id = ?")) {

			stmt.setString(1, studentId);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				String dbPassword = rs.getString("password");
				if (dbPassword.equals(oldPassword)) {
					String updateSql = "UPDATE users SET password = ? WHERE student_id = ?";
					try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
						updateStmt.setString(1, newPassword);
						updateStmt.setString(2, studentId);
						updateStmt.executeUpdate();
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}

