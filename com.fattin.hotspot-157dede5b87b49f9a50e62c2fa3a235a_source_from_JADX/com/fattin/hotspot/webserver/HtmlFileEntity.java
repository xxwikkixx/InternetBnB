package com.fattin.hotspot.webserver;

import com.fattin.hotspot.helpers.Util;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.http.entity.AbstractHttpEntity;

public class HtmlFileEntity extends AbstractHttpEntity {
    private String content;
    private InputStream inputStream;

    public HtmlFileEntity(InputStream inputStream) {
        setContentType("text/html; charset=UTF-8");
        this.inputStream = inputStream;
        this.content = Util.convertStreamToString(this.inputStream);
    }

    public InputStream getContent() throws IOException, IllegalStateException {
        return this.inputStream;
    }

    public long getContentLength() {
        return (long) this.content.length();
    }

    public boolean isRepeatable() {
        return false;
    }

    public boolean isStreaming() {
        return false;
    }

    public void writeTo(OutputStream outstream) throws IOException {
        if (outstream == null) {
            throw new IllegalArgumentException("Output stream may not be null");
        }
        OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
        writer.write(this.content);
        writer.flush();
    }
}
