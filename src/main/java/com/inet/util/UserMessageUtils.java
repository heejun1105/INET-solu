package com.inet.util;

/**
 * 사용자에게 노출되는 오류 메시지를 기술적 예외 대신 이해하기 쉬운 문구로 변환합니다.
 */
public final class UserMessageUtils {

    private UserMessageUtils() {}

    /**
     * 예외 메시지를 사용자 친화적인 한글 메시지로 변환합니다.
     * @param e 예외 (null 가능)
     * @param contextLabel 화면/기능 이름 (예: "장비 엑셀 업로드", "학교 추가")
     * @return 사용자에게 보여줄 메시지
     */
    public static String toUserFriendly(Throwable e, String contextLabel) {
        if (e == null) {
            return contextLabel + " 중 오류가 발생했습니다. 다시 시도해 주세요.";
        }
        String msg = e.getMessage() != null ? e.getMessage().trim() : "";
        if (msg.isEmpty()) {
            return contextLabel + " 중 오류가 발생했습니다. 다시 시도해 주세요.";
        }
        String lower = msg.toLowerCase();

        // 이미 사용자 친화적인 한글 메시지면 그대로 반환 (우리 서비스에서 던진 메시지)
        if (isAlreadyUserFriendly(msg, lower)) {
            return msg;
        }

        // 기술적 예외 패턴 → 공통 안내
        if (lower.contains("query did not return a unique result") || lower.contains("results were returned")
                || lower.contains("nonuniqueresult") || lower.contains("unique result")) {
            return "입력한 데이터가 이미 다른 항목과 중복됩니다. 중복되지 않는 값으로 다시 입력해 주세요.";
        }
        if (lower.contains("dataintegrityviolation") || lower.contains("constraint") || lower.contains("duplicate entry")
                || lower.contains("unique constraint") || lower.contains("duplicate key") || lower.contains("foreign key")) {
            return "이미 사용 중인 데이터와 충돌합니다. 중복된 값이 없는지 확인한 후 다시 시도해 주세요.";
        }
        if (lower.contains("nullpointer") || lower.contains("illegalargument") || lower.contains("numberformatexception")) {
            return contextLabel + " 중 오류가 발생했습니다. 입력 내용을 확인한 후 다시 시도해 주세요.";
        }
        if (lower.contains("java.") || lower.contains("sql.") || lower.contains("at com.") || lower.contains("at org.")
                || lower.contains("exception") || lower.contains("error:") || msg.contains("Caused by:")) {
            return contextLabel + " 중 오류가 발생했습니다. 다시 시도해 주세요. 문제가 계속되면 관리자에게 문의해 주세요.";
        }

        // 짧고 한글이 섞여 있으면 그대로 (예: 서비스에서 던진 짧은 메시지)
        if (msg.length() <= 120 && (msg.contains("을") || msg.contains("를") || msg.contains("가") || msg.contains("이") || msg.contains("해주세요"))) {
            return msg;
        }

        return contextLabel + " 중 오류가 발생했습니다. 다시 시도해 주세요.";
    }

    private static boolean isAlreadyUserFriendly(String msg, String lower) {
        if (msg.length() > 200) return false;
        if (lower.contains("exception") || lower.contains("error:") || lower.contains("at ")) return false;
        // 우리가 던지는 한글 메시지 패턴
        return msg.contains("이미") || msg.contains("중복") || msg.contains("필요") || msg.contains("입력")
                || msg.contains("번째 행") || msg.contains("확인") || msg.contains("해주세요") || msg.contains("올바르지 않습니다")
                || msg.contains("찾을 수 없습니다") || msg.contains("권한이 없습니다");
    }
}
