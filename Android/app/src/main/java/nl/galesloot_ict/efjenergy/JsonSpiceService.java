package nl.galesloot_ict.efjenergy;

import android.app.Application;
import android.preference.PreferenceManager;

import com.octo.android.robospice.SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.json.jackson2.Jackson2ObjectPersisterFactory;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import javax.net.ssl.HostnameVerifier;

import nl.galesloot_ict.efjenergy.helpers.NullHostnameVerifier;
import nl.galesloot_ict.efjenergy.helpers.TrustAllSSLSimpleClientHttpRequestFactory;

/**
 * Created by FlorisJan on 17-11-2014.
 */
public class JsonSpiceService extends SpringAndroidSpiceService {
    @Override
    public CacheManager createCacheManager( Application application ) {
        CacheManager cacheManager = new CacheManager();
        Jackson2ObjectPersisterFactory jackson2ObjectPersisterFactory = null;
        try {
            jackson2ObjectPersisterFactory = new Jackson2ObjectPersisterFactory( application );
        } catch (CacheCreationException e) {
            e.printStackTrace();
        }
        cacheManager.addPersister( jackson2ObjectPersisterFactory );
        return cacheManager;
    }

    @Override
    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        Boolean trustAllSSL = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_key_api_ssl_trustall",false);
        if ( trustAllSSL ) {
            restTemplate = setRestTemplateTrustAllSSL(restTemplate);
        }

        //find more complete examples in RoboSpice Motivation app
        //to enable Gzip compression and setting request timeouts.

        // web services support json responses
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        final List<HttpMessageConverter< ? >> listHttpMessageConverters = restTemplate.getMessageConverters();

        listHttpMessageConverters.add( jsonConverter );
        listHttpMessageConverters.add( formHttpMessageConverter );
        listHttpMessageConverters.add( stringHttpMessageConverter );
        restTemplate.setMessageConverters( listHttpMessageConverters );
        return restTemplate;
    }

    public RestTemplate setRestTemplateTrustAllSSL(RestTemplate template) {
        HostnameVerifier verifier = new NullHostnameVerifier();
        TrustAllSSLSimpleClientHttpRequestFactory factory = new TrustAllSSLSimpleClientHttpRequestFactory(verifier);
        template.setRequestFactory(factory);
        return template;
    }


}
