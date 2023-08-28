package se.sundsvall.emailreader.integration.ews;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.ews")
public record EWSProperties(String username, String password, String domain, String url) {

}
