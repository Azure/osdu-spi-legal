package org.opengroup.osdu.legal.util;

import static org.junit.Assert.assertEquals;
import static org.opengroup.osdu.legal.util.Constants.DATA_PARTITION_ID;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;

public class TestUtils {

	protected String baseUrl;

	public TestUtils(){
		this(false);
	}

	public TestUtils(boolean enforceHttp){
		baseUrl = System.getProperty("HOST_URL", System.getenv("HOST_URL"));
		if(baseUrl == null || baseUrl.contains("-null")) {
			baseUrl = "https://localhost:8443/api/legal/v1/";
		}

		if(enforceHttp)
			baseUrl = baseUrl.replaceFirst("https", "http");
	}

	public TestUtils(String url){
		baseUrl = url;
	}

	public boolean isLocalHost(){
		return baseUrl.contains("//localhost");
	}

	public static String getMyProjectAccountId(){
		return System.getProperty("MY_TENANT_PROJECT", System.getenv("MY_TENANT_PROJECT"));
	}

	public String getBaseHost() {return baseUrl.substring(8,baseUrl.length()-1);}

	public String getApiPath(String api) throws Exception {
		URL mergedURL = new URL(baseUrl + api);
		return mergedURL.toString();
	}
	
	public static String getMyDataPartition(){
		return System.getProperty("MY_TENANT", System.getenv("MY_TENANT"));
	}

	public static String getMySecondDataPartition(){
		return System.getProperty("MY_SECOND_TENANT", System.getenv("MY_SECOND_TENANT"));
	}

	public ClientResponse send(String path, String httpMethod, String token, String requestBody, String query)
			throws Exception {

        Map<String, String> headers = getHeaders();

		return send(path, httpMethod, token, requestBody, query, headers);
	}

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        //either header should work the same for now so assign either to validate this
		headers.put(DATA_PARTITION_ID, getMyDataPartition());

        return headers;
    }

  public ClientResponse send(String path, String httpMethod, String token, String requestBody,
      String query, Map<String, String> headers) throws Exception {

    Client client = getClient();
    WebResource webResource = client.resource(getApiPath(path + query));

    final WebResource.Builder builder = webResource.getRequestBuilder();
    builder.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).
        header("Authorization", token);

    headers.forEach(builder::header);

    ClientResponse response = builder.method(httpMethod, ClientResponse.class, requestBody);

    return response;
  }

	@SuppressWarnings("unchecked")
	public <T> T getResult(ClientResponse response, int exepectedStatus, Class<T> classOfT) {
		String json = response.getEntity(String.class);
		System.out.println(json);

		assertEquals(exepectedStatus, response.getStatus());
		if (exepectedStatus == 204) {
			return null;
		}

		assertEquals("application/json", response.getType().toString());
		if (classOfT == String.class) {
			return (T) json;
		}

		Gson gson = new Gson();
		return gson.fromJson(json, classOfT);
	}

	public Client getClient() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

		return Client.create();
	}
}

