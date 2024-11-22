package schedulerAlertApp;

import javax.swing.*;
import java.awt.*;

public class SchedulerInterface {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

    private Register user;
    private Scheduler scheduler;

    public SchedulerInterface(Register user, Scheduler scheduler) {
        this.user = user;
        this.scheduler = scheduler;

        // JFrame 설정
        frame = new JFrame("Scheduler Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
		frame.setResizable(false);

        // CardLayout과 mainPanel 설정
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 화면 추가
        mainPanel.add(autoLoginScreen(), "AutoLogin");
        mainPanel.add(loginScreen(), "Login");
        mainPanel.add(mainScreen(), "Main");

        // 메인 패널을 프레임에 추가
        frame.add(mainPanel);

     // 자동 로그인 로직
        if (user.autoLogin()) {
            cardLayout.show(mainPanel, "Main"); // 메인 화면으로 전환
        } else {
            cardLayout.show(mainPanel, "Login"); // 로그인 화면으로 전환
        }

        frame.setVisible(true);
    }

    // 오토로그인 화면
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
	    goToLoginButton.addActionListener(e -> showScreen("Login"));
	    panel.add(goToLoginButton, BorderLayout.SOUTH);

	    return panel;
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
	        loginButton.addActionListener(e -> {
	            boolean autoLogin = autoLoginCheckBox.isSelected();
	            String id = idField.getText();
	            String password = new String(passwordField.getPassword());
	
	            int loginResult = user.login(id, password, autoLogin);
	
	            if (loginResult == 1) {
	                showScreen("Main");
	            } else if (loginResult == 2) {
	                JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
	            } else if (loginResult == 3) {
	                JOptionPane.showMessageDialog(panel, "비밀번호가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
	            } else {
	                JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
	            }
	        });
	        buttonPanel.add(loginButton);

	        JButton registerButton = new JButton("회원가입");
	        registerButton.addActionListener(e -> showScreen("Register"));
	        buttonPanel.add(registerButton);

	        JButton findPasswordButton = new JButton("비밀번호 찾기");
	        findPasswordButton.addActionListener(e -> showScreen("FindPassword"));
	        buttonPanel.add(findPasswordButton);

	        centerPanel.add(buttonPanel);

	        // 아래쪽 유연한 공간 추가 (컴포넌트들을 정중앙에 배치)
	        centerPanel.add(Box.createVerticalGlue());

	        panel.add(centerPanel, BorderLayout.CENTER);

	        return panel;
	    }
    
    public JPanel mainScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("Main Screen");
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }
    
    public JPanel findPasswordScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // 위쪽 유연한 공간 추가 (전체 컴포넌트를 중앙 배치)
        panel.add(Box.createVerticalGlue());

        // ID 입력 필드
        JLabel idLabel = new JLabel("ID:");
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idLabel);

        JTextField idField = new JTextField(20);
        idField.setMaximumSize(new Dimension(300, 30));
        idField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idField);

        panel.add(Box.createVerticalStrut(10)); // 컴포넌트 간 간격

        // Question Index 선택
        JLabel questionLabel = new JLabel("본인 확인 질문:");
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(questionLabel);

        JComboBox<String> questionComboBox = new JComboBox<>(new String[] {
            "질문 1: 당신의 출생지는?",
            "질문 2: 당신의 첫 번째 반려동물 이름은?",
            "질문 3: 당신이 다닌 첫 번째 학교는?"
        });
        questionComboBox.setMaximumSize(new Dimension(300, 30));
        questionComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(questionComboBox);

        panel.add(Box.createVerticalStrut(10)); // 컴포넌트 간 간격

        // Answer 입력 필드
        JLabel answerLabel = new JLabel("답변:");
        answerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(answerLabel);

        JTextField answerField = new JTextField(20);
        answerField.setMaximumSize(new Dimension(300, 30));
        answerField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(answerField);

        panel.add(Box.createVerticalStrut(10)); // 컴포넌트 간 간격

        // New Password 입력 필드
        JLabel newPasswordLabel = new JLabel("새 비밀번호:");
        newPasswordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(newPasswordLabel);

        JPasswordField newPasswordField = new JPasswordField(20);
        newPasswordField.setMaximumSize(new Dimension(300, 30));
        newPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(newPasswordField);

        panel.add(Box.createVerticalStrut(10)); // 컴포넌트 간 간격

        // PW 재설정 버튼
        JButton resetButton = new JButton("비밀번호 재설정");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> {
            String id = idField.getText();
            int questionIndex = questionComboBox.getSelectedIndex(); // 선택된 질문의 인덱스
            String questionAns = answerField.getText();
            String newPassword = new String(newPasswordField.getPassword());

            // Register 클래스의 findPassword 메서드 호출
            int result = user.findPassword(id, questionIndex, questionAns, newPassword);

            // 결과 처리
            if (result == 1) {
                JOptionPane.showMessageDialog(panel, "비밀번호 재설정 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                showScreen("Login"); // 로그인 화면으로 전환
            } else if (result == 2) {
                JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else if (result == 3) {
                JOptionPane.showMessageDialog(panel, "질문 정보 또는 답변이 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(resetButton);

        // 아래쪽 유연한 공간 추가 
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    // 화면 전환 메서드
    public void showScreen(String name) {
        cardLayout.show(mainPanel, name);
    }

    // main() 메서드
    public static void main(String[] args) {
        // Register, Scheduler 객체 생성
        Register register = new Register();
        Scheduler scheduler = new Scheduler();

        // SchedulerInterface 초기화
        new SchedulerInterface(register, scheduler);
    }
}
