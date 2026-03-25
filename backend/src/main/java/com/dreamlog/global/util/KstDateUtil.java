package com.dreamlog.global.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class KstDateUtil {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private KstDateUtil() {}

    public static LocalDate todayKst() {
        return LocalDate.now(KST);
    }

    public static LocalDateTime nowKst() {
        return LocalDateTime.now(KST);
    }

    public static ZoneId kstZone() {
        return KST;
    }
}
