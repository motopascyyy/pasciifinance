package com.pasciitools.pasciifinance.common.configuration;

import com.sun.istack.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "td")
public class TDConfig {
    @NotNull private String webbrokerUrl;
    @NotNull private String easywebUrl;
    @NotNull private String easywebUserName;
    @NotNull private String easywebPassword;
    @NotNull private String webbrokerUserName;
    @NotNull private String webbrokerPassword;
    @NotNull private String usernameFieldOptions;

    @NotNull private String easywebSuccessLoginUrl;

    @NotNull private String webbrokerSuccessLoginUrl;

    @NotNull private String easywebNavigate;

    @NotNull private String webbrokerNavigate;

    public String getEasywebNavigate() {
        return easywebNavigate;
    }

    public void setEasywebNavigate(String easywebNavigate) {
        this.easywebNavigate = easywebNavigate;
    }

    public String getWebbrokerNavigate() {
        return webbrokerNavigate;
    }

    public void setWebbrokerNavigate(String webbrokerNavigate) {
        this.webbrokerNavigate = webbrokerNavigate;
    }

    public String getWebbrokerUrl() {
        return webbrokerUrl;
    }

    public void setWebbrokerUrl(String webbrokerUrl) {
        this.webbrokerUrl = webbrokerUrl;
    }

    public String getEasywebUrl() {
        return easywebUrl;
    }

    public void setEasywebUrl(String easywebUrl) {
        this.easywebUrl = easywebUrl;
    }

    public String getEasywebUserName() {
        return easywebUserName;
    }

    public void setEasywebUserName(String easywebUserName) {
        this.easywebUserName = easywebUserName;
    }

    public String getEasywebPassword() {
        return easywebPassword;
    }

    public void setEasywebPassword(String easywebPassword) {
        this.easywebPassword = easywebPassword;
    }

    public String getWebbrokerUserName() {
        return webbrokerUserName;
    }

    public void setWebbrokerUserName(String webbrokerUserName) {
        this.webbrokerUserName = webbrokerUserName;
    }

    public String getWebbrokerPassword() {
        return webbrokerPassword;
    }

    public void setWebbrokerPassword(String webbrokerPassword) {
        this.webbrokerPassword = webbrokerPassword;
    }

    public String getUsernameFieldOptions() {
        return usernameFieldOptions;
    }

    public void setUsernameFieldOptions(String usernameFieldOptions) {
        this.usernameFieldOptions = usernameFieldOptions;
    }

    public String getEasywebSuccessLoginUrl() {
        return easywebSuccessLoginUrl;
    }

    public void setEasywebSuccessLoginUrl(String easywebSuccessLoginUrl) {
        this.easywebSuccessLoginUrl = easywebSuccessLoginUrl;
    }

    public String getWebbrokerSuccessLoginUrl() {
        return webbrokerSuccessLoginUrl;
    }

    public void setWebbrokerSuccessLoginUrl(String webbrokerSuccessLoginUrl) {
        this.webbrokerSuccessLoginUrl = webbrokerSuccessLoginUrl;
    }
}
