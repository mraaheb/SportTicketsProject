package C_Data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CSVUtils {
    private static final String USERS   = "data/users.csv";
    private static final String MATCHES = "data/matches.csv";

    // ===== Users =====
    public static synchronized List<User> readUsers() throws IOException {
        List<User> list = new ArrayList<>();
        File f = new File(USERS);
        if (!f.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln.isBlank()) continue;
                String[] p = ln.split("\\s*,\\s*");
                if (p.length >= 2) {
                    list.add(new User(p[0], p[1]));
                }
            }
        }
        return list;
    }

    public static synchronized boolean appendUser(User u) throws IOException {
        for (User x : readUsers())
            if (x.username.equalsIgnoreCase(u.username))
                return false;

        File f = new File(USERS);
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8, true))) {
            if (f.exists() && f.length() > 0) bw.newLine();
            bw.write(u.username + "," + u.password);
        }
        return true;
    }

    public static synchronized boolean checkLogin(String u, String p) throws IOException {
        for (User x : readUsers())
            if (x.username.equalsIgnoreCase(u) && x.password.equals(p))
                return true;
        return false;
    }

    // ===== Matches =====
    public static synchronized List<Match> readMatches() throws IOException {
        List<Match> list = new ArrayList<>();
        File f = new File(MATCHES);
        if (!f.exists()) return list; // حماية لو الملف مو موجود

        try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                if (ln.isBlank()) continue;
                String[] p = ln.split("\\s*,\\s*");
                if (p.length == 6) {
                    int av = 0;
                    try { av = Integer.parseInt(p[5]); } catch (Exception ignore) {}
                    list.add(new Match(p[0], p[1], p[2], p[3], p[4], av));
                }
            }
        }
        return list;
    }

    public static synchronized List<Match> filter(String event, String date) throws IOException {
        String ev = event == null ? "" : event.trim();
        String dt = date  == null ? "" : date.trim();
        List<Match> out = new ArrayList<>();
        for (Match m : readMatches()) {
            if (m.event.equalsIgnoreCase(ev) && m.date.equals(dt)) {
                out.add(m);
            }
        }
        return out;
    }

    public static synchronized boolean reserve(String id, int count) throws IOException {
        List<Match> list = readMatches();
        boolean ok = false;

        for (Match m : list) {
            if (m.id.equalsIgnoreCase(id)) {
                if (m.available >= count) {
                    m.available -= count;
                    ok = true;
                } else {
                    return false;
                }
            }
        }
        if (ok) writeMatches(list);
        return ok;
    }

    private static void writeMatches(List<Match> list) throws IOException {
        File f = new File(MATCHES);
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8, false))) {
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) bw.newLine();
                bw.write(list.get(i).toCSV());
            }
        }
    }

    // ===== Helpers for UI/server =====

    // أوصاف المباريات المتاحة لحدث معيّن (distinct) بشرط المتبقي > 0
    public static synchronized List<String> listAvailableMatchesForEvent(String event) throws IOException {
        String ev = event == null ? "" : event.trim();
        LinkedHashSet<String> set = new LinkedHashSet<>(); // يحافظ على الترتيب
        for (Match m : readMatches()) {
            if (m.event.equalsIgnoreCase(ev) && m.available > 0) {
                set.add(m.desc);
            }
        }
        return new ArrayList<>(set);
    }

    // التواريخ المتاحة لوصف مباراة داخل حدث معيّن (distinct) بشرط المتبقي > 0
    public static synchronized List<String> listAvailableDatesFor(String event, String desc) throws IOException {
        String ev = event == null ? "" : event.trim();
        String ds = desc  == null ? "" : desc.trim();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (Match m : readMatches()) {
            if (m.event.equalsIgnoreCase(ev)
                    && m.desc.equalsIgnoreCase(ds)
                    && m.available > 0) {
                set.add(m.date);
            }
        }
        return new ArrayList<>(set);
    }
    // تواريخ متاحة لحدث معيّن (distinct) بشرط available > 0
// تواريخ متاحة لحدث معيّن (distinct) بشرط available > 0 (مرتبة ومُنظَّفة)
public static synchronized java.util.List<String> listAvailableDatesForEvent(String event) throws java.io.IOException {
    String ev = (event == null ? "" : event.trim());
    java.util.TreeSet<String> set = new java.util.TreeSet<>(); // ترتيب تصاعدي + بدون تكرار
    for (Match m : readMatches()) {
        // فلترة صارمة على الحدث + متوفر > 0
        if (m.available > 0 && m.event.equalsIgnoreCase(ev)) {
            set.add(m.date.trim());
        }
    }
    return new java.util.ArrayList<>(set);
}


}
