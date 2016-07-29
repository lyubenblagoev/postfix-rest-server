package com.lyubenblagoev.postfixrest.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "mail-server")
public class MailServerConfiguration {

	private String vhostsPath;
	
	public String getVhostsPath() {
		return vhostsPath;
	}
	
	public void setVhostsPath(String vhostsPath) {
		this.vhostsPath = vhostsPath;
	}

}
