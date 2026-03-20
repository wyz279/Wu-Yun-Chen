import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// 建立一個名為 RecommendDialog 的類別，繼承 JDialog，用來顯示推薦社團的彈出式視窗
public class RecommendDialog extends JDialog {
	// 儲存使用者在問卷中所選的有興趣的類別
	private final List<String> interests = new ArrayList<>();
	private StringBuilder prefer = new StringBuilder();
	private String prefer1 = "", prefer2 = "", prefer3 = "";

	// 所有可供選擇的社團類別
	private final String[] categories = { "音樂", "美術", "表演藝術", "文學", "美食", "舞蹈", "學術研究", "公益服務", "運動", "學生自治",
			"活動籌備", "語言學習", "社會議題", "宗教" };


	// 建構子，設定 dialog 的基本屬性，並呼叫第一題
	public RecommendDialog(JFrame parent) {
		super(parent, "推薦系統", true); // 設定為模態視窗
		setSize(600, 350); // 尺寸為 600x350
		setLocationRelativeTo(parent); // 置中顯示
		setLayout(new BorderLayout());
		setResizable(false);
		showQuestion1(); // 啟動第一題
		setVisible(true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);//
		setLocationRelativeTo(null);
	}

	// 問題 1：選擇有興趣的類別
	private void showQuestion1() {
		JPanel panel = createMultiSelectPanel("1. 請選出「有興趣」或「擅長」的類別（可複選）", categories, interests);
		setContentPane(panel);
		revalidate();
	}

	// 問題 2：選擇偏好（單獨行動或與人互動）
	private void showQuestion2() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel label = new JLabel("2. 我比較喜歡＿＿。", SwingConstants.CENTER);
		label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
		panel.add(label, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel(new FlowLayout());
		JRadioButton option1 = new JRadioButton("單獨行動");
		JRadioButton option2 = new JRadioButton("與人互動");
		option1.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		option2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		ButtonGroup group = new ButtonGroup(); // 確保單選
		group.add(option1);
		group.add(option2);
		optionsPanel.add(option1);
		optionsPanel.add(option2);

		JButton next = new JButton("確認");
		next.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
		next.addActionListener((ActionEvent e) -> {
			if (option1.isSelected()) {
				prefer1 = " 獨自完成 ";
			}
			else if (option2.isSelected()) {
				prefer1 = " 與人互動 ";
			}
			showQuestion3();
		});

		panel.add(optionsPanel, BorderLayout.CENTER);
		panel.add(next, BorderLayout.SOUTH);
		setContentPane(panel);
		revalidate();
	}

	// 問題 3：是否喜歡挑戰新事物
	private void showQuestion3() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel label = new JLabel("3. 喜歡嘗試或挑戰新事物嗎？", SwingConstants.CENTER);
		label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
		panel.add(label, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel(new FlowLayout());
		JRadioButton option1 = new JRadioButton("是");
		JRadioButton option2 = new JRadioButton("否");
		option1.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		option2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		ButtonGroup group = new ButtonGroup();
		group.add(option1);
		group.add(option2);
		optionsPanel.add(option1);
		optionsPanel.add(option2);

		JButton next = new JButton("確認");
		next.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
		next.addActionListener((ActionEvent e) -> {
			if (option1.isSelected()) {
				prefer2 = " 嘗試新事物 ";
			}
			else if (option2.isSelected()) {
				prefer2 = " 活用已知知識 ";
			}
			showQuestion4();
		});

		panel.add(optionsPanel, BorderLayout.CENTER);
		panel.add(next, BorderLayout.SOUTH);
		setContentPane(panel);
		revalidate();
	}

	// 問題 4：課程偏好（講授或實作）
	private void showQuestion4() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel label = new JLabel("4. 我希望社團課程＿＿部分多一點。", SwingConstants.CENTER);
		label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
		panel.add(label, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel(new FlowLayout());
		JRadioButton option1 = new JRadioButton("講授");
		JRadioButton option2 = new JRadioButton("實作");
		option1.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		option2.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
		ButtonGroup group = new ButtonGroup();
		group.add(option1);
		group.add(option2);
		optionsPanel.add(option1);
		optionsPanel.add(option2);

		JButton finish = new JButton("確認");
		finish.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
		finish.addActionListener((ActionEvent e) -> {
			if (option1.isSelected()) {
				prefer3 = " 講授 ";
			}
			else if (option2.isSelected()) {
				prefer3 = " 實作 ";
			}
			showRecommendation(); // 顯示推薦結果
		});

		panel.add(optionsPanel, BorderLayout.CENTER);
		panel.add(finish, BorderLayout.SOUTH);
		setContentPane(panel);
		revalidate();
	}

