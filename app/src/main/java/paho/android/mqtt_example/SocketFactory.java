package paho.android.mqtt_example;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.cert.CertificateException;

/**
 * Original SocketFactory file taken from https://github.com/owntracks/android
 */

public class SocketFactory extends javax.net.ssl.SSLSocketFactory {
    private javax.net.ssl.SSLSocketFactory factory;

    public static class SocketFactoryOptions {

        private InputStream caCrtInputStream;
        private InputStream caClientP12InputStream;
        private String caClientP12Password;

        public SocketFactoryOptions withCaInputStream(InputStream stream) {
            this.caCrtInputStream = stream;
            return this;
        }

        public SocketFactoryOptions withClientP12InputStream(InputStream stream) {
            this.caClientP12InputStream = stream;
            return this;
        }

        public SocketFactoryOptions withClientP12Password(String password) {
            this.caClientP12Password = password;
            return this;
        }

        public boolean hasCaCrt() {
            return caCrtInputStream != null;
        }

        public boolean hasClientP12Crt() {
            return caClientP12Password != null;
        }

        public InputStream getCaCrtInputStream() {
            return caCrtInputStream;
        }

        public InputStream getCaClientP12InputStream() {
            return caClientP12InputStream;
        }

        public String getCaClientP12Password() {
            return caClientP12Password;
        }

        public boolean hasClientP12Password() {
            return (caClientP12Password != null) && !caClientP12Password.equals("");
        }
    }

    public SocketFactory() throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, KeyManagementException, java.security.cert.CertificateException, UnrecoverableKeyException {
        this(new SocketFactoryOptions());
    }


    private TrustManagerFactory tmf;

    public SocketFactory(SocketFactoryOptions options)
            throws KeyStoreException, NoSuchAlgorithmException, IOException,
            KeyManagementException, java.security.cert.CertificateException, UnrecoverableKeyException {
        Log.v(this.toString(), "initializing CustomSocketFactory");

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
// From https://www.washington.edu/itconnect/security/ca/load-der.crt

        Log.e("INPUT",options.caCrtInputStream.available()+"");
        InputStream caInput = new BufferedInputStream(options.caCrtInputStream);
        Certificate ca = cf.generateCertificate(caInput);


// Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

// Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

// Create an SSLContext that uses our TrustManager

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLSv1.2");//TLSv1.2,tlsv1
//        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

//        context.init(kmf.getKeyManagers(), getTrustManagers(), null);
        this.factory = context.getSocketFactory();

    }

    public TrustManager[] getTrustManagers() {
        return tmf.getTrustManagers();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.factory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.factory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        SSLSocket r = (SSLSocket) this.factory.createSocket();
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocket r = (SSLSocket) this.factory.createSocket(s, host, port, autoClose);
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {

        SSLSocket r = (SSLSocket) this.factory.createSocket(host, port);
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        SSLSocket r = (SSLSocket) this.factory.createSocket(host, port, localHost, localPort);
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket r = (SSLSocket) this.factory.createSocket(host, port);
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocket r = (SSLSocket) this.factory.createSocket(address, port, localAddress, localPort);
        r.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
        return r;
    }
}
