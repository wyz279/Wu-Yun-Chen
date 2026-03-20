import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ManagerFrame extends JFrame {
	private MyApp mainApp;
	private JButton faq, passwordChange, backToHome;
	private JButton clubConfirm, clubModify, clubDelete, actConfirm, actModify, actDelete;
	private JPanel mainPanel, clubPanel, actPanel, notifyPanel;
	private JPanel leftPanel, middlePanel, rightPanel;
	private JLabel name1, type1, introduction1, date1, expense1, contact1, name2, type2, introduction2, place2, date2,
			dateSample, tag;// 不能活動跟社團的重複使用
	private JTextField clubName, clubDate, clubExpense, clubContact, actName, actPlace, actDate;
	private JTextArea clubIn, actIn, notify;
	private JScrollPane actScroll, clubScroll, noScroll;
	private JComboBox clubType, actType;
	private JPanel clubButtonPanel, actButtonPanel;
	private JCheckBox tag11, tag12;
	private JCheckBox tag21, tag22;
	private JCheckBox tag31, tag32;
	private JButton actUploadButton, actRemoveImageButton;
	private JLabel actImageLabel;
	private byte[] currentActivityImage;
	private JPanel tagsPanel;
	private Font btnFond = new Font("微軟正黑體", Font.PLAIN, 16);
	private Font inFond = new Font("微軟正黑體", Font.PLAIN, 14);

	public ManagerFrame(MyApp mainApp) {
		this.mainApp = mainApp;
		setSize(1000, 700);
		setTitle("Manager Frame");
		createLabel();
		createTextFeild();
		createComboBox();
		createCheckBox();
		createButton();
		createPanel();
		add(mainPanel);
		showFollowChangeNotifications();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setLocationRelativeTo(null);
	}

	private void createLabel() {
		name1 = new JLabel("名稱:");
		type1 = new JLabel("類型:");
		introduction1 = new JLabel("介紹:");
		date1 = new JLabel("時間:");
		expense1 = new JLabel("社費:");
		contact1 = new JLabel("聯絡:");
		name2 = new JLabel("名稱:");
		type2 = new JLabel("類型:");
		introduction2 = new JLabel("介紹:");
		place2 = new JLabel("地點:");
		date2 = new JLabel("日期:");
		dateSample = new JLabel("        範例: 2025-05-26");
		tag = new JLabel("標籤:");
		JLabel[] labels = { name1, type1, introduction1, date1, expense1, contact1, name2, type2, introduction2, place2,
				date2, tag };
		for (JLabel label : labels) {
			label.setFont(inFond);
		}
	}

	private void createTextFeild() {
		clubName = new JTextField(18);
		clubDate = new JTextField(18);
		clubExpense = new JTextField(18);
		clubContact = new JTextField(18);
		actName = new JTextField(18);
		actPlace = new JTextField(18);
		actDate = new JTextField(18);
		clubIn = new JTextArea(3, 18);// 大於5標籤會消失，因為是GridLayout
		actIn = new JTextArea(3, 18);
		notify = new JTextArea(7, 40);
		notify.setEditable(false);
		actScroll = new JScrollPane(actIn);
		clubScroll = new JScrollPane(clubIn);
		noScroll = new JScrollPane(notify);
		String userId = mainApp.getCurrentUserId();
		ClubInfo club = getClubInfo(userId);
		if (club != null) {
			clubName.setText(club.getName());
			clubDate.setText(club.getDate());
			clubExpense.setText(club.getExpense());
			clubIn.setText(club.getDescription());
			clubContact.setText(club.getContact());
			clubName.setEditable(false);
			clubDate.setEditable(false);
			clubExpense.setEditable(false);
			clubIn.setEditable(false);
			clubContact.setEditable(false);
		}
		// 初始化图片预览标签
		actImageLabel = new JLabel("無圖片", SwingConstants.CENTER);
		actImageLabel.setPreferredSize(new Dimension(150, 130));
		actImageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

		ActivityInfo activity = getActivityInfo(userId);
		if (activity != null) {
			actName.setText(activity.getName());
			actDate.setText(activity.getDate());
			actPlace.setText(activity.getPlace());
			actIn.setText(activity.getDescription());

			if (activity.getImage() != null) {
				currentActivityImage = activity.getImage();
				displayImage(actImageLabel, currentActivityImage);
			}
			actName.setEditable(false);
			actDate.setEditable(false);
			actPlace.setEditable(false);
			actIn.setEditable(false);
		}
	}

	private void createComboBox() {
		clubType = new JComboBox();
		clubType.setPreferredSize(new Dimension(165, 20));
		actType = new JComboBox();
		actType.setPreferredSize(new Dimension(165, 20));
		JComboBox[] boxes = { clubType, actType };
		for (JComboBox box : boxes) {
			box.addItem("藝術");
			box.addItem("學術");
			box.addItem("體適能");
			box.addItem("服務");
			box.addItem("聯誼");
			box.addItem("自治團體");
		}
		String userId = mainApp.getCurrentUserId();
		ClubInfo club = getClubInfo(userId);
		clubType.setSelectedIndex(-1);
		actType.setSelectedIndex(-1);
		if (club != null) {
			clubType.setSelectedItem(club.getType());
			clubType.setEditable(false);
		}
		ActivityInfo activity = getActivityInfo(userId);
		if (activity != null) {
			actType.setSelectedItem(activity.getType());
			actType.setEditable(false);
		}
	}

	private void createCheckBox() {
		tag11 = new JCheckBox("獨自完成");
		tag12 = new JCheckBox("與人互動");
		tag21 = new JCheckBox("嘗試新事物");
		tag22 = new JCheckBox("活用已知知識");
		tag31 = new JCheckBox("講授");
		tag32 = new JCheckBox("實作");
		JCheckBox[] tags = { tag11, tag12, tag21, tag22, tag31, tag32 };
		for (JCheckBox t : tags) {
			t.setFont(inFond);
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
		clubConfirm = new JButton("確認");
		clubModify = new JButton("修改");
		clubDelete = new JButton("刪除");
		actConfirm = new JButton("確認");
		actModify = new JButton("修改");
		actDelete = new JButton("刪除");
		JButton[] lButtons = { clubConfirm, clubModify, clubDelete, actConfirm, actModify, actDelete };
		for (JButton lButton : lButtons) {
			lButton.setFont(inFond);
			lButton.setFocusPainted(false);
			lButton.setPreferredSize(new Dimension(97, 30));
		}
		actUploadButton = new JButton("上傳圖片");
		actUploadButton.setFont(inFond);
		actUploadButton.setFocusPainted(false);
		actUploadButton.setPreferredSize(new Dimension(120, 30));
		actUploadButton.addActionListener(e -> uploadImage());

		// 创建移除图片按钮
		actRemoveImageButton = new JButton("移除圖片");
		actRemoveImageButton.setFont(inFond);
		actRemoveImageButton.setFocusPainted(false);
		actRemoveImageButton.setPreferredSize(new Dimension(120, 30));
		actRemoveImageButton.addActionListener(e -> removeImage());

		clubConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String userId = mainApp.getCurrentUserId();
				ClubInfo club = getClubInfo(userId);

				// 收集輸入欄位資料
				String name = clubName.getText().trim();
				String date = clubDate.getText().trim();
				String expense = clubExpense.getText().trim();
				String contact = clubContact.getText().trim();
				String type = clubType.getSelectedItem() != null ? clubType.getSelectedItem().toString() : "";
				String description = clubIn.getText().trim();

				// 資料欄位檢查
				if (name.isEmpty() || date.isEmpty() || expense.isEmpty() || contact.isEmpty() || type.isEmpty()
						|| description.isEmpty()) {
					JOptionPane.showMessageDialog(null, "請填寫所有欄位！");
					return;
				}

				// 收集勾選項目
				StringJoiner selectedOptions = new StringJoiner(", ");
				JCheckBox[] tag = { tag11, tag12, tag21, tag22, tag31, tag32 };
				for (JCheckBox t : tag) {
					if (t.isSelected()) {
						selectedOptions.add(t.getText());
					}
				}
				String tags = selectedOptions.toString(); 

				boolean success;
				if (club != null) {
					success = updateClubInfo(name, type, date, expense, contact, description, userId, tags);
					if (success) {
						JOptionPane.showMessageDialog(null, "社團資料更新成功！");
						clubName.setEditable(false);
						clubDate.setEditable(false);
						clubType.setEnabled(false);
						clubExpense.setEditable(false);
						clubIn.setEditable(false);
						clubContact.setEditable(false);
						for (JCheckBox t : tag) {
							t.setEnabled(false);
						}
						clubButtonPanel.removeAll();
						clubButtonPanel.add(clubModify);
						clubButtonPanel.add(clubDelete);
						clubButtonPanel.revalidate();
						clubButtonPanel.repaint();
					} else {
						JOptionPane.showMessageDialog(null, "社團資料更新失敗！");
					}
				} else {
					success = insertClub(name, type, date, expense, contact, description, userId, tags);
					if (success) {
						JOptionPane.showMessageDialog(null, "社團資料新增成功！");
						// 只有在新增成功時才鎖定欄位與更新按鈕
						clubName.setEditable(false);
						clubDate.setEditable(false);
						clubType.setEnabled(false);
						clubExpense.setEditable(false);
						clubIn.setEditable(false);
						clubContact.setEditable(false);
						for (JCheckBox t : tag) {
							t.setEnabled(false);
						}

						clubButtonPanel.removeAll();
						clubButtonPanel.add(clubModify);
						clubButtonPanel.add(clubDelete);
						clubButtonPanel.revalidate();
						clubButtonPanel.repaint();
					} else {
						JOptionPane.showMessageDialog(null, "社團資料新增失敗！");
						// 新增失敗就不改變任何狀態
					}
				}
			}
		});

		clubModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				clubName.setEditable(true);
				clubDate.setEditable(true);
				clubType.setEnabled(true);
				clubExpense.setEditable(true);
				clubIn.setEditable(true);
				clubContact.setEditable(true);
				JCheckBox[] tag = { tag11, tag12, tag21, tag22, tag31, tag32 };
				for (JCheckBox t : tag) {
					t.setEnabled(true);
				}
				// 更新按鈕
				clubButtonPanel.removeAll(); // 清除舊按鈕
				clubButtonPanel.add(clubConfirm); // 加入確認按鈕
				clubButtonPanel.revalidate(); // 重新佈局
				clubButtonPanel.repaint(); // 重繪
			}
		});
		clubDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String userId = mainApp.getCurrentUserId();
				deleteClub(userId);

				clubName.setText("");
				clubDate.setText("");
				clubExpense.setText("");
				clubContact.setText("");
				clubIn.setText("");
				clubType.setSelectedIndex(-1);

				actName.setText("");
				actDate.setText("");
				actPlace.setText("");
				actIn.setText("");
				actType.setSelectedIndex(-1);

				clubName.setEditable(true);
				clubDate.setEditable(true);
				clubType.setEnabled(true);
				clubExpense.setEditable(true);
				clubIn.setEditable(true);
				clubContact.setEditable(true);
				actName.setEditable(true);
				actDate.setEditable(true);
				actType.setEnabled(true);
				actPlace.setEditable(true);
				actIn.setEditable(true);
				JCheckBox[] tag = { tag11, tag12, tag21, tag22, tag31, tag32 };
				for (JCheckBox t : tag) {
					t.setEnabled(true);
				}

				// 更新社團按鈕
				clubButtonPanel.removeAll();
				clubButtonPanel.add(clubConfirm);
				clubButtonPanel.revalidate();
				clubButtonPanel.repaint();

				// 更新活動按鈕
				actButtonPanel.removeAll();
				actButtonPanel.add(actConfirm);
				actButtonPanel.revalidate();
				actButtonPanel.repaint();
			}
		});

		actConfirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String userId = mainApp.getCurrentUserId();
				ActivityInfo activity = getActivityInfo(userId); // 判斷是否已有活動資料

				// 收集欄位資料
				String name = actName.getText().trim();
				String date = actDate.getText().trim();
				String place = actPlace.getText().trim();
				String type = actType.getSelectedItem() != null ? actType.getSelectedItem().toString() : "";
				String description = actIn.getText().trim();

				// 資料檢查
				if (name.isEmpty() || date.isEmpty() || place.isEmpty() || type.isEmpty() || description.isEmpty()) {
					JOptionPane.showMessageDialog(null, "請填寫所有欄位！");
					return;
				}

				boolean success;
				if (activity != null) {
					// 更新活動
					success = updateActInfo(name, type, date, place, description, userId, currentActivityImage);
					if (success) {
						JOptionPane.showMessageDialog(null, "活動資料更新成功！");
					} else {
						JOptionPane.showMessageDialog(null, "活動資料更新失敗！");
					}
				} else {
					// 新增活動
					success = insertActInfo(name, type, date, place, description, userId, currentActivityImage);
					if (success) {
						JOptionPane.showMessageDialog(null, "活動資料新增成功！");
					} else {
						JOptionPane.showMessageDialog(null, "活動資料新增失敗！");
					}
				}

				if (success) {
					// 鎖定欄位
					actName.setEditable(false);
					actDate.setEditable(false);
					actType.setEnabled(false);
					actPlace.setEditable(false);
					actIn.setEditable(false);

					// 恢復按鈕狀態
					actButtonPanel.removeAll();
					actButtonPanel.add(actModify);
					actButtonPanel.add(actDelete);
					actButtonPanel.revalidate();
					actButtonPanel.repaint();
				}
			}
		});

		actModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				actName.setEditable(true);
				actDate.setEditable(true);
				actType.setEnabled(true);
				actPlace.setEditable(true);
				actIn.setEditable(true);
				// 更新按鈕
				actButtonPanel.removeAll(); // 清除舊按鈕
				actButtonPanel.add(actConfirm); // 加入確認按鈕
				actButtonPanel.revalidate(); // 重新佈局
				actButtonPanel.repaint(); // 重繪
			}
		});
		actDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String userId = mainApp.getCurrentUserId();
				boolean deleted = deleteAct(userId);
				if (deleted) {
					actName.setText("");
					actDate.setText("");
					actPlace.setText("");
					actIn.setText("");
					actType.setSelectedIndex(-1);
					actName.setEditable(true);
					actDate.setEditable(true);
					actType.setEnabled(true);
					actPlace.setEditable(true);
					actIn.setEditable(true);
					// 更新按鈕
					actButtonPanel.removeAll(); // 清除舊按鈕
					actButtonPanel.add(actConfirm); // 加入確認按鈕
					actButtonPanel.revalidate(); // 重新佈局
					actButtonPanel.repaint(); // 重繪
				}
			}
		});
	}

	private void createPanel() {
		clubPanel = new JPanel();
		clubPanel.setLayout(new BoxLayout(clubPanel, BoxLayout.Y_AXIS));
		clubPanel.setPreferredSize(new Dimension(400, 650));
		clubPanel.setBorder(BorderFactory.createTitledBorder("刊登/修改社團"));
		clubPanel.add(Box.createVerticalStrut(10));
		JPanel clubPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 10));// (水平間距,垂直間距(在上方))
		clubPanel1.add(name1);
		clubPanel1.add(clubName);
		clubPanel.add(clubPanel1);
		JPanel clubPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		clubPanel2.add(type1);
		clubPanel2.add(clubType);
		clubPanel.add(clubPanel2);
		JPanel clubPanel3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		clubPanel3.add(date1);
		clubPanel3.add(clubDate);
		clubPanel.add(clubPanel3);
		JPanel clubPanel4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		clubPanel4.add(expense1);
		clubPanel4.add(clubExpense);
		clubPanel.add(clubPanel4);
		JPanel clubPanel5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		clubPanel5.add(contact1);
		clubPanel5.add(clubContact);
		clubPanel.add(clubPanel5);
		JPanel clubPanel6 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		clubPanel6.add(introduction1);
		clubPanel6.add(clubScroll);
		clubPanel.add(clubPanel6);
		JPanel clubPanel7 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		clubPanel7.add(tag11);
		clubPanel7.add(tag12);
		JPanel clubPanel8 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		clubPanel8.add(tag21);
		clubPanel8.add(tag22);
		JPanel clubPanel9 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		clubPanel9.add(tag31);
		clubPanel9.add(tag32);
		JPanel clubPanel10 = new JPanel(new GridLayout(3, 1));
		clubPanel10.add(clubPanel7);
		clubPanel10.add(clubPanel8);
		clubPanel10.add(clubPanel9);
		JPanel clubPanel11 = new JPanel(new FlowLayout(FlowLayout.CENTER, 13, 0));
		clubPanel11.add(tag);
		clubPanel11.add(clubPanel10);
		clubPanel.add(clubPanel11);
		String userId = mainApp.getCurrentUserId();
		ClubInfo club = getClubInfo(userId);
		clubButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 8));
		if (club == null) {
			clubButtonPanel.add(clubConfirm);
		} else {
			clubButtonPanel.add(clubModify);
			clubButtonPanel.add(clubDelete);
		}
		clubPanel.add(clubButtonPanel);
		if (club != null) {
			clubName.setEditable(false);
			clubDate.setEditable(false);
			clubType.setEnabled(false);
			clubExpense.setEditable(false);
			clubIn.setEditable(false);
			clubContact.setEditable(false);
			JCheckBox[] tag = { tag11, tag12, tag21, tag22, tag31, tag32 };
			for (JCheckBox t : tag) {
				t.setEnabled(false);
			}
		}

		actPanel = new JPanel();
		actPanel.setLayout(new BoxLayout(actPanel, BoxLayout.Y_AXIS));
		actPanel.setPreferredSize(new Dimension(390, 490));
		actPanel.setBorder(BorderFactory.createTitledBorder("刊登/修改活動"));
		actPanel.add(Box.createVerticalStrut(10));
		JPanel actPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		actPanel1.add(name2);
		actPanel1.add(actName);
		actPanel.add(actPanel1);
		JPanel actPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5));
		actPanel2.add(type2);
		actPanel2.add(actType);
		actPanel.add(actPanel2);
		JPanel actPanel3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5));
		actPanel3.add(date2);
		actPanel3.add(actDate);
		actPanel.add(actPanel3);
		JPanel actPanel4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		actPanel4.add(dateSample);
		actPanel.add(actPanel4);
		JPanel actPanel5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 5));
		actPanel5.add(place2);
		actPanel5.add(actPlace);
		actPanel.add(actPanel5);
		JPanel actPanel6 = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 0));
		actPanel6.add(introduction2);
		actPanel6.add(actScroll);
		actPanel.add(actPanel6);

		JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
		imagePanel.add(actUploadButton);
		imagePanel.add(actRemoveImageButton);

		JPanel imagePreviewPanel = new JPanel(new BorderLayout());
		imagePreviewPanel.add(new JLabel("活動圖片:"), BorderLayout.NORTH);
		imagePreviewPanel.add(actImageLabel, BorderLayout.CENTER);

		// 将图片组件添加到活动面板
		actPanel.add(Box.createVerticalStrut(10));
		actPanel.add(imagePreviewPanel);
		actPanel.add(imagePanel);

		ActivityInfo activity = getActivityInfo(userId);
		actButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 8));
		if (activity == null) {
			actButtonPanel.add(actConfirm);
		} else {
			actButtonPanel.add(actModify);
			actButtonPanel.add(actDelete);
		}
		
		actPanel.add(actButtonPanel);
		notifyPanel = new JPanel();
		notifyPanel.setPreferredSize(new Dimension(390, 160));
		notifyPanel.setBorder(BorderFactory.createTitledBorder("通知、公告"));
		notifyPanel.add(noScroll);
		leftPanel = new JPanel();
		leftPanel.setPreferredSize(new Dimension(400, 700));
		leftPanel.add(clubPanel);
		middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.setPreferredSize(new Dimension(400, 700));
		JScrollPane actScrollPane = new JScrollPane(actPanel);
		actScrollPane.setPreferredSize(new Dimension(400, 450));
		middlePanel.add(actPanel);
		middlePanel.add(Box.createVerticalStrut(10)); // 添加間距
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

	private void uploadImage() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("選擇活動圖片");
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("圖片文件", "jpg", "jpeg", "png", "gif"));

		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File selectedFile = fileChooser.getSelectedFile();
				Path path = selectedFile.toPath();
				currentActivityImage = Files.readAllBytes(path);
				displayImage(actImageLabel, currentActivityImage);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this, "讀取圖片失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	// 移除图片方法
	private void removeImage() {
		currentActivityImage = null;
		actImageLabel.setIcon(null);
		actImageLabel.setText("無圖片");
	}

	// 显示图片方法
	private void displayImage(JLabel label, byte[] imageData) {
		if (imageData == null || imageData.length == 0) {
			label.setIcon(null);
			label.setText("無圖片");
			return;
		}

		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
			BufferedImage originalImage = ImageIO.read(bis);

			// 调整图片大小以适应标签
			int labelWidth = label.getWidth() > 0 ? label.getWidth() : 150;
			int labelHeight = label.getHeight() > 0 ? label.getHeight() : 150;

			Image scaledImage = originalImage.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);

			label.setIcon(new ImageIcon(scaledImage));
			label.setText(null);
		} catch (IOException ex) {
			label.setIcon(null);
			label.setText("圖片加載失敗");
			JOptionPane.showMessageDialog(this, "圖片加載失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
		}
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

	public class ClubInfo {
		private String name;
		private String type;
		private String date;
		private String expense;
		private String contact;
		private String description;

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

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
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
	}

	private ClubInfo getClubInfo(String studentID) {
		String sql = "SELECT name, type, date, expense, contact, description FROM clubs WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, studentID);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				ClubInfo info = new ClubInfo();
				info.setName(rs.getString("name"));
				info.setType(rs.getString("type"));
				info.setDate(rs.getString("date"));
				info.setExpense(rs.getString("expense"));
				info.setContact(rs.getString("contact"));
				info.setDescription(rs.getString("description"));
				return info;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean updateClubInfo(String name, String type, String date, String expense, String contact,
			String description, String userId, String tags) {
		String sql = "UPDATE clubs SET name = ?, type = ?, date = ?, expense = ?, contact = ?, description = ? , tags = ? WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, name);
			stmt.setString(2, type);
			stmt.setString(3, date);
			stmt.setString(4, expense);
			stmt.setString(5, contact);
			stmt.setString(6, description);
			stmt.setString(7, tags);
			stmt.setString(8, userId);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean insertClub(String name, String type, String date, String expense, String contact,
			String description, String userId, String tags) {
		String prefix = getClubCodePrefix(clubType.getSelectedItem().toString());
		String clubCode = generateNextClubCode(prefix); 

		String sql = "INSERT INTO clubs (club_code, name, type, date, expense, contact, description, account, tags) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, clubCode);
			stmt.setString(2, name);
			stmt.setString(3, type);
			stmt.setString(4, date);
			stmt.setString(5, expense);
			stmt.setString(6, contact);
			stmt.setString(7, description);
			stmt.setString(8, userId);
			stmt.setString(9, tags);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getClubCodePrefix(String type) {
		switch (type) {
		case "自治團體":
			return "A";
		case "學術":
			return "B";
		case "藝術":
			return "C";
		case "聯誼":
			return "D";
		case "服務":
			return "E";
		case "體適能":
			return "F";
		default:
			return null;
		}
	}

	//找出資料庫目前最大號碼，然後 +1
	private String generateNextClubCode(String prefix) {
		String sql = "SELECT club_code FROM clubs WHERE club_code LIKE ? ORDER BY club_code DESC LIMIT 1";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, prefix + "%");
			ResultSet rs = stmt.executeQuery();
			int nextNumber = 1;

			if (rs.next()) {
				String lastCode = rs.getString("club_code");
				String numberPart = lastCode.substring(1); 
				nextNumber = Integer.parseInt(numberPart) + 1;
			}

			return String.format("%s%03d", prefix, nextNumber); 

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void deleteClub(String userId) {
		int confirm = JOptionPane.showConfirmDialog(null, "確定要刪除此社團資料嗎？\n（相關活動也將一併刪除）", "刪除確認",
				JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try (Connection conn = DatabaseManager.getConnection()) {
				conn.setAutoCommit(false); // 使用交易控制確保一致性

				// 先刪除活動資料
				String deleteActivitiesSql = "DELETE FROM activities WHERE account = ?";
				try (PreparedStatement psAct = conn.prepareStatement(deleteActivitiesSql)) {
					psAct.setString(1, userId);
					psAct.executeUpdate();
				}

				// 再刪除社團資料
				String deleteClubSql = "DELETE FROM clubs WHERE account = ?";
				try (PreparedStatement psClub = conn.prepareStatement(deleteClubSql)) {
					psClub.setString(1, userId);
					int rowsAffected = psClub.executeUpdate();

					if (rowsAffected > 0) {
						conn.commit(); 
						JOptionPane.showMessageDialog(null, "社團與相關活動刪除成功！");
					} else {
						conn.rollback(); 
						JOptionPane.showMessageDialog(null, "找不到要刪除的社團。");
					}
				}

			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "刪除失敗：" + e.getMessage());
			}
		}
	}

	public class ActivityInfo {
		private String name;
		private String type;
		private String date;
		private String place;
		private String description;
		private byte[] image;

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

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getPlace() {
			return place;
		}

		public void setPlace(String place) {
			this.place = place;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public byte[] getImage() {
			return image;
		}

		public void setImage(byte[] image) {
			this.image = image;
		}
	}

	private ActivityInfo getActivityInfo(String studentID) {
		String sql = "SELECT name, type, date, place, description, image FROM activities WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, studentID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				ActivityInfo activity = new ActivityInfo();
				activity.setName(rs.getString("name"));
				activity.setType(rs.getString("type"));
				activity.setDate(rs.getString("date"));
				activity.setPlace(rs.getString("place"));
				activity.setDescription(rs.getString("description"));
				activity.setImage(rs.getBytes("image"));
				return activity;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean updateActInfo(String name, String type, String date, String place, String description,
			String userId, byte[] image) {
		String sql = "UPDATE activities SET name = ?, type = ?, date = ?, place = ?, description = ?, image = ? WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, name);
			stmt.setString(2, type);
			java.sql.Date sqlDate = java.sql.Date.valueOf(date);
			stmt.setDate(3, sqlDate);
			stmt.setString(4, place);
			stmt.setString(5, description);
			stmt.setBytes(6, image);
			stmt.setString(7, userId);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean insertActInfo(String name, String type, String date, String place, String description,
			String userId, byte[] image) {
		String prefix = getClubCodePrefix(actType.getSelectedItem().toString());
		String actCode = generateNextActCode(prefix);

		String clubName = getClubNameByUserId(userId);
		if (clubName == null) {
			JOptionPane.showMessageDialog(null, "找不到對應的社團名稱，無法新增活動，請先刊登社團");
			return false;
		}

		String sql = "INSERT INTO activities (activities_code, name, type, date, place, description, club, account, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, actCode);
			stmt.setString(2, name);
			stmt.setString(3, type);
			java.sql.Date sqlDate = java.sql.Date.valueOf(date);
			stmt.setDate(4, sqlDate);
			stmt.setString(5, place);
			stmt.setString(6, description);
			stmt.setString(7, clubName);
			stmt.setString(8, userId); 
			stmt.setBytes(9, image);

			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getActCodePrefix(String type) {
		switch (type) {
		case "自治團體":
			return "A";
		case "學術":
			return "B";
		case "藝術":
			return "C";
		case "聯誼":
			return "D";
		case "服務":
			return "E";
		case "體適能":
			return "F";
		default:
			return null;
		}
	}

	//找出資料庫目前最大號碼，然後 +1
	private String generateNextActCode(String prefix) {
		String sql = "SELECT activities_code FROM activities WHERE activities_code LIKE ? ORDER BY activities_code DESC LIMIT 1";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, prefix + "%");
			ResultSet rs = stmt.executeQuery();
			int nextNumber = 1;

			if (rs.next()) {
				String lastCode = rs.getString("activities_code");
				String numberPart = lastCode.substring(1); 
				nextNumber = Integer.parseInt(numberPart) + 1;
			}

			return String.format("%s%03d", prefix, nextNumber);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getClubNameByUserId(String userId) {
		String sql = "SELECT name FROM clubs WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("name");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getClubCodeByUserId(String userId) {
		String sql = "SELECT club_code FROM clubs WHERE account = ?";
		try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, userId);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("club_code");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean deleteAct(String userId) {
		int confirm = JOptionPane.showConfirmDialog(null, "確定要刪除此活動資料嗎？", "刪除確認", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			try (Connection conn = DatabaseManager.getConnection()) {
				String sql = "DELETE FROM activities WHERE account = ?";
				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setString(1, userId);
				int rowsAffected = ps.executeUpdate();
				if (rowsAffected > 0) {
					JOptionPane.showMessageDialog(null, "刪除成功");
					return true;
				} else {
					JOptionPane.showMessageDialog(null, "找不到要刪除的活動");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "刪除失敗：" + e.getMessage());
			}
		}
		return false;
	}

	private void showFollowChangeNotifications() {
		try {
			String userId = mainApp.getCurrentUserId();
			String clubCode = getClubCodeByUserId(userId);
			Map<LocalDate, Integer> followStats = FollowDAO.getCumulativeFollowCountLast7Days(clubCode);

			//結果及變動
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
			ArrayList<String> lines = new ArrayList<>();

			Integer previous = null;
			for (Map.Entry<LocalDate, Integer> entry : followStats.entrySet()) {
				LocalDate date = entry.getKey();
				int current = entry.getValue();
				String dateStr = date.format(formatter);

				StringBuilder line = new StringBuilder();
				line.append(String.format("%s：追蹤總人數為 %d 人", dateStr, current));

				if (previous != null) {
					int diff = current - previous;
					if (diff != 0) {
						line.append(String.format("，追蹤總人數變動為 %d 人", Math.abs(diff)));
					} else {
						line.append("，追蹤總人數無變動");
					}
				}

				lines.add(line.toString());
				previous = current;
			}

			// 顯示
			Collections.reverse(lines);
			notify.setText(String.join("\n", lines));

		} catch (SQLException e) {
			e.printStackTrace();
			notify.setText("取得追蹤通知失敗！");
		}
	}
}

