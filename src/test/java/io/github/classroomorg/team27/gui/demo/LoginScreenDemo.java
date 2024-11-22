package io.github.classroomorg.team27.gui.demo;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
//import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// 다른 클래스와의 상호작용 코드 제외 또는 주석 처리
public class LoginScreenDemo {
	private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public LoginScreenDemo() {
		frame = new JFrame("Scheduler Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
		frame.setResizable(false);
        
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(loginScreen(), "Login");
        frame.add(mainPanel);
        frame.setVisible(true);
	}
    
    // 로그인 화면
    public JPanel loginScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // 중앙: 전체 컴포넌트를 포함할 패널
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // 위쪽 유연한 공간 추가 (컴포넌트들을 정중앙에 배치)
        centerPanel.add(Box.createVerticalGlue());

        // 프로그램 이름 라벨
        JLabel programNameLabel = new JLabel("Task Manager", JLabel.CENTER);
        programNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        programNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(programNameLabel);

        centerPanel.add(Box.createVerticalStrut(10)); // 프로그램 이름과 ID 라벨 간격

        // ID 라벨 및 입력 필드
        JLabel idLabel = new JLabel("ID:");
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(idLabel);

        JTextField idField = new JTextField(20);
        idField.setMaximumSize(new Dimension(300, 30));
        idField.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(idField);

        centerPanel.add(Box.createVerticalStrut(10)); // ID와 Password 간격

        // Password 라벨 및 입력 필드
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(300, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(passwordField);

        centerPanel.add(Box.createVerticalStrut(10)); // Password와 체크박스 간격

        // 자동 로그인 체크박스
        JCheckBox autoLoginCheckBox = new JCheckBox("자동 로그인");
        autoLoginCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(autoLoginCheckBox);

        centerPanel.add(Box.createVerticalStrut(10)); // 체크박스와 버튼 간격

        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton loginButton = new JButton("로그인");
//        loginButton.addActionListener(e -> {
//            boolean autoLogin = autoLoginCheckBox.isSelected();
//            String id = idField.getText();
//            String password = new String(passwordField.getPassword());
//
//            int loginResult = user.login(id, password, autoLogin);
//
//            if (loginResult == 1) {
//                showScreen("Main");
//            } else if (loginResult == 2) {
//                JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
//            } else if (loginResult == 3) {
//                JOptionPane.showMessageDialog(panel, "비밀번호가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
//            } else {
//                JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
//            }
//        });
        buttonPanel.add(loginButton);

        JButton registerButton = new JButton("회원가입");
//        registerButton.addActionListener(e -> showScreen("Register"));
        buttonPanel.add(registerButton);

        JButton findPasswordButton = new JButton("비밀번호 찾기");
//        findPasswordButton.addActionListener(e -> showScreen("FindPassword"));
        buttonPanel.add(findPasswordButton);

        centerPanel.add(buttonPanel);

        // 아래쪽 유연한 공간 추가 (컴포넌트들을 정중앙에 배치)
        centerPanel.add(Box.createVerticalGlue());

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }


	public static void main(String[] args) {
		new LoginScreenDemo();

	}

}
