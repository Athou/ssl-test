package test;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509ExtendedTrustManager;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.socket.PortFactory;

import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.PemUtils;

public class SslTest {

	@Test
	public void test() throws Exception {
		SSLFactory sslFactory = SSLFactory.builder().withTrustMaterial(buildTrustManager()).build();
		HttpsURLConnection.setDefaultSSLSocketFactory(sslFactory.getSslSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(sslFactory.getHostnameVerifier());

		ClientAndServer mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
		mockServer.when(new HttpRequest().withSecure(true).withMethod("GET"))
				.respond(new HttpResponse().withStatusCode(HttpStatusCode.OK_200.code()));

		URL url = new URL("https", "127.0.0.1", mockServer.getPort(), "");
		HttpURLConnection httpsConnection = (HttpURLConnection) url.openConnection();
		Assertions.assertEquals(HttpStatusCode.OK_200.code(), httpsConnection.getResponseCode());
	}

	private X509ExtendedTrustManager buildTrustManager() {
		InputStream certStream = getClass().getResourceAsStream("/org/mockserver/socket/CertificateAuthorityCertificate.pem");
		return PemUtils.loadTrustMaterial(certStream);
	}

}
