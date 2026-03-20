import javax.swing.*;

public class Test {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 登入後可以接主介面
           MyApp mainFrame = new MyApp();
           mainFrame.setVisible(true);
          
        });
        
    }
}


