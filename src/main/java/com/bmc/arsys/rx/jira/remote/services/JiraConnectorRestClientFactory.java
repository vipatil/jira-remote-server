package com.bmc.arsys.rx.jira.remote.services;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.bmc.arsys.rx.jira.remote.atlassian.AsynchronousJiraRestClientFactory;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class JiraConnectorRestClientFactory {

    private static final Supplier<JiraConnectorRestClientFactory> SINGLETON_SUPPLIER = Suppliers.memoize(JiraConnectorRestClientFactory::new);

    private SSLContext sslContext;

    private JiraConnectorRestClientFactory() {
        init();
    }

    public static JiraConnectorRestClientFactory instance() {
        return SINGLETON_SUPPLIER.get();
    }

    public JiraRestClient create(JiraConnection connection) {
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory(
                sslContext);
        URI jiraServerUri = URI.create(connection.url);
        return factory.createWithBasicHttpAuthentication(jiraServerUri, connection.login,
                connection.password);
    }

    private void init() {
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

        // Install the all-trusting trust manager
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
