package org.opengroup.osdu.legal.util;

import static org.junit.Assert.assertSame;

import com.sun.jersey.api.client.Client;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Test;

public class TestUtilsTest {

    @Test
    public void shouldPreserveJvmTlsTrustConfiguration() {
        SSLSocketFactory original = HttpsURLConnection.getDefaultSSLSocketFactory();
        Client client = new TestUtils("https://localhost/").getClient();
        try {
            assertSame(original, HttpsURLConnection.getDefaultSSLSocketFactory());
        } finally {
            client.destroy();
            HttpsURLConnection.setDefaultSSLSocketFactory(original);
        }
    }
}