	private void showRecommendation() {
	    List<String> recommendations = new ArrayList<>();

	    String sql = "SELECT name, club_code, tags FROM clubs";


	    try (Connection conn = DatabaseManager.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            String club_code = rs.getString("club_code");
	            String name = rs.getString("name");
	            String tags = rs.getString("tags");


	            // 先判斷社團代碼第一字母是否在 prefer 字串中存在
	            boolean codeMatch = false;
	            for (int i = 0; i < prefer.length(); i++) {
	                if (prefer.charAt(i) == club_code.charAt(0)) {
	                    codeMatch = true;
	                    break;
	                }
	            }
	            if (!codeMatch) {
	                continue;  
	            }

	            // 判斷 tags 是否包含 prefer1~3 (非空才判斷)
	            boolean prefer1Match = prefer1.isEmpty() || (tags != null && tags.contains(prefer1.trim()));
	            boolean prefer2Match = prefer2.isEmpty() || (tags != null && tags.contains(prefer2.trim()));
	            boolean prefer3Match = prefer3.isEmpty() || (tags != null && tags.contains(prefer3.trim()));

	            if (prefer1Match && prefer2Match && prefer3Match) {
	                recommendations.add(name);
	            }
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(this, "資料庫連線或查詢錯誤：" + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
	    }

	    // 顯示推薦結果畫面
	    JPanel panel = new JPanel();
	    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	    JLabel title = new JLabel("          推薦結果", SwingConstants.CENTER);
	    title.setFont(new Font("Microsoft JhengHei", Font.BOLD, 24));
	    title.setAlignmentX(Component.CENTER_ALIGNMENT);
	    panel.add(title);
	    panel.add(Box.createVerticalStrut(20));

	    if (recommendations.isEmpty()) {
	        JLabel noMatch = new JLabel("很抱歉，沒有符合您偏好的社團。", SwingConstants.CENTER);
	        noMatch.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
	        panel.add(noMatch);
	    } else {
	    	// 顯示一次「推薦社團：」的標題
	    	JLabel headerLabel = new JLabel("推薦社團：");
	    	headerLabel.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18));
	    	panel.add(headerLabel);
	    	panel.add(Box.createVerticalStrut(10)); // 空行

	    	// 將推薦社團組合成一段文字
	    	StringBuilder sb = new StringBuilder();
	    	for (String clubName : recommendations) {
	    	    sb.append(clubName).append("\n");
	    	}

	    	// 建立 JTextArea 來顯示所有推薦社團
	    	JTextArea textArea = new JTextArea(sb.toString());
	    	textArea.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
	    	textArea.setEditable(false);
	    	textArea.setLineWrap(true);
	    	textArea.setWrapStyleWord(true);

	    	// 加入 JScrollPane
	    	JScrollPane scrollPane = new JScrollPane(textArea);
	    	scrollPane.setPreferredSize(new Dimension(350, 300)); // 可依視窗大小調整
	    	panel.add(scrollPane);

	    }

	    setContentPane(panel);
	    revalidate();
	}






	// 建立複選題用的版面元件，共用此方法產生問題 1 和 2 的界面//改了，不知道怎麼運作
	private JPanel createMultiSelectPanel(String question, String[] options, List<String> targetList) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel label = new JLabel(question, SwingConstants.LEFT);
		label.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 18));
		panel.add(label, BorderLayout.NORTH);

		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridLayout(0, 3, 10, 10)); // 三欄排列
		JCheckBox[] boxes = new JCheckBox[options.length];
		for (int i = 0; i < options.length; i++) {
			boxes[i] = new JCheckBox(options[i]);
			boxes[i].setFont(new Font("Microsoft JhengHei", Font.PLAIN, 16));
			optionsPanel.add(boxes[i]);
		}

		JButton next = new JButton("確認");
		next.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
		next.addActionListener(e -> {
			targetList.clear();
			for (JCheckBox box : boxes) {
				if (box.isSelected()) {
					String hobby = box.getText();
					targetList.add(hobby);
					if(hobby.equals("音樂")) {
						prefer.append("C");
					}
					if(hobby.equals("美術")) {
						prefer.append("C");
					}
					if(hobby.equals("表演藝術")) {
						prefer.append("C");
					}
					if(hobby.equals("文學")) {
						prefer.append("B");
					}
					if(hobby.equals("美食")) {
						prefer.append("B");
					}
					if(hobby.equals("舞蹈")) {
						prefer.append("C");
					}
					if(hobby.equals("學術研究")) {
						prefer.append("B");
					}
					if(hobby.equals("公益服務")) {
						prefer.append("E");
					}
					if(hobby.equals("運動")) {
						prefer.append("F");
					}
					if(hobby.equals("學生自治")) {
						prefer.append("A");
					}
					if(hobby.equals("活動籌備")) {
						prefer.append("A");
					}
					if(hobby.equals("語言學習")) {
						prefer.append("B");
					}
					if(hobby.equals("社會議題")) {
						prefer.append("D");
					}
					if(hobby.equals("宗教")) {
						prefer.append("D");
					}
				}
			}
				showQuestion2();
		});

		panel.add(optionsPanel, BorderLayout.CENTER);
		panel.add(next, BorderLayout.SOUTH);
		return panel;
	}
	
}

