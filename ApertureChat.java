import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class ApertureChat extends JFrame {
    private JTextArea area; private JTextField in, ip, port;
    private JButton serverBtn, connectBtn, sendBtn;
    private ServerSocket server; private Socket socket;
    private PrintWriter out; private BufferedReader br;
    private boolean isRun = false;

    public ApertureChat() {
        setTitle("Aperture LAN Chat v1.0"); setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE); setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout(5, 5)); p.setBackground(new Color(30,35,40));
        
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(new Color(45,50,55));
        serverBtn = new JButton("Создать Сервер"); top.add(serverBtn);
        top.add(new JLabel("IP:")).setForeground(Color.WHITE);
        ip = new JTextField("127.0.0.1", 8); top.add(ip);
        top.add(new JLabel("Порт:")).setForeground(Color.WHITE);
        port = new JTextField("7777", 4); top.add(port);
        connectBtn = new JButton("Подключиться"); top.add(connectBtn); // КНОПКА ТУТ!
        p.add(top, BorderLayout.NORTH);

        area = new JTextArea(); area.setBackground(Color.BLACK);
        area.setForeground(new Color(255, 127, 39)); area.setEditable(false);
        p.add(new JScrollPane(area), BorderLayout.CENTER);

        JPanel bot = new JPanel(new BorderLayout(5, 0));
        in = new JTextField(); in.setEnabled(false); bot.add(in, BorderLayout.CENTER);
        sendBtn = new JButton("ОТПРАВИТЬ"); sendBtn.setEnabled(false); bot.add(sendBtn, BorderLayout.EAST);
        p.add(bot, BorderLayout.SOUTH); add(p);

        in.addActionListener(e -> send()); sendBtn.addActionListener(e -> send());
        serverBtn.addActionListener(e -> startServer()); connectBtn.addActionListener(e -> startClient());
        
        try { area.append("[Система]: Ваш IP: " + InetAddress.getLocalHost().getHostAddress() + "\n"); } catch(Exception ignored){}
    }

    private void startServer() {
        new Thread(() -> {
            try {
                int prt = Integer.parseInt(port.getText().trim());
                server = new ServerSocket(prt); serverBtn.setEnabled(false); connectBtn.setEnabled(false);
                area.append("[Система]: Сервер запущен. Ожидание...\n");
                socket = server.accept(); initStreams();
            } catch(Exception e) { reset(); }
        }).start();
    }

    private void startClient() {
        new Thread(() -> {
            try {
                String sIp = ip.getText().trim(); int prt = Integer.parseInt(port.getText().trim());
                serverBtn.setEnabled(false); connectBtn.setEnabled(false);
                area.append("[Система]: Подключение к " + sIp + "...\n");
                socket = new Socket(sIp, prt); initStreams();
            } catch(Exception e) { area.append("[Ошибка]: " + e.getMessage() + "\n"); reset(); }
        }).start();
    }

    private void initStreams() throws Exception {
        out = new PrintWriter(socket.getOutputStream(), true);
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isRun = true; in.setEnabled(true); sendBtn.setEnabled(true);
        area.append("[Система]: Соединение установлено!\n");
        new Thread(() -> {
            try { String msg; while(isRun && (msg = br.readLine()) != null) { area.append(msg + "\n"); } }
            catch(Exception e) { if(isRun) area.append("[Система]: Связь разорвана.\n"); } finally { reset(); }
        }).start();
    }

    private void send() {
        String txt = in.getText().trim(); if(txt.isEmpty()) return;
        String mode = (server != null) ? "Сервер" : "Клиент";
        String msg = "[" + mode + "]: " + txt; out.println(msg); area.append(msg + "\n"); in.setText("");
    }

    private void reset() {
        isRun = false; try{if(socket!=null)socket.close();}catch(Exception ignored){}
        try{if(server!=null)server.close();}catch(Exception ignored){}
        server = null; socket = null; in.setEnabled(false); sendBtn.setEnabled(false);
        serverBtn.setEnabled(true); connectBtn.setEnabled(true);
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new ApertureChat().setVisible(true)); }
}
