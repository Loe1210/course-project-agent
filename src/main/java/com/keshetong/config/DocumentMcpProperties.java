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
    private String bridgeScriptPath;
    private String outputPath;
    private long timeoutMs = 120000;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
    }

    public void setBridgeScriptPath(String bridgeScriptPath) {
        this.bridgeScriptPath = bridgeScriptPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
}
