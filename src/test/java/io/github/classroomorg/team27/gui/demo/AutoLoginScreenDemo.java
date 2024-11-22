package io.github.classroomorg.team27.gui.demo;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

// 다른 클래스와의 상호작용 코드 제외 또는 주석 처리
public class AutoLoginScreenDemo {
	private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
	
	public AutoLoginScreenDemo() {
		frame = new JFrame("Scheduler Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
		frame.setResizable(false);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(autoLoginScreen(), "AutoLogin");
        frame.add(mainPanel);
        frame.setVisible(true);
	}
	
	public JPanel autoLoginScreen() {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());

	    // 상단: 제목
	    JLabel titleLabel = new JLabel("자동로그인 화면", JLabel.CENTER);
	    titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
	    panel.add(titleLabel, BorderLayout.NORTH);

	    // 중앙: 로딩 메시지와 프로그레스 바
	    JPanel centerPanel = new JPanel();
	    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
	    centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // 패널 자체 정렬

	    // 여백 조정을 위한 glue 사용
	    centerPanel.add(Box.createVerticalGlue()); // 위쪽 공간

	    JLabel statusLabel = new JLabel("자동로그인 진행 중...");
	    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    centerPanel.add(statusLabel);

	    JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);
	    progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
	    centerPanel.add(progressBar);
	    
	    centerPanel.add(Box.createVerticalStrut(20)); // 라벨과 프로그레스바 간격
	    centerPanel.add(Box.createVerticalGlue()); // 아래쪽 공간

	    panel.add(centerPanel, BorderLayout.CENTER);

	    // 하단: 버튼
	    JButton goToLoginButton = new JButton("로그인 화면으로 이동");
//	     goToLoginButton.addActionListener(e -> showScreen("Login"));
	    panel.add(goToLoginButton, BorderLayout.SOUTH);

	    return panel;
	}

	public static void main(String[] args) {
		new AutoLoginScreenDemo();

	}

}
