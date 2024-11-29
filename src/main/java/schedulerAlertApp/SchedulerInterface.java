package schedulerAlertApp;

import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class SchedulerInterface {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;

	private JPanel mainScreenPanel;
    private JPanel addScheScreenPanel;
    private JPanel specifyScheListPanel;
	private JPanel delScheScreenPanel;

    private Register user;
    private Scheduler scheduler;

    private String selectedDate; // 일정 삭제 날짜 저장용

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
        mainPanel.add(registerScreen(), "Register");
        mainPanel.add(findPasswordScreen(), "FindPassword");
        // 메인 패널을 프레임에 추가
        frame.add(mainPanel);
		frame.setVisible(true);

        // 2초 뒤 자동 로그인 실행
        javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
            if (user.autoLogin()) {
                initializePostLoginScreens(); // 로그인 후 화면들 초기화
                showScreen("Main");
                frame.setSize(1200, 800);
            } else {
                showScreen("Login");
            }
        });
        timer.setRepeats(false); // 한 번만 실행되도록 설정
        timer.start();
    }

	 // 로그인 후 화면들을 초기화하는 메서드
	 private void initializePostLoginScreens() {
        mainScreenPanel = mainScreen();
        addScheScreenPanel = addScheScreen();
        specifyScheListPanel = showSpecifyScheListScreen();
		delScheScreenPanel = delScheScreen();

        mainPanel.add(mainScreenPanel, "Main");
        mainPanel.add(addScheScreenPanel, "AddSchedule");
        mainPanel.add(specifyScheListPanel, "SpecifyScheList");
		mainPanel.add(delScheScreenPanel, "DeleteSchedule");
    }

    // 오토로그인 화면
	public JPanel autoLoginScreen() {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());

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
					initializePostLoginScreens(); // 로그인 성공 시 화면들 초기화
					showScreen("Main");
					frame.setSize(1200, 800);
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
	 
	   public JPanel registerScreen() {
	        JPanel panel = new JPanel();
	        panel.setLayout(null); 

	        // Register 제목 라벨
	        JLabel titleLabel = new JLabel("Register");
	        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(200, 50, 400, 40); 
	        panel.add(titleLabel);

	        // idLabel 기준 Y좌표 설정
	        int baseY = 150;

	        // ID Label & Field
	        JLabel idLabel = new JLabel("ID:");
	        idLabel.setBounds(150, baseY, 100, 30);
	        JTextField idField = new JTextField();
	        idField.setBounds(250, baseY, 200, 30);
	        JButton verifyIDButton = new JButton("Verify ID");
	        verifyIDButton.setBounds(460, baseY, 120, 30);
	        styleButton(verifyIDButton, new Color(135, 206, 250)); // 하늘색

	        // Password Label & Field
	        JLabel passwordLabel = new JLabel("Password:");
	        passwordLabel.setBounds(150, baseY + 50, 100, 30);
	        JTextField passwordField = new JTextField();
	        passwordField.setBounds(250, baseY + 50, 330, 30);

	        // Security Question Label & ComboBox
	        JLabel questionLabel = new JLabel("Security Question:");
	        questionLabel.setBounds(150, baseY + 100, 150, 30);
	        JComboBox<String> questionComboBox = new JComboBox<>(new String[]{
	            "질문 1: 당신의 출생지는?",
	            "질문 2: 당신의 첫 번째 반려동물 이름은?",
	            "질문 3: 당신이 다닌 첫 번째 학교는?"
	        });
	        questionComboBox.setBounds(310, baseY + 100, 270, 30);

	        // Answer Label & Field
	        JLabel answerLabel = new JLabel("Answer:");
	        answerLabel.setBounds(150, baseY + 150, 100, 30);
	        JTextField answerField = new JTextField();
	        answerField.setBounds(250, baseY + 150, 330, 30);

	        // Buttons
	        JButton registerButton = new JButton("Register");
	        registerButton.setBounds(250, baseY + 220, 120, 40);
	        styleButton(registerButton, new Color(135, 206, 250)); // 하늘색

	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.setBounds(400, baseY + 220, 120, 40);
	        styleButton(cancelButton, new Color(255, 215, 0)); // 노란색

	        // 패널에 추가
	        panel.add(idLabel);
	        panel.add(idField);
	        panel.add(verifyIDButton);
	        panel.add(passwordLabel);
	        panel.add(passwordField);
	        panel.add(questionLabel);
	        panel.add(questionComboBox);
	        panel.add(answerLabel);
	        panel.add(answerField);
	        panel.add(registerButton);
	        panel.add(cancelButton);

	        // 글자 수 제한
	        limitTextFieldLength(idField, 12);
	        limitTextFieldLength(passwordField, 16);
	        limitTextFieldLength(answerField, 50);

	       
	        verifyIDButton.addActionListener(e -> {
	            String enteredId = idField.getText().trim();
	            if (enteredId.isEmpty()) {
	                JOptionPane.showMessageDialog(panel, "ID를 입력하세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
	            CompletableFuture<String[]> future = user.getKeyArray(false);
	            future.thenAccept(existingIds -> {
	                java.util.List<String> idList = Arrays.asList(existingIds);
	                if (idList.contains(enteredId)) {
	                    JOptionPane.showMessageDialog(panel, "이미 존재하는 ID입니다.", "Error", JOptionPane.ERROR_MESSAGE);
	                } else {
	                    JOptionPane.showMessageDialog(panel, "사용 가능한 ID입니다.", "Success", JOptionPane.INFORMATION_MESSAGE);
	                }
	            }).exceptionally(ex -> {
	                JOptionPane.showMessageDialog(panel, "중복 확인 중 오류 발생: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                return null;
	            });
	        });

	        registerButton.addActionListener(e -> {
	            String enteredId = idField.getText().trim();
	            String enteredPassword = passwordField.getText().trim();
	            int questionIndex = questionComboBox.getSelectedIndex();
	            String enteredAnswer = answerField.getText().trim();

	            if (enteredId.isEmpty() || enteredPassword.isEmpty() || enteredAnswer.isEmpty()) {
	                JOptionPane.showMessageDialog(panel, "모든 필드를 입력해야 합니다.", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }

	            user.register(enteredId, enteredPassword, questionIndex, enteredAnswer)
	                .thenAccept(success -> {
	                    if (success) {
	                        JOptionPane.showMessageDialog(panel, "회원가입이 완료되었습니다.", "Success", JOptionPane.INFORMATION_MESSAGE);
	                    } else {
	                        JOptionPane.showMessageDialog(panel, "회원가입에 실패했습니다.", "Error", JOptionPane.ERROR_MESSAGE);
	                    }
	                }).exceptionally(ex -> {
	                    JOptionPane.showMessageDialog(panel, "회원가입 중 오류 발생: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                    return null;
	                });
	        });

	        cancelButton.addActionListener(e -> {
	            showScreen("Login"); // 로그인 화면으로 전환
	        });

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
    
	public JPanel mainScreen() {
	        // 메인 패널 설정
	        JPanel mainPanel = new JPanel();
	        mainPanel.setLayout(null);
	        mainPanel.setPreferredSize(new Dimension(1200, 800));
	        mainPanel.setBackground(Color.WHITE);

	        // 상단 제목
	        JLabel titleLabel = new JLabel("Task Manager");
	        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(400, 20, 400, 50);
	        mainPanel.add(titleLabel);

	        // 좌측 시계 이미지
	        JLabel clockLabel = new JLabel();
			clockLabel.setIcon(new ImageIcon(getClass().getResource("/images/clock.png")));
	        clockLabel.setBounds(70, 180, 150, 150);
	        mainPanel.add(clockLabel);

	        // 좌측 버튼 1: "일정 추가하기"
	        JButton addScheduleButton = new JButton("일정 추가하기");
	        addScheduleButton.setIcon(new ImageIcon(getClass().getResource("/images/addScheduleButton.png")));
	        addScheduleButton.setForeground(Color.WHITE);
	        addScheduleButton.setBorderPainted(false);
	        addScheduleButton.setBounds(50, 370, 190, 65);
	        addScheduleButton.addActionListener(e -> showScreen("AddSchedule")); // 화면 전환
	        mainPanel.add(addScheduleButton);

	        // 좌측 버튼 2: "일정 조회하기"
	        JButton viewScheduleButton = new JButton("일정 조회하기");
	        viewScheduleButton.setIcon(new ImageIcon(getClass().getResource("/images/viewScheduleButton.png")));
	        viewScheduleButton.setForeground(Color.WHITE);
	        viewScheduleButton.setBorderPainted(false);
	        viewScheduleButton.setBounds(50, 480, 190, 65);
	        viewScheduleButton.addActionListener(e -> showScreen("SpecifyScheList")); // 화면 전환
	        mainPanel.add(viewScheduleButton);

	        // 우측 패널
	        JPanel rightPanel = new JPanel();
	        rightPanel.setLayout(new BorderLayout());
	        rightPanel.setBorder(BorderFactory.createLineBorder(new Color(0x87B2FB), 5, true)); // 둥근 직사각형 테두리
	        rightPanel.setBackground(Color.WHITE);
	        rightPanel.setBounds(300, 100, 850, 600);

	        // 일정 요약 패널 scheduler.allScheSummary(user) 호출
//	        TreeMap<Integer, Integer> scheSummary = scheduler.allScheSummary(user); // 예시 데이터
//	        JPanel scheduleSummaryPanel = showAllScheScreen(scheSummary);
	        
	        // 테스트 scheSummary 데이터 생성
	        TreeMap<Integer, Integer> testScheSummary = new TreeMap<>();
	        testScheSummary.put(1, 3); // 1일 남은 일정 3개
	        testScheSummary.put(2, 5); // 2일 남은 일정 5개
	        testScheSummary.put(7, 2); // 7일 남은 일정 2개
	        JPanel scheduleSummaryPanel = showAllScheScreen(testScheSummary);
	        
	        rightPanel.add(scheduleSummaryPanel, BorderLayout.CENTER);
	        mainPanel.add(rightPanel);

	        // 우측 상단 ID 표시
	        JLabel userIdLabel = new JLabel(user.getUserId() + "님");
	        userIdLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
	        userIdLabel.setBounds(950, 30, 150, 30);
	        mainPanel.add(userIdLabel);

	        // 로그아웃 버튼
	        JButton logoutButton = new JButton("로그아웃");
	        logoutButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
	        logoutButton.setBounds(1050, 30, 100, 30);
	        styleButton(logoutButton, new Color(255, 99, 71));  // 토마토 레드 계열
	        logoutButton.addActionListener(e -> {
	            CompletableFuture<Boolean> future = user.logout();
	            future.thenAccept(success -> {
	                if (success) {
	                    JOptionPane.showMessageDialog(frame, "로그아웃 되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
	                    showScreen("Login");
	                    frame.setSize(800, 600);  // 로그인 화면 크기로 조정
	                } else {
	                    JOptionPane.showMessageDialog(frame, "로그아웃에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	        });
	        mainPanel.add(logoutButton);

	        return mainPanel;
	    }
    
	   
	   public JPanel showAllScheScreen(TreeMap<Integer, Integer> scheSummary) {
	        // 패널 생성
	        JPanel summaryPanel = new JPanel();
	        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS)); // 세로 정렬
	        summaryPanel.setBackground(Color.WHITE); 
	        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 패널 여백
	        

	        // 요약 데이터 표시
	        for (Map.Entry<Integer, Integer> entry : scheSummary.entrySet()) {
	            int remainingDays = entry.getKey();
	            int taskCount = entry.getValue();
	            String taskSummary = taskCount + "개의 일정이 " + remainingDays + "일 남았습니다.";

	            // 각 요약 데이터를 JLabel로 추가
	            JLabel taskLabel = new JLabel(taskSummary);
	            taskLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20)); // 폰트 설정
	            summaryPanel.add(taskLabel);
	        }

	        // 스크롤 가능하도록 JScrollPane에 추가
	        JScrollPane scrollPane = new JScrollPane(summaryPanel);
	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)); // 테두리 설정

	        // 최종 패널 반환
	        JPanel containerPanel = new JPanel(new BorderLayout());
	        containerPanel.setBackground(Color.WHITE);
	        containerPanel.add(scrollPane, BorderLayout.CENTER);

	        return containerPanel;
	    }
	   
	   public JPanel addScheScreen() {
	        // 메인 패널 설정
	        JPanel panel = new JPanel();
	        panel.setLayout(null); // 절대 배치
	        panel.setPreferredSize(new Dimension(1200, 800)); // 크기 1200x800
	        panel.setBackground(Color.WHITE);

	        // 상단 제목
	        JLabel titleLabel = new JLabel("일정 추가");
	        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 36));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(400, 50, 400, 50); // 상단 중앙
	        panel.add(titleLabel);

	        // 날짜 입력 필드
	        JLabel dateLabel = new JLabel("날짜 (YYYYMMDD):");
	        dateLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
	        dateLabel.setBounds(200, 150, 200, 30);
	        panel.add(dateLabel);

	        JTextField dateField = new JTextField();
	        dateField.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
	        dateField.setBounds(450, 150, 300, 40);
	        panel.add(dateField);

	        // 일정 내용 입력 필드
	        JLabel dataLabel = new JLabel("일정 내용:");
	        dataLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
	        dataLabel.setBounds(200, 250, 200, 30);
	        panel.add(dataLabel);

	        JTextArea dataField = new JTextArea();
	        dataField.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
	        dataField.setLineWrap(true); // 줄 바꿈 허용
	        dataField.setWrapStyleWord(true);
	        JScrollPane dataScrollPane = new JScrollPane(dataField);
	        dataScrollPane.setBounds(450, 250, 500, 300);
	        panel.add(dataScrollPane);

	        // 저장 버튼
	        JButton saveButton = new JButton("Save");
	        saveButton.setBounds(450, 600, 150, 50);
	        styleButton(saveButton, new Color(173, 216, 230)); // 연한 파란색
	        panel.add(saveButton);

	        // 취소 버튼
	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.setBounds(650, 600, 150, 50);
	        styleButton(cancelButton, new Color(255, 215, 0)); // 노란색
	        panel.add(cancelButton);

	        // 저장 버튼 클릭 이벤트
	        saveButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String date = dateField.getText().trim();
	                String data = dataField.getText().trim();

	               
	                if (date.isEmpty() || data.isEmpty()) {
	                    JOptionPane.showMessageDialog(panel, "모든 필드를 입력해주세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
                    //날짜가 "YYYYMMDD" 형식인지 검증
	                if (!date.matches("\\d{8}")) {
	                    JOptionPane.showMessageDialog(panel, "날짜는 YYYYMMDD 형식으로 입력해주세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }

	                // addSche 메서드 호출
	                //boolean success = scheduler.addSche(user, date, data);
	                
	                
	                boolean success = true;
	                // 일정 추가 성공 여부에 따라 알림 표시 및 화면 전환
	                if (success) {
	                    JOptionPane.showMessageDialog(panel, "일정 추가 성공!", "Success", JOptionPane.INFORMATION_MESSAGE);
	                } else {
	                    JOptionPane.showMessageDialog(panel, "일정 추가 실패!", "Error", JOptionPane.ERROR_MESSAGE);
	                }
	                showScreen("Main"); // 메인 화면으로 이동
	            }
	        });

	        // 취소 버튼 클릭 이벤트
	        cancelButton.addActionListener(e -> showScreen("Main"));

	        return panel;
	    }

	public JPanel showSpecifyScheListScreen() {
        // 메인 패널 설정
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(1200, 800));
        panel.setBackground(Color.WHITE);

        // 상단 제목
        JLabel titleLabel = new JLabel("일정 조회하기");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(400, 50, 400, 50);
        panel.add(titleLabel);


		// 날짜 선택 콤보박스
        // TODO: getKeyArray() 호출하여 실제 일정 날짜 가져오기

        // 날짜 선택 콤보박스
        String[] scheduleDates = user.getKeyArray(true).join(); // 동기적으로 데이터 가져오기(로그인 시점에 초기화)
    	JComboBox<String> dateComboBox = new JComboBox<>(scheduleDates.length > 0 ? scheduleDates : new String[]{"저장된 일정이 없습니다"});
        dateComboBox.setBounds(350, 150, 200, 30);
        dateComboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        panel.add(dateComboBox);

		// 조회 버튼
        JButton viewButton = new JButton("조회");
        viewButton.setBounds(570, 150, 100, 30);
        viewButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        styleButton(viewButton, new Color(135, 206, 250));
        panel.add(viewButton);

		 // 삭제 버튼 
		 JButton deleteButton = new JButton("삭제");
		 deleteButton.setBounds(680, 150, 100, 30);
		 deleteButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
		 styleButton(deleteButton, new Color(0xCC93E9));  // 연보라색
		 deleteButton.setEnabled(scheduleDates.length > 0);
		 deleteButton.addActionListener(e -> {
		     selectedDate = dateComboBox.getSelectedItem().toString();  // 선택된 날짜 저장
		     showScreen("DeleteSchedule");
		 });
		 panel.add(deleteButton);

        // 일정 표시 패널
        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));
        schedulePanel.setBackground(Color.WHITE);

        // 스크롤 패널에 일정 패널 추가
        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        scrollPane.setBounds(300, 200, 600, 400);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.add(scrollPane);

        // 조회 버튼 클릭 이벤트
        viewButton.addActionListener(e -> {
            schedulePanel.removeAll(); // 기존 일정 제거
            String selectedDate = (String) dateComboBox.getSelectedItem();
            
            // scheduler.specifyScheList() 호출하여 실제 일정 데이터 가져오기
            // String[] schedules = scheduler.specifyScheList(user, selectedDate);
			// for (int i = 0; i <  schedules.length; i++) {
            //     JLabel scheduleLabel = new JLabel((i + 1) + ". " +  schedules[i]);
            //     scheduleLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
            //     scheduleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            //     schedulePanel.add(scheduleLabel);
            // }
            
            // 테스트용 예시 데이터
            String[] testSchedules = {
                "오전 8시 - 아침 먹기",
                "오후 1시 - 점심 먹기",
                "오후 7시 - 저녁 먹기",
                "오후 11시 - 잠 자기"
            };

            // 테스트 일정 표시
            for (int i = 0; i < testSchedules.length; i++) {
                JLabel scheduleLabel = new JLabel((i + 1) + ". " + testSchedules[i]);
                scheduleLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
                scheduleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                schedulePanel.add(scheduleLabel);
            }

            schedulePanel.revalidate();
            schedulePanel.repaint();
        });

        // 메인으로 돌아가기 버튼
        JButton backButton = new JButton("메인으로 돌아가기");
        backButton.setBounds(500, 650, 200, 40);
        styleButton(backButton, new Color(255, 215, 0));
		viewButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        backButton.addActionListener(e -> showScreen("Main"));
        panel.add(backButton);

        return panel;
    }
    public JPanel delScheScreen() {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(1200, 800));
        panel.setBackground(Color.WHITE);

        // 상단 제목
        JLabel titleLabel = new JLabel("일정 삭제하기");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(400, 50, 400, 50);
        panel.add(titleLabel);

        // 선택된 날짜 표시 (YYYYMMDD -> YYYY년 MM월 DD일)
        String formattedDate = selectedDate != null ? 
            selectedDate.substring(0, 4) + "년 " + 
            selectedDate.substring(4, 6) + "월 " + 
            selectedDate.substring(6, 8) + "일" : 
            "날짜가 선택되지 않음";

        JLabel dateLabel = new JLabel("선택된 날짜: " + formattedDate);
        dateLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
        dateLabel.setBounds(350, 120, 300, 30);
        panel.add(dateLabel);

        // 일정 선택 콤보박스(specifyScheList(user, selectedDate)호출로 주석 처리)
        // String[] schedules = scheduler.specifyScheList(user, selectedDate);
        // JComboBox<String> scheduleComboBox = new JComboBox<>(schedules);

		// 일정 선택 콤보박스 (테스트용 예시)
		String[] testSchedules = {
			"오전 8시 - 아침 먹기",
			"오후 1시 - 점심 먹기",
			"오후 7시 - 저녁 먹기",
			"오후 11시 - 잠 자기"
		};
		JComboBox<String> scheduleComboBox = new JComboBox<>(testSchedules);

        scheduleComboBox.setBounds(350, 170, 400, 30);
        scheduleComboBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        panel.add(scheduleComboBox);

        // 삭제 버튼
        JButton deleteButton = new JButton("삭제");
        deleteButton.setBounds(770, 170, 100, 30);
        deleteButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        styleButton(deleteButton, new Color(0xCC93E9));  // 연보라색
        panel.add(deleteButton);

        // 삭제 버튼 클릭 이벤트
        deleteButton.addActionListener(e -> {
            int selectedIndex = scheduleComboBox.getSelectedIndex();
            int confirm = JOptionPane.showConfirmDialog(
                panel,
                "선택한 일정을 삭제하시겠습니까?",
                "일정 삭제 확인",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                // boolean success = scheduler.delSche(user, selectedDate, selectedIndex);
                boolean success = true;  // 테스트용
                if (success) {
                    JOptionPane.showMessageDialog(panel, "일정이 성공적으로 삭제되었습니다.");
                    showScreen("Main");
                } else {
                    JOptionPane.showMessageDialog(panel, "일정 삭제에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 메인으로 돌아가기 버튼
        JButton backButton = new JButton("메인으로 돌아가기");
        backButton.setBounds(500, 650, 200, 40);
        styleButton(backButton, new Color(255, 215, 0));
        backButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        backButton.addActionListener(e -> showScreen("Main"));
        panel.add(backButton);

        return panel;
    }
	   
 // 버튼 스타일 설정
    private void styleButton(JButton button, Color bgColor) {
    	button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        //button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // 글자 수 제한 메서드
    private void limitTextFieldLength(JTextField textField, int maxLength) {
        textField.setDocument(new javax.swing.text.PlainDocument() {
            @Override
            public void insertString(int offset, String str, javax.swing.text.AttributeSet attr)
                throws javax.swing.text.BadLocationException {
                if (str == null || getLength() + str.length() > maxLength) {
                    return;
                }
                super.insertString(offset, str, attr);
            }
        });
    }

    // 화면 전환 메서드
	public void showScreen(String name) {
        if ((name.equals("Main") || name.equals("AddSchedule") || 
            name.equals("SpecifyScheList") || name.equals("DeleteSchedule"))    
            && mainScreenPanel == null) {
            JOptionPane.showMessageDialog(frame, "로그인이 필요합니다.", "알림", JOptionPane.WARNING_MESSAGE);
            cardLayout.show(mainPanel, "Login");
            return;
        }
		// 현재 표시된 패널 제거
		Component[] components = mainPanel.getComponents();
		for (Component comp : components) {
			mainPanel.remove(comp);
		}
	
		// 새 패널 생성
		switch (name) {
			case "Login":
				mainPanel.add(loginScreen(), "Login");
				break;
			case "Register":
				mainPanel.add(registerScreen(), "Register");
				break;
			case "FindPassword":
				mainPanel.add(findPasswordScreen(), "FindPassword");
				break;
			case "Main":
				mainScreenPanel = mainScreen();
				mainPanel.add(mainScreen(), "Main");
				break;
			case "AddSchedule":
				mainScreenPanel = mainScreen();
				mainPanel.add(addScheScreen(), "AddSchedule");
				break;
			case "SpecifyScheList":
				mainScreenPanel = mainScreen();
				mainPanel.add(showSpecifyScheListScreen(), "SpecifyScheList");
				break;
			case "DeleteSchedule":
				mainScreenPanel = mainScreen();
				mainPanel.add(delScheScreen(), "DeleteSchedule");
				break;
		}
		cardLayout.show(mainPanel, name);
		mainPanel.revalidate();
		mainPanel.repaint();
    }

    // main() 메서드
    public static void main(String[] args) {
        // Register, Scheduler 객체 생성
        Register register = new Register();
        Scheduler scheduler = new Scheduler();

        new SchedulerInterface(register, scheduler);
    }

}
