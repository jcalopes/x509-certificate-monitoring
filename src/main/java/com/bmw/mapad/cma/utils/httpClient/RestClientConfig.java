package com.bmw.mapad.cma.utils.httpClient;

import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Configuration file to specify different http rest clients.
 */
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    @Value("${cma.notifier.jira.baseUrl:https://atc.bmwgroup.net/jira/rest/api/2/}")
    String jiraBaseUrl;
    @Value("${cma.notifier.jira.token:token}")
    String jiraToken;

    @Value("${cma.notifier.confluence.baseUrl:https://atc.bmwgroup.net/confluence/rest/api/}")
    String confluenceBaseUrl;
    @Value("${cma.notifier.confluence.token:token}")
    String confluenceToken;

    /**
     * Return a Retrofit instance providing a http client. This client is based on API interface defined elsewhere so
     * will offer a couple of pre-defined endpoints defined there.
     * @return An implementation of Retrofit interface.
     */
    @Bean("jiraClient")
    public Retrofit jiraClient(){
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient
                .Builder()
                .addInterceptor((chain)->{
                    Request request = chain.request();
                    Request authenticatedRequest = request.newBuilder()
                            .header("Authorization", "Bearer " + jiraToken)
                            .build();
                    return chain.proceed(authenticatedRequest);
                })
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES);

        return new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .baseUrl(jiraBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Return a Retrofit instance providing a http client. This client is based on API interface defined elsewhere so
     * will offer a couple of pre-defined endpoints defined there.
     * @return An implementation of Retrofit interface.
     */
    @Bean("confluenceClient")
    public ConfluenceAPI confluenceClient(){
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient
                .Builder()
                .addInterceptor((chain)->{
                    Request authenticatedRequest = chain.request().newBuilder()
                            .header("Authorization", "Bearer " + confluenceToken)
                            .header("X-Atlassian-Token","nocheck")
                            .build();
                    return chain.proceed(authenticatedRequest);
                })
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES);

        return new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .baseUrl(confluenceBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ConfluenceAPI.class);
    }
}
