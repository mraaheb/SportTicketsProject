package B_Server;

import C_Data.*;
import D_Protocol.Protocol;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket s;

    public ClientHandler(Socket s) { this.s = s; }

    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            DataOutputStream out = new DataOutputStream(s.getOutputStream())
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                String resp = handle(line.trim());
                out.writeBytes(resp + "\n");
            }
        } catch (IOException e) {
            System.err.println("Disconnect: " + e.getMessage());
        } finally {
            try { s.close(); } catch (Exception ignore) {}
        }
    }

    private String handle(String m) {
        try {
            String[] p = m.split("\\s*,\\s*");
            if (p.length == 0 || p[0].isBlank()) return Protocol.ERROR + ",BAD_REQUEST";

            switch (p[0].toUpperCase()) {

                case "REGISTER":
                    if (p.length < 3) return Protocol.ERROR + ",ARGS";
                    return CSVUtils.appendUser(new User(p[1], p[2]))
                            ? Protocol.OK
                            : Protocol.ERROR + ",USER_EXISTS";

                case "LOGIN":
                    if (p.length < 3) return Protocol.ERROR + ",ARGS";
                    return CSVUtils.checkLogin(p[1], p[2])
                            ? Protocol.OK
                            : Protocol.ERROR + ",INVALID";

                case "DATES": { // DATES,<event> -> يرجّع سطر لكل تاريخ متاح لهذا الحدث
                     if (p.length < 2) return Protocol.ERROR + ",ARGS";
                     java.util.List<String> xs = C_Data.CSVUtils.listAvailableDatesForEvent(p[1]);
                     StringBuilder sb = new StringBuilder();
                     for (int i = 0; i < xs.size(); i++) {
                        if (i > 0) sb.append('\n');
                        sb.append(xs.get(i));
    }
                     return sb.toString();
}


                case "LIST": // LIST,<event>,<date>
                    if (p.length < 3) return Protocol.ERROR + ",ARGS";
                    String event = p[1], date = p[2];
                    List<Match> matches = CSVUtils.filter(event, date);
                    StringBuilder sb = new StringBuilder();
                    for (Match m0 : matches) {
                        if (m0.available > 0) {
                            if (sb.length() > 0) sb.append('\n');
                            sb.append(m0.id)
                              .append("|")
                              .append(m0.time)
                              .append("|")
                              .append(m0.desc)
                              .append("|")
                              .append(m0.available);
                        }
                    }
                    return sb.toString();

                case "RESERVE": // RESERVE,<id>,<count>
                    if (p.length < 3) return Protocol.ERROR + ",ARGS";
                    int c = 1;
                    try { c = Integer.parseInt(p[2]); } catch (Exception ignore) {}
                    boolean ok = CSVUtils.reserve(p[1], c);
                    return ok ? Protocol.CONFIRMED : Protocol.SOLD_OUT;

                default:
                    return Protocol.ERROR + ",UNKNOWN_CMD";
            }

        } catch (Exception e) {
            return Protocol.ERROR + "," + e.getMessage();
        }
    }

    private String joinLines(List<String> xs) {
        if (xs == null || xs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(xs.get(i));
        }
        return sb.toString();
    }
}
