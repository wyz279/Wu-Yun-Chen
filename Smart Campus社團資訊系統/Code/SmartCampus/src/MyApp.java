import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyApp extends JFrame {
	private JTextField searchField;
	private JComboBox<String> categoryComboBox;
	private JPanel clubInfoPanel;
	private JLabel identityLabel;
	private JButton loginButton;
	private JButton recommendButton;
	private JButton profileButton;
	private JButton manageButton;
	private String identity = "訪客";
	private String currentUserID = null;
	private JPanel carouselPanel;
	private Timer carouselTimer;
	private int currentActivityIndex = 0;
	private List<Activity> upcomingActivities;
	private JLabel carouselLabel;
	private JButton prevButton, nextButton, stopButton;

	public MyApp() {
		setTitle("政大 Smart Campus - 社團管理系統");
		setSize(1000, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());

		UIManager.put("Button.font", new Font("Microsoft JhengHei", Font.PLAIN, 14));
		UIManager.put("Label.font", new Font("Microsoft JhengHei", Font.PLAIN, 14));

		initTitle();
		initSearchArea();
		initRightPanel();
		updateIdentity("訪客");
		updateClubList();
		loadUpcomingActivities();
		setVisible(true);
	}

	public void updateIdentity(String role) {
		identity = role;
		identityLabel.setText("目前身份：" + role);
		if (!"訪客".equals(role)) {
			loginButton.setText("登出");
		} else {
			loginButton.setText("登入");
			setCurrentUserId(null);
		}
		// 控制按鈕顯示
		switch (role) {
		case "訪客":
			recommendButton.setVisible(false);
			profileButton.setVisible(false);
			manageButton.setVisible(false);
			break;
		case "學生":
			recommendButton.setVisible(true);
			profileButton.setVisible(true);
			manageButton.setVisible(false);

			break;
		case "管理員":
			recommendButton.setVisible(false);
			profileButton.setVisible(false);
			manageButton.setVisible(true);
			break;
		}
	}

	// 初始化標題
	private void initTitle() {
		JLabel titleLabel = new JLabel("政大 Smart Campus - 社團管理系統", JLabel.CENTER);
		titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 26));
		titleLabel.setForeground(new Color(0, 70, 140));
		titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
		add(titleLabel, BorderLayout.NORTH);
	}

	// 初始化搜尋區塊與社團列表
	private void initSearchArea() {
		// 搜尋列
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		searchPanel.setBackground(new Color(235, 245, 255));
		searchPanel
				.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 220, 240)),
						BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		searchField = new JTextField(20);
		searchField.setToolTipText("輸入社團關鍵字");

		categoryComboBox = new JComboBox<>(new String[] { "全部", "學術", "藝術", "體適能", "聯誼", "服務", "自治團體" });
		categoryComboBox.setEnabled(rootPaneCheckingEnabled);
		JButton searchButton = new JButton("搜尋");
		searchButton.addActionListener(e -> updateClubList());
		searchPanel.add(searchField);
		searchPanel.add(categoryComboBox);
		searchPanel.add(searchButton);

		// 熱門活動提示
		/**
		 * JButton recommendActivityButton = new JButton("近期熱門活動：春季音樂祭！");
		 * recommendActivityButton.setPreferredSize(new Dimension(500, 35));
		 * recommendActivityButton.setBackground(new Color(255, 230, 200));
		 */
		// 連資料庫
		// recommendActivityButton.addActionListener(e ->
		// JOptionPane.showMessageDialog(this, "活動詳情：4/30 校園音樂節，歡迎參加！"));
		carouselPanel = new JPanel(new BorderLayout());
		carouselPanel.setPreferredSize(new Dimension(500, 200));
		carouselPanel.setBackground(new Color(240, 245, 255));
		Border lineBorder = BorderFactory.createLineBorder(new Color(180, 200, 240));
		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		carouselPanel.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

		carouselLabel = new JLabel("正在加载活动...", JLabel.CENTER);
		carouselLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
		carouselLabel.setForeground(new Color(100, 100, 120));
		carouselLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		carouselPanel.add(carouselLabel, BorderLayout.CENTER);
		// 添加点击事件
		carouselLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (upcomingActivities != null && !upcomingActivities.isEmpty()) {
					showActivityDetails(upcomingActivities.get(currentActivityIndex));
				}
			}
		});
		// 创建控制按钮面板
		JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		controlPanel.setOpaque(false);

		prevButton = createCarouselButton("◀");
		nextButton = createCarouselButton("▶");
		stopButton = createCarouselButton("⏸");

		prevButton.addActionListener(e -> showPreviousActivity());
		nextButton.addActionListener(e -> showNextActivity());
		stopButton.addActionListener(e -> toggleCarousel());

		controlPanel.add(prevButton);
		controlPanel.add(stopButton);
		controlPanel.add(nextButton);

		carouselPanel.add(controlPanel, BorderLayout.SOUTH);

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		topPanel.setBackground(getContentPane().getBackground());
		topPanel.add(carouselPanel);
		topPanel.add(Box.createVerticalStrut(10));
		topPanel.add(searchPanel);

		// 社團資訊顯示區
		clubInfoPanel = new JPanel();
		clubInfoPanel.setLayout(new BoxLayout(clubInfoPanel, BoxLayout.Y_AXIS));
		clubInfoPanel.setBackground(Color.white);

		JScrollPane scrollPane = new JScrollPane(clubInfoPanel);
		scrollPane.setPreferredSize(new Dimension(550, 350));
		scrollPane.setBorder(BorderFactory.createTitledBorder("社團資訊"));

		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
		centerPanel.add(topPanel, BorderLayout.NORTH);
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		add(centerPanel, BorderLayout.CENTER);
	}

	private JButton createCarouselButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
		return button;
	}

	private void loadUpcomingActivities() {
		try {
			upcomingActivities = DatabaseManager.getUpcomingActivities();

			if (upcomingActivities == null || upcomingActivities.isEmpty()) {
				carouselLabel.setText("近期沒有活動安排");
				prevButton.setEnabled(false);
				nextButton.setEnabled(false);
				stopButton.setEnabled(false);
			} else {
				// 开始轮播
				startCarousel();
				// 显示第一个活动
				showActivity(0);
			}
		} catch (Exception ex) {
			carouselLabel.setText("加載活動失敗");
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "加載活動失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void startCarousel() {
		if (carouselTimer == null) {
			carouselTimer = new Timer(3000, e -> showNextActivity());
			carouselTimer.start();
		}
	}

	private void stopCarousel() {
		if (carouselTimer != null) {
			carouselTimer.stop();
		}
	}

	private void toggleCarousel() {
		if (carouselTimer != null) {
			if (carouselTimer.isRunning()) {
				carouselTimer.stop();
				stopButton.setText("▶"); // 改为播放图标
			} else {
				carouselTimer.start();
				stopButton.setText("⏸"); // 改为暂停图标
			}
		}
	}

	private void showActivity(int index) {
		if (upcomingActivities == null || upcomingActivities.isEmpty())
			return;

		// 确保索引在有效范围内
		if (index >= upcomingActivities.size())
			index = 0;
		if (index < 0)
			index = upcomingActivities.size() - 1;

		currentActivityIndex = index;
		Activity activity = upcomingActivities.get(currentActivityIndex);

		try {
			byte[] imageData = activity.getImage();
			if (imageData != null && imageData.length > 0) {
				// 从字节数组创建图片
				ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
				BufferedImage originalImage = ImageIO.read(bis);

				// 缩放图片
				int width = carouselPanel.getWidth() - 40;
				int height = carouselPanel.getHeight() - 60;
				Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);

				// 更新轮播标签
				carouselLabel.setIcon(new ImageIcon(scaledImage));
				carouselLabel.setText(null);

				// 添加活动名称提示
				carouselLabel.setToolTipText(activity.getName());
			} else {
				// 如果没有图片，显示文本
				carouselLabel.setIcon(null);
				carouselLabel.setText(activity.getName() + " - 無圖片");
			}
		} catch (Exception ex) {
			carouselLabel.setIcon(null);
			carouselLabel.setText("加載圖片失敗: " + activity.getName());
			ex.printStackTrace();
		}
	}

	private void showActivityDetails(Activity activity) {
		JDialog detailsDialog = new JDialog(this, "活動詳情", true);
		detailsDialog.setSize(500, 400);
		detailsDialog.setLocationRelativeTo(this);
		detailsDialog.setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		contentPanel.setBackground(new Color(245, 250, 255));

		// 标题
		JLabel titleLabel = new JLabel(activity.getName(), JLabel.CENTER);
		titleLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
		titleLabel.setForeground(new Color(0, 70, 140));

		// 信息面板
		JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
		infoPanel.setOpaque(false);
		infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		addInfoRow(infoPanel, "活動類型:", activity.getType());
		addInfoRow(infoPanel, "日期:", activity.getDate().toString());
		addInfoRow(infoPanel, "地點:", activity.getPlace());

		// 描述
		JTextArea descriptionArea = new JTextArea(activity.getDescription());
		descriptionArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 15));
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setEditable(false);
		descriptionArea.setBackground(new Color(245, 250, 255));
		descriptionArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("活动描述"),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));

		JScrollPane scrollPane = new JScrollPane(descriptionArea);

		// 关闭按钮
		JButton closeButton = new JButton("關閉");
		closeButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
		closeButton.addActionListener(e -> detailsDialog.dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		buttonPanel.add(closeButton);

		contentPanel.add(titleLabel, BorderLayout.NORTH);
		contentPanel.add(infoPanel, BorderLayout.CENTER);
		contentPanel.add(scrollPane, BorderLayout.SOUTH);

		detailsDialog.add(contentPanel, BorderLayout.CENTER);
		detailsDialog.add(buttonPanel, BorderLayout.SOUTH);
		detailsDialog.setVisible(true);
	}

	private void addInfoRow(JPanel panel, String label, String value) {
		JPanel rowPanel = new JPanel(new BorderLayout());
		rowPanel.setOpaque(false);

		JLabel labelLbl = new JLabel(label);
		labelLbl.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
		labelLbl.setForeground(new Color(60, 60, 80));

		JLabel valueLbl = new JLabel(value);
		valueLbl.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
		valueLbl.setForeground(new Color(80, 80, 100));

		rowPanel.add(labelLbl, BorderLayout.WEST);
		rowPanel.add(valueLbl, BorderLayout.CENTER);

		panel.add(rowPanel);
	}

	private void showNextActivity() {
		showActivity(currentActivityIndex + 1);
	}

	private void showPreviousActivity() {
		showActivity(currentActivityIndex - 1);
	}

	// 初始化右側功能列
	private void initRightPanel() {
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setBackground(new Color(240, 250, 255));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		identityLabel = new JLabel("目前身份：訪客", JLabel.CENTER);
		identityLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
		identityLabel.setForeground(Color.ORANGE);
		identityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		loginButton = createMenuButton("登入");
		recommendButton = createMenuButton("推薦系統");
		profileButton = createMenuButton("個人中心");
		manageButton = createMenuButton("管理中心");

		loginButton.addActionListener(e -> {
			// 這裡會跳出登入視窗
			if ("登出".equals(loginButton.getText())) {
				// 登出操作
				setCurrentUserId(null);
				updateIdentity("訪客");
				JOptionPane.showMessageDialog(this, "已登出");
			} else {
				// 登入操作
				new LoginPage(this).setVisible(true);
			}
		});

		recommendButton.addActionListener(e -> {
			// 這裡會跳出推薦系統窗
			new RecommendDialog(this);
		});

		profileButton.addActionListener(e -> {
			// 這裡會跳出個人中心
			new StudentFrame(this);
		});

		manageButton.addActionListener(e -> {
			// 這裡會跳出個人中心
			new ManagerFrame(this);
		});

		rightPanel.add(Box.createVerticalStrut(20));
		rightPanel.add(identityLabel);
		rightPanel.add(Box.createVerticalStrut(15));
		rightPanel.add(loginButton);
		rightPanel.add(Box.createVerticalStrut(15));
		rightPanel.add(recommendButton);
		rightPanel.add(Box.createVerticalStrut(15));
		rightPanel.add(profileButton);
		rightPanel.add(Box.createVerticalStrut(15));
		rightPanel.add(manageButton);

		add(rightPanel, BorderLayout.EAST);
	}

	// 建立功能列按鈕
	private JButton createMenuButton(String text) {
		JButton button = new JButton(text);
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setMaximumSize(new Dimension(160, 35));
		button.setFocusPainted(false);
		button.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		return button;
	}

	private void updateClubList() {
		clubInfoPanel.removeAll();
		String keyword = searchField.getText().trim();
		String category = (String) categoryComboBox.getSelectedItem();

		try (Connection conn = DatabaseManager.getConnection()) {
			String sql = "SELECT club_code,name, type, date, expense, contact, description, tags FROM clubs WHERE name LIKE ? ESCAPE '!'";

			if (!"全部".equals(category)) {
				sql += " AND type = ?";
			}
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				keyword = keyword.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");

				stmt.setString(1, "%" + keyword + "%");
				if (!"全部".equals(category)) {
					stmt.setString(2, category);
				}
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					String clubCode = rs.getString("club_code");
					String name = rs.getString("name");
					String type = rs.getString("type");
					String date = rs.getString("date");
					String expense = rs.getString("expense");
					String contact = rs.getString("contact");
					String description = rs.getString("description");
					String tags = rs.getString("tags");

					JPanel clubPanel = new JPanel(new BorderLayout());
					JButton clubButton = new JButton(name + "（" + type + "）");
					clubButton.setFocusPainted(false);
					clubButton.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 14));
					clubButton.setBackground(new Color(230, 240, 255));
					clubButton.setAlignmentX(Component.LEFT_ALIGNMENT);
					clubButton.addActionListener(e -> {
					    showClubDetailsDialog(name, type, date, expense, contact, description, tags);
					});
					clubInfoPanel.add(clubButton);
					clubInfoPanel.add(Box.createVerticalStrut(10));
					// 添加關注按鈕
					JButton followBtn = new JButton("關注");
					followBtn.addActionListener(e -> {
						String userId = getCurrentUserId();
						if (userId == null || userId.isEmpty()) {
							JOptionPane.showMessageDialog(this, "请先登入！");
							return;
						}
						try {
							if (FollowDAO.isFollowing(userId, clubCode)) {
								JOptionPane.showMessageDialog(this, "您已關注该社团");
							} else {
								FollowDAO.followClub(userId, clubCode);
								JOptionPane.showMessageDialog(this, "關注成功！");
							}
						} catch (SQLException ex) {
							ex.printStackTrace();
							JOptionPane.showMessageDialog(this, "操作失敗：" + ex.getMessage());
						}
					});

					JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
					buttonPanel.add(followBtn);
					clubPanel.add(clubButton, BorderLayout.CENTER);
					clubPanel.add(buttonPanel, BorderLayout.EAST);
					clubInfoPanel.add(clubPanel);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "資料庫錯誤：" + ex.getMessage());
		}

		clubInfoPanel.revalidate();
		clubInfoPanel.repaint();
	}

	private void showClubDetailsDialog(String name, String type, String date, String expense, String contact,
			String description, String tags) {
		JDialog dialog = new JDialog(this, name + " 詳細資訊", true);
		dialog.setSize(450, 400);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout());

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		contentPanel.add(new JLabel("📛 社團名稱: " + name));
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JLabel("📂 類型: " + type));
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JLabel("📅 社團時間: " + date));
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JLabel("💰 會費: " + expense));
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JLabel("📞 聯絡方式: " + contact));
		contentPanel.add(Box.createVerticalStrut(5));
		contentPanel.add(new JLabel("🏷️ 標籤: " + tags));
		contentPanel.add(Box.createVerticalStrut(10));

		JTextArea descArea = new JTextArea(description);
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		descArea.setEditable(false);
		descArea.setBorder(BorderFactory.createTitledBorder("📖 社團介紹"));
		JScrollPane scrollPane = new JScrollPane(descArea);
		scrollPane.setPreferredSize(new Dimension(400, 120));
		contentPanel.add(scrollPane);

		JButton closeButton = new JButton("關閉");
		closeButton.addActionListener(e -> dialog.dispose());

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(closeButton);

		dialog.add(contentPanel, BorderLayout.CENTER);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	public void showHome() {
		getContentPane().removeAll();
		initTitle();
		initSearchArea();
		initRightPanel();
		updateIdentity(identity);
		updateClubList();
		revalidate();
		repaint();
	}

	public void setCurrentUserId(String id) {
		currentUserID = id;
	}

	public String getCurrentUserId() {
		return currentUserID;
	}
}

