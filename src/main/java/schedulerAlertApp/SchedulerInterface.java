package schedulerAlertApp;

import javax.swing.*;
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

    private Register user;
    private Scheduler scheduler;

    private String selectedDate; // 일정 삭제 날짜 저장용

    public SchedulerInterface(Register user, Scheduler scheduler) {
        this.user = user;
        this.scheduler = scheduler;

        frame = new JFrame("Scheduler Interface");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
		frame.setResizable(false);

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
                showScreen("Main");
                frame.setSize(1200, 800);
            } else {
                showScreen("Login");
            }
        });
        timer.setRepeats(false); // 한 번만 실행되도록 설정
        timer.start();
    }

    // 자동로그인 화면
	public JPanel autoLoginScreen() {
	    JPanel panel = new JPanel();
	    panel.setLayout(new BorderLayout());

	    // 프로그레스 바
	    JPanel centerPanel = new JPanel();
	    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
	    centerPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // 패널 자체 정렬

	    centerPanel.add(Box.createVerticalGlue()); // 위쪽 공간

	    JLabel statusLabel = new JLabel("자동로그인 진행 중...");
	    statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    centerPanel.add(statusLabel);

	    JProgressBar progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);
	    progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
	    centerPanel.add(progressBar);
	    
	    centerPanel.add(Box.createVerticalStrut(20)); // 텍스트와 프로그레스바 간격
	    centerPanel.add(Box.createVerticalGlue()); 

	    panel.add(centerPanel, BorderLayout.CENTER);

	    // 로그인화면으로 이동 버튼
	    JButton goToLoginButton = new JButton("로그인 화면으로 이동");
	    goToLoginButton.addActionListener(e -> showScreen("Login"));
	    panel.add(goToLoginButton, BorderLayout.SOUTH);

	    return panel;
	}

    // 로그인 화면
	 public JPanel loginScreen() {
		JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon background = new ImageIcon(getClass().getResource("/images/loginScreen.png"));
				g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};
		panel.setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.setOpaque(false);  
		centerPanel.add(Box.createVerticalGlue());

		// 프로그램 이름 라벨
		centerPanel.add(Box.createVerticalStrut(50));
		JLabel programNameLabel = new JLabel("Task Manager", JLabel.CENTER);
		programNameLabel.setFont(new Font("Arial", Font.BOLD, 28));
		programNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(programNameLabel);

		centerPanel.add(Box.createVerticalStrut(20)); // 프로그램 이름과 ID 레이블 간격

		// ID 레이블 및 입력 필드
		JLabel idLabel = new JLabel("ID");
		idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(idLabel);

		JTextField idField = new JTextField(20);
		idField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
		idField.setMaximumSize(new Dimension(300, 30));
		idField.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(idField);

		centerPanel.add(Box.createVerticalStrut(15)); // ID와 Password 간격

		// Password 레이블 및 입력 필드
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(passwordLabel);

		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1, true));
		passwordField.setMaximumSize(new Dimension(300, 30));
		passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(passwordField);

		centerPanel.add(Box.createVerticalStrut(15)); // Password와 체크박스 간격

		// 자동 로그인 체크박스
		JCheckBox autoLoginCheckBox = new JCheckBox("자동 로그인");
		autoLoginCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		centerPanel.add(autoLoginCheckBox);

		centerPanel.add(Box.createVerticalStrut(15)); // 체크박스와 버튼 간격

		// 버튼 패널
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setOpaque(false); 
		// 로그인 버튼
		JButton loginButton = new JButton("로그인");
		styleButton(loginButton, new Color(135, 206, 250));  // 하늘색
		loginButton.addActionListener(e -> {
			boolean autoLogin = autoLoginCheckBox.isSelected();
			String id = idField.getText();
			String password = new String(passwordField.getPassword());

			int loginResult = user.login(id, password, autoLogin);

			if (loginResult == 1) {
				showScreen("Main");
				frame.setSize(1200, 800);
			} else if (loginResult == 2) {
				JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
			} else if (loginResult == 3) {
				JOptionPane.showMessageDialog(panel, "비밀번호가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
			} else if (loginResult == 4) {
				// 로그인은 성공했지만 자동 로그인 설정은 실패한 경우
				int option = JOptionPane.showConfirmDialog(
					panel,
					"로그인은 성공했지만 자동 로그인 설정에 실패했습니다.\n계속 진행하시겠습니까?",
					"자동 로그인 설정 실패",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				
				if (option == JOptionPane.YES_OPTION) {
					showScreen("Main");
					frame.setSize(1200, 800);
				} else {
					// 사용자가 취소를 선택한 경우
					user.logout();  // 로그인 상태 해제
				}
			} else {
				JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
			}
		});
		buttonPanel.add(loginButton);
		// 회원가입 버튼
		JButton registerButton = new JButton("회원가입");
		styleButton(registerButton, new Color(153, 153, 255));  // 남색
		registerButton.addActionListener(e -> showScreen("Register"));
		buttonPanel.add(registerButton);
		// 비밀번호 재설정 버튼
		JButton findPasswordButton = new JButton("비밀번호 재설정");
		styleButton(findPasswordButton, new Color(204, 153, 255));  // 보라색
		findPasswordButton.addActionListener(e -> showScreen("FindPassword"));
		buttonPanel.add(findPasswordButton);

		centerPanel.add(buttonPanel);
		centerPanel.add(Box.createVerticalGlue());
		panel.add(centerPanel, BorderLayout.CENTER);

		return panel;
	}
	// 회원가입 화면
	   public JPanel registerScreen() {
	        JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
	        panel.setLayout(null); 

	        JLabel titleLabel = new JLabel("Register");
	        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(200, 50, 400, 40); 
	        panel.add(titleLabel);

	        int baseY = 150; // 기준 Y좌표
			// ID 레이블
	        JLabel idLabel = new JLabel("ID");
	        idLabel.setBounds(150, baseY, 100, 30);
			// ID 입력 필드
	        JTextField idField = new JTextField();
	        idField.setBounds(250, baseY, 200, 30);
			// 아이디 확인 버튼
	        JButton verifyIDButton = new JButton("아이디 확인");
	        verifyIDButton.setBounds(460, baseY, 120, 30);
	        styleButton(verifyIDButton, new Color(153, 224, 173)); // 연두색
			// 비밀번호 레이블
	        JLabel passwordLabel = new JLabel("Password");
	        passwordLabel.setBounds(150, baseY + 50, 100, 30);
			// 비밀번호 입력 필드
	        JPasswordField passwordField = new JPasswordField();
	        passwordField.setBounds(250, baseY + 50, 330, 30);
			// 보안 질문 레이블
	        JLabel questionLabel = new JLabel("Security Question");
	        questionLabel.setBounds(150, baseY + 100, 150, 30);
			// 보안 질문 콤보박스
	        JComboBox<String> questionComboBox = new JComboBox<>(new String[]{
	            "질문 1: 당신의 출생지는?",
	            "질문 2: 당신의 첫 번째 반려동물 이름은?",
	            "질문 3: 당신이 다닌 첫 번째 학교는?"
	        });
	        questionComboBox.setBounds(310, baseY + 100, 270, 30);
			// 답변 레이블
	        JLabel answerLabel = new JLabel("Answer");
	        answerLabel.setBounds(150, baseY + 150, 100, 30);
			// 답변 입력 필드
	        JTextField answerField = new JTextField();
	        answerField.setBounds(250, baseY + 150, 330, 30);
			// 회원가입 버튼
	        JButton registerButton = new JButton("회원가입");
	        registerButton.setBounds(250, baseY + 220, 120, 40);
	        styleButton(registerButton, new Color(135, 206, 250)); // 하늘색
			// 취소 버튼
	        JButton cancelButton = new JButton("취소");
	        cancelButton.setBounds(400, baseY + 220, 120, 40);
	        styleButton(cancelButton, new Color(204, 153, 255)); // 보라색
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
			// 아이디 확인 버튼 이벤트
	        verifyIDButton.addActionListener(e -> {
	            String enteredId = idField.getText().trim();
	            if (enteredId.isEmpty()) {
	                JOptionPane.showMessageDialog(panel, "ID를 입력하세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
	            // ID 길이 검증
	            String idError = validateId(enteredId);
	            if (idError != null) {
	                JOptionPane.showMessageDialog(panel, idError, "ID 오류", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
				// 중복 확인	
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
			// 회원가입 버튼 이벤트
	        registerButton.addActionListener(e -> {
	            String enteredId = idField.getText().trim();
	            String enteredPassword = passwordField.getText().trim();
	            int questionIndex = questionComboBox.getSelectedIndex();
	            String enteredAnswer = answerField.getText().trim();
				// 빈칸 확인
	            if (enteredId.isEmpty() || enteredPassword.isEmpty() || enteredAnswer.isEmpty()) {
	                JOptionPane.showMessageDialog(panel, "모든 필드를 입력해야 합니다.", "Error", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
				// ID 길이 검증
	            String idError = validateId(enteredId);
	            if (idError != null) {
	                JOptionPane.showMessageDialog(panel, idError, "ID 오류", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
				// 비밀번호 검증
	            String passwordError = validatePassword(enteredPassword);
	            if (passwordError != null) {
	                JOptionPane.showMessageDialog(panel, passwordError, "비밀번호 오류", JOptionPane.ERROR_MESSAGE);
	                return;
	            }
				// 회원가입	
	            user.register(enteredId, enteredPassword, questionIndex, enteredAnswer)
	                .thenAccept(success -> {
	                    if (success) {
	                        JOptionPane.showMessageDialog(panel, "회원가입이 완료되었습니다.", "Success", JOptionPane.INFORMATION_MESSAGE);
	                        showScreen("Login");
	                    } else {
	                        JOptionPane.showMessageDialog(panel, "회원가입에 실패했습니다.", "Error", JOptionPane.ERROR_MESSAGE);
	                    }
	                }).exceptionally(ex -> {
	                    JOptionPane.showMessageDialog(panel, "회원가입 중 오류 발생: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	                    return null;
	                });
	        });
			// 취소 버튼 이벤트
	        cancelButton.addActionListener(e -> {
	            showScreen("Login"); 
	        });
	        return panel;
	    }

    //비밀번호 찾기 화면
    public JPanel findPasswordScreen() {
        JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
        panel.setLayout(null); 

		// 제목 레이블
        JLabel titleLabel = new JLabel("Find Password");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(200, 50, 400, 40);
        panel.add(titleLabel);

        int baseY = 150; // 기준 Y좌표
		// ID 레이블		
        JLabel idLabel = new JLabel("ID");
        idLabel.setBounds(150, baseY, 100, 30);
		// ID 입력 필드
        JTextField idField = new JTextField();
        idField.setBounds(250, baseY, 330, 30);
		// 보안 질문 레이블
        JLabel questionLabel = new JLabel("Security Question");
        questionLabel.setBounds(150, baseY + 50, 150, 30);
		// 보안 질문 콤보박스
        JComboBox<String> questionComboBox = new JComboBox<>(new String[] {
            "질문 1: 당신의 출생지는?",
            "질문 2: 당신의 첫 번째 반려동물 이름은?",
            "질문 3: 당신이 다닌 첫 번째 학교는?"
        });
        questionComboBox.setBounds(310, baseY + 50, 270, 30);
		// 답변 레이블
        JLabel answerLabel = new JLabel("Answer");
        answerLabel.setBounds(150, baseY + 100, 100, 30);
		// 답변 입력 필드
        JTextField answerField = new JTextField();
        answerField.setBounds(250, baseY + 100, 330, 30);
		// 새 비밀번호 레이블
        JLabel newPasswordLabel = new JLabel("New Password");
        newPasswordLabel.setBounds(150, baseY + 150, 100, 30);
		// 새 비밀번호 입력 필드
        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setBounds(250, baseY + 150, 330, 30);
		// 비밀번호 재설정 버튼
        JButton resetButton = new JButton("비밀번호 재설정");
        resetButton.setBounds(250, baseY + 220, 150, 40);
        styleButton(resetButton, new Color(135, 206, 250)); // 하늘색
		// 취소 버튼
        JButton cancelButton = new JButton("로그인 화면으로");
        cancelButton.setBounds(430, baseY + 220, 150, 40);
        styleButton(cancelButton, new Color(204, 153, 255)); // 보라색
	// 컴포넌트 추가
        panel.add(idLabel);
        panel.add(idField);
        panel.add(questionLabel);
        panel.add(questionComboBox);
        panel.add(answerLabel);
        panel.add(answerField);
        panel.add(newPasswordLabel);
        panel.add(newPasswordField);
        panel.add(resetButton);
        panel.add(cancelButton);
        // 글자 수 제한
        limitTextFieldLength(idField, 12);
        limitTextFieldLength(answerField, 50);
        limitTextFieldLength(newPasswordField, 16);

        // 비밀번호 재설정 버튼 이벤트
        resetButton.addActionListener(e -> {
            String id = idField.getText();
            int questionIndex = questionComboBox.getSelectedIndex();
            String questionAns = answerField.getText();
            String newPassword = new String(newPasswordField.getPassword());

            // 비밀번호 검증
            String passwordError = validatePassword(newPassword);
            if (passwordError != null) {
                JOptionPane.showMessageDialog(panel, passwordError, "비밀번호 오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
			// 비밀번호 재설정
            int result = user.findPassword(id, questionIndex, questionAns, newPassword);
            if (result == 1) {
				// 비밀번호 재설정 성공
                JOptionPane.showMessageDialog(panel, "비밀번호 재설정 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
                showScreen("Login");
            } else if (result == 2) {
				// ID 존재 여부
                JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else if (result == 3) {
				// 질문 정보 또는 답변 일치 여부
                JOptionPane.showMessageDialog(panel, "질문 정보 또는 답변이 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
				// 알 수 없는 오류
                JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        // 취소 버튼 이벤트
        cancelButton.addActionListener(e -> showScreen("Login"));

        return panel;
    }
    
	// 메인 화면
	public JPanel mainScreen() {
			JPanel mainScreenPanel = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					ImageIcon background = new ImageIcon(getClass().getResource("/images/purpleBackground2.png"));
					g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
				}
			};
	        mainScreenPanel.setLayout(null);
	        mainScreenPanel.setPreferredSize(new Dimension(1200, 800));
	        mainScreenPanel.setBackground(Color.WHITE);

	        // 제목 레이블
	        JLabel titleLabel = new JLabel("Task Manager");
	        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(400, 20, 400, 50);
	        mainScreenPanel.add(titleLabel);

	        // 시계 이미지
	        JLabel clockLabel = new JLabel();
			clockLabel.setIcon(new ImageIcon(getClass().getResource("/images/clock.png")));
	        clockLabel.setBounds(70, 180, 150, 150);
	        mainScreenPanel.add(clockLabel);

	        // 일정 추가 버튼
	        JButton addScheduleButton = new JButton("일정 추가하기");
	        addScheduleButton.setIcon(new ImageIcon(getClass().getResource("/images/addScheduleButton.png")));
	        addScheduleButton.setForeground(Color.WHITE);
	        addScheduleButton.setBorderPainted(false);
	        addScheduleButton.setBounds(50, 370, 190, 65);
	        addScheduleButton.addActionListener(e -> showScreen("AddSchedule")); // 화면 전환
	        mainScreenPanel.add(addScheduleButton);

	        // 일정 조회 버튼
	        JButton viewScheduleButton = new JButton("일정 조회하기");
	        viewScheduleButton.setIcon(new ImageIcon(getClass().getResource("/images/viewScheduleButton.png")));
	        viewScheduleButton.setForeground(Color.WHITE);
	        viewScheduleButton.setBorderPainted(false);
	        viewScheduleButton.setBounds(50, 480, 190, 65);
	        viewScheduleButton.addActionListener(e -> showScreen("SpecifyScheList")); // 화면 전환
	        mainScreenPanel.add(viewScheduleButton);

	        // 우측 패널
	        JPanel rightPanel = new JPanel();
	        rightPanel.setLayout(new BorderLayout());
	        rightPanel.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 255), 5, true)); 
	        rightPanel.setBackground(Color.WHITE);
	        rightPanel.setBounds(300, 100, 850, 600);

			// 일정 요약 패널
	        TreeMap<Integer, Integer> scheSummary = scheduler.allScheSummary(user); // 예시 데이터
	        JPanel scheduleSummaryPanel = showAllScheScreen(scheSummary);
	        
	        rightPanel.add(scheduleSummaryPanel, BorderLayout.CENTER);
	        mainScreenPanel.add(rightPanel);

	        // 우측 상단 ID 표시
	        JLabel userIdLabel = new JLabel(user.getUserId() + "님");
	        userIdLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
	        userIdLabel.setBounds(950, 30, 150, 30);
	        mainScreenPanel.add(userIdLabel);

	        // 로그아웃 버튼
	        JButton logoutButton = new JButton("로그아웃");
	        logoutButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
	        logoutButton.setBounds(1050, 30, 100, 30);
	        styleButton(logoutButton, new Color(255, 99, 71));  
	        logoutButton.addActionListener(e -> {
	            CompletableFuture<Boolean> future = user.logout();
	            future.thenAccept(success -> {
	                if (success) {
						// 로그아웃 성공
                        JOptionPane.showMessageDialog(frame, "로그아웃 되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
	                    showScreen("Login");
	                    frame.setSize(800, 600);  // 로그인 화면 크기로 조정
	                } else {
						// 로그아웃 실패
                        JOptionPane.showMessageDialog(frame, "로그아웃에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
	                }
	            });
	        });
	        mainScreenPanel.add(logoutButton);

	        return mainScreenPanel;
	    }
    
	   // 일정 요약 화면
	   public JPanel showAllScheScreen(TreeMap<Integer, Integer> scheSummary) {
	        JPanel summaryPanel = new JPanel();
	        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
	        summaryPanel.setBackground(Color.WHITE);
	        summaryPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

	        // 제목 레이블
	        JLabel titleLabel = new JLabel("한 눈에 보는 일정");
	        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
	        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	        summaryPanel.add(titleLabel);
	        summaryPanel.add(Box.createVerticalStrut(20));  

	        if (scheSummary.isEmpty()) {
				// 일정 없음
	            JLabel emptyLabel = new JLabel("등록된 일정이 없습니다.");
	            emptyLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
	            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
	            summaryPanel.add(emptyLabel);
	        } else {
				// 일정 있음
				for (Map.Entry<Integer, Integer> entry : scheSummary.entrySet()) {
					String taskSummary = entry.getValue() + "개의 일정이 " + entry.getKey() + "일 남았습니다.";
					JLabel taskLabel = new JLabel(taskSummary);
					taskLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
					taskLabel.setAlignmentX(Component.CENTER_ALIGNMENT);  
					summaryPanel.add(taskLabel);
					summaryPanel.add(Box.createVerticalStrut(5));  
				}
	        }

	        return summaryPanel;
	    }
	   
		// 일정 추가 화면
	   public JPanel addScheScreen() {
			JPanel panel = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					ImageIcon background = new ImageIcon(getClass().getResource("/images/purpleBackground1.png"));
					g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
				}
			};
	        panel.setLayout(null); 
	        panel.setPreferredSize(new Dimension(1200, 800));

	        // 제목 레이블
	        JLabel titleLabel = new JLabel("일정 추가");
	        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 36));
	        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
	        titleLabel.setBounds(400, 50, 400, 50); 
	        panel.add(titleLabel);

	        // 날짜 레이블
	        JLabel dateLabel = new JLabel("날짜 (YYYYMMDD)");
	        dateLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
	        dateLabel.setBounds(200, 150, 200, 30);
	        panel.add(dateLabel);
			// 날짜 입력 필드
	        JTextField dateField = new JTextField();
	        dateField.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
	        dateField.setBounds(450, 150, 300, 40);
	        dateField.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 255), 3, true)); 
	        panel.add(dateField);
	        // 일정 내용 레이블
	        JLabel dataLabel = new JLabel("일정 내용");
	        dataLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
	        dataLabel.setBounds(200, 250, 200, 30);
	        panel.add(dataLabel);
			// 일정 내용 입력 필드
	        JTextArea dataField = new JTextArea();
	        dataField.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
	        dataField.setLineWrap(true); // 줄 바꿈 허용
	        dataField.setWrapStyleWord(true);
	        dataField.setBorder(BorderFactory.createEmptyBorder(5,5, 5, 5)); 
			// 스크롤 패널	
	        JScrollPane dataScrollPane = new JScrollPane(dataField);
	        dataScrollPane.setBounds(450, 250, 500, 300);
	        dataScrollPane.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 255),3,true)); 
	        panel.add(dataScrollPane);
	        // 저장 버튼
	        JButton saveButton = new JButton("Save");
	        saveButton.setBounds(450, 600, 150, 50);
	        styleButton(saveButton, new Color(173, 216, 230)); // 하늘색
	        panel.add(saveButton);
	        // 취소 버튼
	        JButton cancelButton = new JButton("Cancel");
	        cancelButton.setBounds(650, 600, 150, 50);
	        styleButton(cancelButton, new Color(204, 153, 255)); // 보라색
	        panel.add(cancelButton);

	        // 저장 버튼 클릭 이벤트
	        saveButton.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                String date = dateField.getText().trim();
	                String data = dataField.getText().trim();
					// 빈 필드 여부
	                if (date.isEmpty() || data.isEmpty()) {
	                    JOptionPane.showMessageDialog(panel, "모든 필드를 입력해주세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
					// 날짜 형식 검증
	                if (!date.matches("\\d{8}")) {
	                    JOptionPane.showMessageDialog(panel, "날짜는 YYYYMMDD 형식으로 입력해주세요.", "Error", JOptionPane.ERROR_MESSAGE);
	                    return;
	                }
					// addSche 메서드 호출
	                boolean success = scheduler.addSche(user, date, data);
	                
	                	if (success) {
						// 일정 추가 성공
                        JOptionPane.showMessageDialog(panel, "일정 추가 성공!", "Success", JOptionPane.INFORMATION_MESSAGE);
	                } else {
						// 일정 추가 실패
                        JOptionPane.showMessageDialog(panel, "일정 추가 실패!", "Error", JOptionPane.ERROR_MESSAGE);
	                }
	                showScreen("Main"); // 메인 화면으로 이동
	            }
	        });
	        // 취소 버튼 클릭 이벤트
	        cancelButton.addActionListener(e -> showScreen("Main"));

	        return panel;
	    }

		// 일정 조회 화면
	public JPanel showSpecifyScheListScreen() {
        JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon background = new ImageIcon(getClass().getResource("/images/purpleBackground1.png"));
				g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(1200, 800));
     
        // 제목 레이블
        JLabel titleLabel = new JLabel("일정 조회하기");
        titleLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(400, 50, 400, 50);
        panel.add(titleLabel);
 
        String[] scheduleDates = user.getKeyArray(true).join(); // 일정 가져오기
		// 날짜 선택 콤보박스
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
		schedulePanel.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 255), 3, true));
        schedulePanel.setBackground(Color.WHITE);

        // 스크롤 패널에 일정 패널 추가
        JScrollPane scrollPane = new JScrollPane(schedulePanel);
        scrollPane.setBounds(300, 200, 600, 400);
        panel.add(scrollPane);
        // 조회 버튼 클릭 이벤트
        viewButton.addActionListener(e -> {
            schedulePanel.removeAll(); // 기존 일정 제거
            String selectedDate = (String) dateComboBox.getSelectedItem(); 
            String[] schedules = scheduler.specifyScheList(user, selectedDate);
			// 일정 표시
			for (int i = 0; i <  schedules.length; i++) {
                JLabel scheduleLabel = new JLabel((i + 1) + ". " +  schedules[i]);
                scheduleLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
                scheduleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                schedulePanel.add(scheduleLabel);
            }
            schedulePanel.revalidate(); // 레이아웃 재검사
            schedulePanel.repaint(); // 화면 갱신
        });

        // 메인으로 돌아가기 버튼
        JButton backButton = new JButton("메인으로 돌아가기");
        backButton.setBounds(500, 650, 200, 40);
        styleButton(backButton, new Color(153, 153, 255));
		viewButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        backButton.addActionListener(e -> showScreen("Main"));
        panel.add(backButton);

        return panel;
    }

	// 일정 삭제 화면
    public JPanel delScheScreen() {
        JPanel panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				ImageIcon background = new ImageIcon(getClass().getResource("/images/purpleBackground1.png"));
				g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
			}
		};
        panel.setLayout(null);
        panel.setPreferredSize(new Dimension(1200, 800));

        // 제목 레이블
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
		// 일정 가져오기
        String[] schedules = scheduler.specifyScheList(user, selectedDate);
		// 일정 선택 콤보박스
        JComboBox<String> scheduleComboBox = new JComboBox<>(schedules);
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
                boolean success = scheduler.delSche(user, selectedDate, selectedIndex);
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
        styleButton(backButton, new Color(153, 153, 255));
        backButton.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        backButton.addActionListener(e -> showScreen("Main"));
        panel.add(backButton);

        return panel;
    }
	   
	// 버튼 스타일 설정 메서드
    private void styleButton(JButton button, Color bgColor) {
    	button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);		//button.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
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

	// 비밀번호 검증 메서드
    private String validatePassword(String password) {
        if (password.length() < 6) {
            return "비밀번호는 6자리 이상이어야 합니다.";
        }
        
        // 사용할 수 없는 특수문자 검사
        String invalidChars = "(){}[]\\/'\";" ;
        for (char c : password.toCharArray()) {
            if (invalidChars.indexOf(c) != -1) {
                return "비밀번호에 (){}[]\\/'\"; 문자는 사용할 수 없습니다.";
            }
        }   
        return null; // 유효한 비밀번호
    }

    // ID 검증 메서드
    private String validateId(String id) {
        if (id.length() < 6) {
            return "ID는 6자리 이상이어야 합니다.";
        }
		String invalidChars = "(){}[]\\/'\";" ;
		for (char c : id.toCharArray()) {
            if (invalidChars.indexOf(c) != -1) {
                return "비밀번호에 (){}[]\\/'\"; 문자는 사용할 수 없습니다.";
            }
        }
        return null; // 유효한 ID
    }

    // 화면 전환 메서드
	public void showScreen(String name) {
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
				mainPanel.add(mainScreen(), "Main");
				break;
			case "AddSchedule":
				mainPanel.add(addScheScreen(), "AddSchedule");
				break;
			case "SpecifyScheList":
				mainPanel.add(showSpecifyScheListScreen(), "SpecifyScheList");
				break;
			case "DeleteSchedule":
				mainPanel.add(delScheScreen(), "DeleteSchedule");
				break;
		}
		cardLayout.show(mainPanel, name);
		mainPanel.revalidate();
		mainPanel.repaint();
    }

	// main() 메서드
    public static void main(String[] args) {

        Register register = new Register();
        Scheduler scheduler = new Scheduler();

        new SchedulerInterface(register, scheduler);
    }

}
