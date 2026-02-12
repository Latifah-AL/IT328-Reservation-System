
import java.io.*;
import java.net.*;

//( كلاس مسؤول عن اتصال العميل مع السيرفر (لارسال الاوامر واستقبال الردود 
public class NetworkClient {
    private static final String HOST = "localhost"; // عنوان السيرفر المحلي
    private static final int PORT = 9090;           // منفذ السيرفر

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected = false;  // حالة الاتصال

    // الاتصال مع السيرفر
    public boolean connect() {
        if (connected) return true;
        try {
            // فتح اتصال TCP مع السيرفر
            socket = new Socket(HOST, PORT);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            connected = true;
            return true;
        } catch (Exception e) {
            return false; // فشل الاتصال
        }
    }
    
    // إرسال أمر نصي للسيرفر واستقبال الرد كـ String
    private String send(String msg) {
        try {
            if (!connected) return "ERROR|NotConnected";
            out.println(msg);
            String res = in.readLine();
            return (res == null) ? "ERROR|NoResponse" : res;
        } catch (Exception e) {
            return "ERROR|IO";
        }
    }
    
    // أمر التسجيل (REGISTER)
    public String register(String user, String pass) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("REGISTER|" + user + "|" + pass);
    }
    
    // أمر تسجيل الدخول(LOGIN)
    public String login(String user, String pass) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("LOGIN|" + user + "|" + pass);
    }
    
    // أمر فحص حالة التوفر (AVAIL)
    public String avail(String r, String d, String s) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("AVAIL|" + r + "|" + d + "|" + s);
    }
    
    // أمر الحجز (RESERVE)
    public String reserve(String user, String r, String d, String s) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("RESERVE|" + user + "|" + r + "|" + d + "|" + s);
    }
    
    // أمر تصفح الحجوزات (GET_RES)
    public String getReservations(String user) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("GET_RES|" + user);
    }
    
    // أمر الغاء الحجز (RESERVE)
    public String cancelReservation(String user, String r, String d, String s) {
        if (!connect()) return "ERROR|ConnectFailed";
        return send("CANCEL|" + user + "|" + r + "|" + d + "|" + s);
    }
}





