package com.bmw.mapad.cma.certificateextractor.acm;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.certificatemanager.AWSCertificateManager;
import com.amazonaws.services.certificatemanager.AWSCertificateManagerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to specify different AWS clients to interact with its several services.
 */
@Configuration
public class AwsClientConfig {
    @Value("${cma.extractor.aws.region:region}")
    String region;
    @Value(("${cma.extractor.aws.profile:default}"))
    String profile;
    @Value(("${cma.extractor.aws.accesskey:key}"))
    String accessKeyId;
    @Value(("${cma.extractor.aws.secretkey:secret}"))
    String secretAccessKey;

    /**
     * Return a client to interact with certificate manager service provided by AWS SDK.
     * @return ACM client.
     */
    @Bean
    public AWSCertificateManager AcmClient() {
        //ProfileCredentialsProvider profilesCredentials = new ProfileCredentialsProvider(profile);
        AWSStaticCredentialsProvider awsCredentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
        return AWSCertificateManagerClientBuilder.standard()
                .withRegion(Regions.valueOf(region))
                .withCredentials(awsCredentials)
                .build();
    }
}