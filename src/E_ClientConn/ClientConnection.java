package E_ClientConn;

import java.io.*;
import java.net.Socket;

public class ClientConnection implements Closeable {

    // ===== Lab-style defaults (server endpoint) =====
    // On server machine: keep "localhost"
    // On client machines: change to your server IP, e.g. "192.168.1.25"
    public static final String DEFAULT_HOST = "localhost";
    public static final int    DEFAULT_PORT = 9090;

    private Socket sock;
    private BufferedReader in;
    private DataOutputStream out;

    // Connect using defaults (host/port) — matches lab style
    public boolean connect() {
        return connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    // Connect using explicit host/port (still available if you need it)
    public boolean connect(String host, int port) {
        try {
            sock = new Socket(host, port);
           in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
           out = new DataOutputStream(sock.getOutputStream());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Send a single line; expect a single line back
    public String sendOneLine(String msg) throws IOException {
        out.writeBytes(msg + "\n");
        return in.readLine();
    }

    // Send request; read as many lines as server has ready (multi-line)
    /*public String sendMultiLine(String msg) throws IOException {
        out.writeBytes(msg + "\n");
        sock.setSoTimeout(120);
        StringBuilder sb = new StringBuilder();
        String ln;
        try {
            while ((ln = in.readLine()) != null) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(ln);
                if (!in.ready()) break;
            }
        } catch (IOException ex) {
            // swallow read timeout/EOF — return what we have
        }
        return sb.toString();
    }*/
    // داخل التابع public String sendMultiLine(String msg)
    public String sendMultiLine(String msg) throws IOException {
        out.writeBytes(msg + "\n");
        
        // زيادة المهلة المؤقتة
        sock.setSoTimeout(1000); // 1000 مللي ثانية (ثانية واحدة)
        
        StringBuilder sb = new StringBuilder();
        String ln;
        try {
            // الاعتماد فقط على المهلة للانتهاء، مع إزالة شرط ready()
            while (true) {
                ln = in.readLine();
                if (ln == null) break;
                
                if (sb.length() > 0) sb.append("\n");
                sb.append(ln);
            }
        } catch (java.net.SocketTimeoutException ex) {
            // تجاهل مهلة القراءة
        } catch (IOException ex) {
            // خطأ آخر
        } finally {
            // إعادة ضبط المهلة إلى صفر بعد الانتهاء
            try { sock.setSoTimeout(0); } catch (Exception ignore) {}
        }
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        try { if (in   != null) in.close();   } catch (Exception ignore) {}
        try { if (out  != null) out.close();  } catch (Exception ignore) {}
        try { if (sock != null) sock.close(); } catch (Exception ignore) {}
    }
}
