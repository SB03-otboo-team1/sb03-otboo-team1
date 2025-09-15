package com.onepiece.otboo.infra.seeder;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

final class SeedUtils {

    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RND = new SecureRandom();

    private SeedUtils() {
    }

    static String randStr(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RND.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    static int randInt(int min, int max) {
        return min + RND.nextInt((max - min) + 1);
    }

    static double randDouble(double min, double max) {
        return min + (max - min) * RND.nextDouble();
    }

    static UUID uuid() {
        return UUID.randomUUID();
    }

    static List<String> fetchIds(JdbcTemplate jdbc, String table) {
        return jdbc.query("SELECT id::varchar FROM " + table, (rs, i) -> rs.getString(1));
    }

    static boolean hasAny(JdbcTemplate jdbc, String table) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return c != null && c > 0;
    }

    static <T> List<T> pickSome(List<T> source, int count) {
        if (source.isEmpty()) {
            return List.of();
        }
        List<T> res = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            res.add(source.get(RND.nextInt(source.size())));
        }
        return res;
    }

    static <T> Set<Pair<T, T>> uniquePairs(List<T> items, int targetCount) {
        Set<Pair<T, T>> pairs = new HashSet<>();
        int n = items.size();
        if (n < 2) {
            return pairs;
        }
        while (pairs.size() < targetCount) {
            T a = items.get(RND.nextInt(n));
            T b = items.get(RND.nextInt(n));
            if (!a.equals(b)) {
                pairs.add(new Pair<>(a, b));
            }
            if (pairs.size() > n * n) {
                break; // 루프 방지
            }
        }
        return pairs;
    }

    static Instant now() {
        return Instant.now();
    }

    static final class Pair<A, B> {

        final A a;
        final B b;

        Pair(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }

        @Override
        public int hashCode() {
            return (a == null ? 0 : a.hashCode()) * 31 + (b == null ? 0 : b.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Pair<?, ?> p)) {
                return false;
            }
            return (a == null ? p.a == null : a.equals(p.a)) && (b == null ? p.b == null
                : b.equals(p.b));
        }
    }
}

