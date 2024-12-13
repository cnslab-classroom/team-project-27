package schedulerAlertApp;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.checkerframework.checker.units.qual.C;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Arrays; //임시

public class Scheduler {
    public boolean addSche(Register user, String date, String data) {
        // 1. Precondition 확인
        if (user == null || date == null || data == null || data.isEmpty()) {
            System.err.println("Invalid input: user, date, or data is null or empty.");
            return false;
        }

        // 2. 날짜 형식 검증
        if (!date.matches("\\d{8}")) {
            System.err.println("Invalid date format. Expected format: YYYYMMDD.");
            return false;
        }

        // 3. Firebase 경로 구성
        String path = "schedules/" + date.replace("/", ""); // "schedules/20241130" 형식으로 변환

        // 4. 데이터 저장
        try {
            CompletableFuture<Boolean> future = user.setData(path, data); // path에 일정 추가
            boolean result = future.get(); // 비동기 작업 완료 대기

            // 5. 결과 반환
            if (result) {
                System.out.println("Schedule added successfully: " + path);
                return true;
            } else {
                System.err.println("Failed to add schedule.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while adding schedule: " + e.getMessage());
            return false;
        }
    }

    public TreeMap<Integer, Integer> allScheSummary(Register user) {
        TreeMap<Integer, Integer> scheduleSummary = new TreeMap<>(); // 결과 저장

        try {
            // 1. 모든 일정의 키(key) 목록 가져오기
            CompletableFuture<String[]> futureKeys = user.getKeyArray(true);
            List<String> keys = new ArrayList<>();
            keys = Arrays.asList(futureKeys.get()); // 비동기 결과 가져오기

            // keys 유효성 검사
            if (keys == null || keys.isEmpty()) {
                System.out.println("No schedules found.");
                return scheduleSummary; // 빈 TreeMap 반환
            }

            // 2. 현재 날짜 가져오기
            LocalDate todayDate = LocalDate.now(); // 현재 날짜 객체
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd"); // 날짜 포맷
            String today = todayDate.format(formatter); // 현재 날짜 문자열

            // 3. 각 키에 대해 데이터를 가져와 날짜 차이 계산
            boolean isDel = true; // 삭제 성공 여부 확인
            List<String> effectiveDate = new ArrayList<>(); // 유효한 일정 날짜 저장

            for (String key : keys) {
                if (today.compareTo(key) > 0) { // 지난 일정인지 확인
                    // 지난 일정 : delSch() 호출 -> 삭제
                    isDel = delSche(user, key, -1);
                } else {
                    // 유효한 일정이므로 List에 추가
                    effectiveDate.add(key);
                }
            }

            // 4. 유효한 일정을 날짜 별로 데이터를 가져와 일정 개수를 계산
            List<Integer> scheNum = new ArrayList<>(); // effectiveDate index 순서대로 일정 개수 저장
            List<CompletableFuture<List>> futureData = new ArrayList<>();

            for (int i = 0; i < effectiveDate.size(); i++) {
                // 해당되는 user의 schedules 경로에 들어가 유효한 일정 날짜를 가져옴
                futureData.add(user.getData("schedules/" + effectiveDate.get(i), List.class));
            }

            for (int i = 0; i < effectiveDate.size(); i++) {
                // futureData(비동기)를 동기적으로 가져와 일정 개수 계산
                List<String> data = futureData.get(i).get();

                scheNum.add(data.size()); // 각 일정 별 일정 개수 추가
            }

            // 5. 저장된 일정의 날짜까지 남은 일수와 해당 일정의 개수를 scheduleSummary(TreeMap)에 추가
            for (int i = 0; i < effectiveDate.size(); i++) {
                // effectiveDate에 저장된 날짜 문자열 LocalDate로 변환
                LocalDate date = LocalDate.parse(effectiveDate.get(i), formatter);

                // 현재 날짜(todayDate)와 일정 날짜(date) 사이의 남은 일수 계산
                Long daysRemaining = ChronoUnit.DAYS.between(todayDate, date);

                // scheduleSummary(TreeMap)에 남은 일수와 일정 개수를 저장
                scheduleSummary.put(daysRemaining.intValue(), scheNum.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while fetching schedule summary.");
        }

        return scheduleSummary; // 결과 반환
    }

    public String[] specifyScheList(Register user, String date) {
        // 1. 반환할 일정 목록을 저장할 리스트 선언
        List<String> fetchedData = new ArrayList<>(); // 가져온 데이터를 fetchedData(List) 추가

        try {
            // 2. Firebase 경로 설정
            String path = "schedules/" + date.replace("/", ""); // 예: "schedules/20241212"

            // 3. 경로 디버깅 로그 추가
            // System.out.println("Fetching data from path: " + path);

            // 3. 해당 경로에서 데이터를 가져오기
            CompletableFuture<List> futureData = user.getData(path, List.class);
            fetchedData = futureData.get(); // 비동기 결과 가져오기

            // 4. 가져온 데이터가 비어있지 않으면 리스트에 추가(데이터 get에 성공한 경우)
            if (fetchedData != null) {
                // 일정 데이터 추가
                System.out.println("Success");
                return fetchedData.toArray(new String[0]); // List -> String
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while fetching schedule list for date: " + date);
        }

        // 5. 문자열 배열로 변환하여 반환
        return fetchedData.toArray(new String[0]);
    }

    public boolean delSche(Register user, String date, int index) {
        try {
            // 1. Firebase 경로 설정
            String path = "schedules/" + date.replace("/", ""); // 예: "schedules/20241212"

            // 2. Firebase에서 데이터 가져오기
            CompletableFuture<List> getFuture = user.getData(path, List.class);
            List<String> todoList = new ArrayList<>(); // 일정 목록

            // 데이터 가져오기 성공
            getFuture.thenRun(() -> {
                System.out.println("Deleting Schedule is successfully.");
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                return null;
            });

            // 비동기 결과를 동기적으로 가져오기
            try {
                todoList = getFuture.get(); // 비동기 작업이 끝날 때까지 대기
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Data retrieved: " + todoList);

            // 3. 데이터 검증
            if (todoList == null) {
                System.err.println("No schedules found for date: " + date);
                return false;
            }

            // 4. 인덱스에 따라 데이터 삭제 처리
            CompletableFuture<Boolean> delFuture = user.delData(path, index); // index에 있는 일정 삭제

            // 데이터 삭제 성공
            delFuture.thenApply(result -> {
                System.out.println("Password updated successfully.");
                return true;
            }).exceptionally(e -> {
                System.err.println("Error setting password: " + e.getMessage());
                return false;
            });

            // 비동기 작업 결과 동기적으로 가져오기
            try {
                if (delFuture.get()) // wait for the asynchronous task to complete
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error while deleting schedule at index: " + index + " for date: " + date);
        }
        return false;
    }

    public static void main(String[] args) {

        Scheduler scheduler1 = new Scheduler();
        Register registers = new Register();

        /*
        int result;
        result = registers.login("abcd", "abcd1234", true);
        System.out.println("Login result: " + result);
        System.out.println("UserId: " + registers.getUserId());

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TreeMap<Integer, Integer> mapData = scheduler1.allScheSummary(registers);

        for (Map.Entry<Integer, Integer> entry : mapData.entrySet()) {
            System.out.println(entry.getValue() + "개의 일정이 " + entry.getKey() +
                    "일 남았습니다!");
        }
        */
        

    }
}
