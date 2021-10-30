package com.hengtiansoft.strategy.blind;

import com.hengtiansoft.eventbus.SubscribeEvent;
import com.hengtiansoft.strategy.model.StrategyModel;
import com.hengtiansoft.strategy.bo.strategy.BaseStrategy;
import com.hengtiansoft.strategy.bo.event.TickEvent;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class RunningStrategys extends BaseStrategy {

    private String id;
    private StrategyModel strategy;

    public RunningStrategys(StrategyModel strategy)
    {
        this.id = UUID.randomUUID().toString();
        this.strategy = strategy;
    }

    public boolean init()
    {
        File file = null;
        BufferedWriter bufferedWriter = null;
        try{
            file = new File("StrategyPython/strategies/"+this.id);
            if(!file.exists()) {
                file.mkdirs();
            }
            bufferedWriter = new BufferedWriter(new FileWriter(new File(file,"bo.py")));
            bufferedWriter.write(strategy.getCode());
            bufferedWriter.close();
            return true;
        }
        catch(IOException e) {

            if(file!=null&&file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException ex) {
                    //todo:日志
                    ex.printStackTrace();
                }
            }
            //todo:日志
            e.printStackTrace();

        }
        finally {
            if(bufferedWriter!=null) {
                try {
                    bufferedWriter.close();
                } catch (IOException ex) {
                    //todo:日志
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    //@SubscribeEvent
    //@DisallowConcurrentEvents
    public void HandleTickSynchronized(TickEvent tickEvent) throws Exception
    {
    }

    final public void subscribe(String security)
    {
        addEventListened(TickEvent.class,security);
    }

    @SubscribeEvent
    public void HandleTick(TickEvent tickEvent)
    {
        Process process;
        try {
            String pwd = System.getProperty("user.dir");
            process = Runtime.getRuntime().exec(pwd+"/StrategyPython/StrategyEnv/Scripts/python.exe "+ pwd +"/StrategyPython/handleTick.py 0 sssssss");
            //process = Runtime.getRuntime().exec(pwd+"\\StrategyPython\\StrategyEnv\\Scripts\\python.exe "+ pwd +"\\StrategyPython\\test1.py 0 sssssss");

            SequenceInputStream sis = new SequenceInputStream (process.getInputStream (), process.getErrorStream ());
            InputStreamReader isr = new InputStreamReader (sis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader (isr);

            OutputStreamWriter osw = new OutputStreamWriter (process.getOutputStream ());
            BufferedWriter bw = new BufferedWriter (osw);

            String line = null;

            while (null != ( line = br.readLine () ))
            {
                System.out.println (line);
            }

            process.waitFor();
            process.destroy();
            br.close ();
            isr.close ();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
