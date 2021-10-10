import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Unit test for simple App.
 */
public class AppTest 
{

    @Test
    public void python()
    {
        System.out.println(System.getProperty("user.dir"));
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("python Z:/workplace/oms/test1.py");
            //proc = Runtime.getRuntime().exec("python Z:/workplace/oms/StrategyPython/handleTick.py 0 sssssss");
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
            int i = proc.exitValue();
            // 0成功
            System.out.println(i);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
