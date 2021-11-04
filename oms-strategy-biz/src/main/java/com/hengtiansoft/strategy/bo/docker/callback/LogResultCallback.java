package com.hengtiansoft.strategy.bo.docker.callback;

import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.model.Frame;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class LogResultCallback extends ResultCallbackTemplate<LogResultCallback, Frame> {

    private OutputStream out = new ByteArrayOutputStream();
    private OutputStream err = new ByteArrayOutputStream();

    public String getResult() {
        return out.toString();
    }

    public String getError() {
        return err.toString();
    }

    @Override
    public void onNext(Frame frame) {
        if (frame != null) {
            try {
                switch (frame.getStreamType()) {
                    case STDOUT:
                    case RAW:
                        if (out != null) {
                            out.write(frame.getPayload());
                            out.flush();
                        }
                        break;
                    case STDERR:
                        if (err != null) {
                            err.write(frame.getPayload());
                            err.flush();
                        }
                        break;
                    default:
                        log.error("unknown stream type:" + frame.getStreamType());
                }
            } catch (IOException e) {
                onError(e);
            }

            log.debug(frame.toString());
        }
    }
}
