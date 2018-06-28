/**
 * 
 */
package com.emc.metalnx.services.irods.mail;

/**
 * Value object holding configured mail service properties from metalnx.config
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class MailProperties {

	private String host = "";
	private int port = 22;
	private String username = "";
	private String password = "";
	private String from = "";
	private String to = "";
	private String cc = "";
	private boolean startTlsEnable = false;
	private boolean smtpAuth = false;
	private String mailTransportProtocol = "";
	private String defaultEncoding = "UTF-8";
	private boolean debug = false;
	private boolean enabled = false;

	public String getHost() {
		System.out.println("Host :: " +host);
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		System.out.println("Port :: " +port);
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isStartTlsEnable() {
		return startTlsEnable;
	}

	public void setStartTlsEnable(boolean startTlsEnable) {
		this.startTlsEnable = startTlsEnable;
	}

	public boolean isSmtpAuth() {
		return smtpAuth;
	}

	public void setSmtpAuth(boolean smtpAuth) {
		this.smtpAuth = smtpAuth;
	}

	public String getMailTransportProtocol() {
		return mailTransportProtocol;
	}

	public void setMailTransportProtocol(String mailTransportProtocol) {
		this.mailTransportProtocol = mailTransportProtocol;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MailProperties [");
		if (host != null) {
			builder.append("host=").append(host).append(", ");
		}
		builder.append("port=").append(port).append(", ");
		if (username != null) {
			builder.append("username=").append(username).append(", ");
		}
		if (from != null) {
			builder.append("from=").append(from).append(", ");
		}
		if (to != null) {
			builder.append("to=").append(to).append(", ");
		}
		if (cc != null) {
			builder.append("cc=").append(cc).append(", ");
		}
		builder.append("startTlsEnable=").append(startTlsEnable).append(", smtpAuth=").append(smtpAuth).append(", ");
		if (mailTransportProtocol != null) {
			builder.append("mailTransportProtocol=").append(mailTransportProtocol).append(", ");
		}
		if (defaultEncoding != null) {
			builder.append("defaultEncoding=").append(defaultEncoding).append(", ");
		}
		builder.append("debug=").append(debug).append(", enabled=").append(enabled).append("]");
		return builder.toString();
	}

}
