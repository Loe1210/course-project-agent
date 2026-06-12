package com.keshetong.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties(prefix = "document.mcp")
public class DocumentMcpProperties {

    private boolean enabled;
    private String pythonPath;
    private String serverScriptPath;
    private String transport = "sse";
    private String host = "127.0.0.1";
    private int port = 18080;
    private String sseEndpoint = "/sse";
    private String logLevel = "INFO";
    private String outputPath;
    private long timeoutMs = 120000;
    private long startupTimeoutMs = 30000;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    public void setServerScriptPath(String serverScriptPath) {
        this.serverScriptPath = serverScriptPath;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSseEndpoint(String sseEndpoint) {
        this.sseEndpoint = sseEndpoint;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public void setStartupTimeoutMs(long startupTimeoutMs) {
        this.startupTimeoutMs = startupTimeoutMs;
    }
}
