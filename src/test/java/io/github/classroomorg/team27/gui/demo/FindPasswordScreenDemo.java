package io.github.classroomorg.team27.gui.demo;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// 다른 클래스와의 상호작용 코드 제외 또는 주석 처리
public class FindPasswordScreenDemo {
	private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    public FindPasswordScreenDemo() {
 		frame = new JFrame("Scheduler Interface");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(800, 600);
         frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
         
         cardLayout = new CardLayout();
         mainPanel = new JPanel(cardLayout);
         mainPanel.add(findPasswordScreen(), "Login");
         frame.add(mainPanel);
         frame.setVisible(true);
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

        // 재설정 버튼
        JButton resetButton = new JButton("비밀번호 재설정");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
//        resetButton.addActionListener(e -> {
//            String id = idField.getText();
//            int questionIndex = questionComboBox.getSelectedIndex(); // 선택된 질문의 인덱스
//            String questionAns = answerField.getText();
//            String newPassword = new String(newPasswordField.getPassword());
//
//            // Register 클래스의 findPassword 메서드 호출
//            int result = user.findPassword(id, questionIndex, questionAns, newPassword);
//
//            // 결과 처리
//            if (result == 1) {
//                JOptionPane.showMessageDialog(panel, "비밀번호 재설정 성공!", "성공", JOptionPane.INFORMATION_MESSAGE);
//                showScreen("Login"); // 로그인 화면으로 전환
//            } else if (result == 2) {
//                JOptionPane.showMessageDialog(panel, "ID가 존재하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
//            } else if (result == 3) {
//                JOptionPane.showMessageDialog(panel, "질문 정보 또는 답변이 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
//            } else {
//                JOptionPane.showMessageDialog(panel, "알 수 없는 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
//            }
//        });
        panel.add(resetButton);

        // 아래쪽 유연한 공간 추가 
        panel.add(Box.createVerticalGlue());

        return panel;
    }



	public static void main(String[] args) {
		new FindPasswordScreenDemo();

	}

}
