package nl.galesloot_ict.efjenergy.helpers;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;

import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created by FlorisJan on 4-12-2014.
 */
public class TrustAllSSLSimpleClientHttpRequestFactory extends SimpleClientHttpRequestFactory {

    private final HostnameVerifier verifier;

    public TrustAllSSLSimpleClientHttpRequestFactory(HostnameVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
        if (connection instanceof HttpsURLConnection) {

            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[] {};
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                }
            }
            };


            // Install the all-trusting trust manager
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                ((HttpsURLConnection) connection).setDefaultSSLSocketFactory(sc.getSocketFactory());

            } catch (Exception e) {
                e.printStackTrace();
            }



            ((HttpsURLConnection) connection).setHostnameVerifier(verifier);
        }
        super.prepareConnection(connection, httpMethod);
    }

}