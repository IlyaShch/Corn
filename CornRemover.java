import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;


public class CornRemover
{
    public static void main(final String[] args) {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf("@"));
        System.out.println(pid);
        try {
            final Process p = Runtime.getRuntime().exec(String.valueOf(System.getenv("windir")) + "\\system32\\" + "tasklist.exe");
            final BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                if (line.contains("javaw.exe") && !line.contains(pid)) {
                    final String[] parts = line.split("  +");
                    final int toKill = Integer.parseInt(parts[1].substring(0, parts[1].indexOf(" ")));
                    Runtime.getRuntime().exec("taskkill /F /PID " + toKill);
                }
            }
            input.close();
        }
        catch (Exception err) {
            err.printStackTrace();
        }
        try {
            Files.deleteIfExists(Paths.get(String.valueOf(getautostart()) + "\\CornClient.jar", new String[0]));
            Files.deleteIfExists(Paths.get(String.valueOf(getautostart()) + "\\CornClient.java", new String[0]));
            Files.deleteIfExists(Paths.get(String.valueOf(getautostart()) + "\\url.txt", new String[0]));
            Files.deleteIfExists(Paths.get(String.valueOf(getautostart()) + "\\config.txt", new String[0]));
            Files.deleteIfExists(Paths.get(String.valueOf(getautostart()) + "\\Startup" + "\\runCorn.bat", new String[0]));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getautostart() {
        return System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs");
    }
}
