import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        File file = new File("StrategyPython/strategies/s2");
        if(file!=null&&file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException ex) {
                //todo:日志
                ex.printStackTrace();
            }
        }
    }
}
