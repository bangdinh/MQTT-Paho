package paho.android.mqtt_example;

import android.content.Context;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SslUtility {
    private static SslUtility		mInstance = null;
    private Context mContext = null;
    private HashMap<Integer, SSLSocketFactory> mSocketFactoryMap = new HashMap<Integer, SSLSocketFactory>();

    public SslUtility(Context context) {
        mContext = context;
    }

    public static SslUtility getInstance( ) {
        if ( null == mInstance ) {
            throw new RuntimeException("first call must be to SslUtility.newInstance(Context) ");
        }
        return mInstance;
    }

    public static SslUtility newInstance( Context context ) {
        if ( null == mInstance ) {
            mInstance = new SslUtility( context );
        }
        return mInstance;
    }

    public SSLSocketFactory getSocketFactory(int certificateId, String certificatePassword ) {

        SSLSocketFactory result = mSocketFactoryMap.get(certificateId);  	// check to see if already created

        if ( ( null == result) && ( null != mContext ) ) {					// not cached so need to load server certificate

            try {
                KeyStore keystoreTrust = KeyStore.getInstance("BKS");		// Bouncy Castle

                keystoreTrust.load(mContext.getResources().openRawResource(certificateId),
                        certificatePassword.toCharArray());

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

                trustManagerFactory.init(keystoreTrust);

                SSLContext sslContext = SSLContext.getInstance("TLS");

                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

                result = sslContext.getSocketFactory();

                mSocketFactoryMap.put( certificateId, result);	// cache for reuse
            }
            catch ( Exception ex ) {
                // log exception
            }
        }

        return result;
    }

}
