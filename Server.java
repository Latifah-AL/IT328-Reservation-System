import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
    public static void main(String[] args) throws IOException {
        // إنشاء سيرفر يستقبل اتصالات من العملاء(TCP)
        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server listening on port 9090");

        while (true) {
            // قبول عميل جديد وتشغيله بشكل مستقل
            Socket client = serverSocket.accept();
            System.out.println("Connected to client ");
            new Thread(new ClientHandler(client)).start();
        }
    }
}
class ClientHandler implements Runnable { 
    
    // تعريف المتغيرات الأساسية للاتصال
    private final Socket client;              
    private final BufferedReader in;         
    private final PrintWriter out;  
    
   // المستخدمون
    // Map: تعني حفظ اسم المستخدم مع كلمة المرور
    private static final Map<String,String> users = new HashMap<>();

    // القوائم الثابتة
    private static final List<String> restaurants =
            Arrays.asList("Lazy Cat","Entrecote","Lunch Room");
    private static final List<String> days =
            Arrays.asList("Sat","Sun","Mon","Tue","Wed","Thu","Fri");
    private static final List<String> slots =
            Arrays.asList("17:00","18:00","19:00","20:00","21:00");

    // التوفر: 1 متاح / 0 محجوز
    private static final int[][][] availability =
            new int[restaurants.size()][days.size()][slots.size()];

    // حجوزات كل مستخدم
    private static final Map<String,List<String>> reservations =
            Collections.synchronizedMap(new HashMap<>());

    static {
        // ضبط كل الموعد كمتاح 
        for (int i=0;i<restaurants.size();i++)
            for (int j=0;j<days.size();j++)
                for (int k=0;k<slots.size();k++)
                    availability[i][j][k] = 1;
    }



    // عميل واحد
   public ClientHandler(Socket c) throws IOException {
        this.client = c;
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
    }

    @Override
    public void run() {
        try {
            String line;
            // استقبال الأوامر النصية من العميل و انشاء الرد و ارساله
            while ((line = in.readLine()) != null) {
                String response = handle(line.trim());
                out.println(response);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        } finally {
            // إغلاق الموارد
            try { in.close(); } catch (Exception ignore) {}
            try { out.close(); } catch (Exception ignore) {}
            try { client.close(); } catch (Exception ignore) {}
        }
    }

        // تنفيذ الأوامر: REGISTER / LOGIN / AVAIL / RESERVE / GET_RES / CANCEL
        private String handle(String line){
            if (line.isEmpty()) return "ERROR|BadRequest";
            
            // نفصل البيانات بـ "|"
            String[] p = line.split("\\|");
            String cmd = p[0];

            // REGISTER|user|pass عند تسجيل العميل
            if ("REGISTER".equals(cmd)) {
                if (p.length < 3) return "ERROR|BadRequest";
                String u = p[1].trim(), pass = p[2].trim();
                if (u.isEmpty() || pass.isEmpty()) return "ERROR|EmptyFields";
                
                if (users.containsKey(u)) return "User Exists";

                // يضيف اليوزر
                users.put(u, pass);

                // يربط لليوزر قائمه بالحجوزات الخاصه فيه
                reservations.put(u, new ArrayList<>());
                return "OK|Registered";
            }

            // LOGIN|user|pass عند محاولة العميل تسجيل الدخول
            if ("LOGIN".equals(cmd)) {
                if (p.length < 3) return "ERROR|BadRequest";
                String u = p[1].trim(), pass = p[2].trim();
                if (u.isEmpty() || pass.isEmpty()) return "ERROR|EmptyFields";

                String saved = users.get(u);
                if (saved != null && saved.equals(pass)) {
                    if (!reservations.containsKey(u))
                        reservations.put(u, new ArrayList<>()); 
                    return "OK|LoggedIn";
                }
                return "wrong username or password";
            }

            // AVAIL|r|d|s عند محاولة العميل لمعرفة المواعيد المتاحة
            if ("AVAIL".equals(cmd)) {
                if (p.length < 4) return "ERR|BadRequest";
                int r = restaurants.indexOf(p[1]);
                int d = days.indexOf(p[2]);
                int s = slots.indexOf(p[3]);
                if (r < 0 || d < 0 || s < 0) return "ERROR|BadRequest";
                return "AVAIL_RES|" + availability[r][d][s];
            }

            // RESERVE|user|r|d|s عند محاولة العميل للحجز
            if ("RESERVE".equals(cmd)) {
                if (p.length != 5) return "ERROR|BadRequest";
                String username = p[1].trim();
                if (!users.containsKey(username)) return "ERROR|NotRegistered";

                int r = restaurants.indexOf(p[2]);
                int d = days.indexOf(p[3]);
                int s = slots.indexOf(p[4]);
                if (r < 0 || d < 0 || s < 0) return "BadRequest";
                
                // اذا حاول العميل حجز موعد غير متاح
                if (availability[r][d][s] <= 0) return "The time slot is not available ";
                
                // تحديث الحالة   
                availability[r][d][s] = 0; 

                String rec = p[2] + "|" + p[3] + "|" + p[4];

                reservations.get(username).add(rec);

                return "OK|Reserved";
            }

            // GET_RES|user  استرجاع الحجوزات السابقه للمستخدم
            if ("GET_RES".equals(cmd)) {
                if (p.length != 2) return "BadRequest";
                String username = p[1].trim();
                if (!users.containsKey(username)) return "NotRegistered";

                List<String> list = reservations.get(username);
                if (list == null || list.isEmpty()) return "RES_LIST|";

                String result = "RES_LIST";
                for (String rec : list) {
                    String fixed = rec.replace("|", ";"); // change | to ;
                    result += "|" + fixed;
                }
                return result;
            }

            // CANCEL|user|r|d|s عند محاولة العميل لالغاء الحجز
            if ("CANCEL".equals(cmd)) {
                if (p.length != 5) return "ERROR|BadRequest";
                String u = p[1].trim();
                if (!users.containsKey(u)) return "ERROR|NotRegistered";

                int r = restaurants.indexOf(p[2]);
                int d = days.indexOf(p[3]);
                int s = slots.indexOf(p[4]);
                if (r < 0 || d < 0 || s < 0) return "ERROR|BadRequest";

                String rec = p[2] + "|" + p[3] + "|" + p[4];

                List<String> list = reservations.get(u);
                if (list == null || !list.remove(rec)) return "ERROR|NotFound";

                availability[r][d][s] = 1; // تحديث الحالة

                return "OK|Cancelled";
            }
            
             // أمر غير معروف
            return "ERROR|BadRequest";
        }
 }

