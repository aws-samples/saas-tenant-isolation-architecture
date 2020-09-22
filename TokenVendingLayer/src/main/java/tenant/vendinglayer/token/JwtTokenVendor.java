package tenant.vendinglayer.token;

import com.amazon.aws.partners.saasfactory.cognito.JwtClaimsExtractor;
import com.amazon.aws.partners.saasfactory.exception.PolicyAssumptionException;
import com.amazon.aws.partners.saasfactory.policy.PolicyGenerator;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.AssumeRoleResponse;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.text.ParseException;
import java.util.Map;
import java.util.regex.Pattern;

public class JwtTokenVendor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenVendor.class);
    private static final String sharedSecret = "%9TdD7G6RjgTdm7K&!A16d%*ed4£DyKs";
    private static final String TENANT_CLAIM = "custom:tenant_id";
    private static final Pattern BEARER_TOKEN_REGEX = Pattern.compile("^[B|b]earer +");
    private final StsClient sts;
    private String tenant;
    private final String role;
    private final int durationSeconds;
    private final Map<String, String> headers;
    private final PolicyGenerator policyGenerator;

    public JwtTokenVendor(TokenVendorBuilder builder) {
        this.durationSeconds = builder.durationSeconds;
        this.policyGenerator = builder.policyGenerator;
        this.role = builder.role;
        this.headers = builder.headers;
        this.sts = StsClient.builder()
                .region(builder.region)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    public AwsCredentialsProvider vendToken() {
        try {
            String token = getBearerToken(this.headers);
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(sharedSecret);

            if(!signedJWT.verify(verifier)) {
                throw new JOSEException("Unable to verify JWT token");
            }

            tenant = (String) signedJWT.getJWTClaimsSet().getClaim(TENANT_CLAIM);
        } catch (JOSEException | ParseException e) {
            LOGGER.info("Error validating JWT", e);
            throw new PolicyAssumptionException("Unable to verify your identity.");
        }

        policyGenerator.tenant(tenant);
        String scopedPolicy = policyGenerator.generatePolicy();
        return getCredentialsForTenant(scopedPolicy, tenant);
    }

    public AwsCredentialsProvider getCredentialsForTenant(String scopedPolicy, String tenant) {
        if (scopedPolicy != null && !scopedPolicy.trim().isEmpty()) {
            try {
                AssumeRoleResponse assumeRoleResponse = sts.assumeRole((assumeRoleReq) -> {
                    assumeRoleReq.durationSeconds(durationSeconds).policy(scopedPolicy).roleArn(role).roleSessionName(tenant);
                });
                Credentials scopedCredentials = assumeRoleResponse.credentials();
                StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(AwsSessionCredentials.create(scopedCredentials.accessKeyId(), scopedCredentials.secretAccessKey(), scopedCredentials.sessionToken()));
                return credentialsProvider;
            } catch (SdkServiceException var6) {
                LOGGER.error("STS::AssumeRole", var6);
                throw new RuntimeException(var6);
            }
        } else {
            LOGGER.info("TokenVendor::Attempting to assumeRole with empty policy, should not happen!");
            throw new PolicyAssumptionException("Missing or empty policy, cannot allow access.");
        }
    }

    private String getBearerToken(Map<String, String> request) {
        String jwt = null;
        if (request != null) {
            String bearerToken = null;
            if (request.containsKey("Authorization")) {
                bearerToken = (String)request.get("Authorization");
            } else if (request.containsKey("authorization")) {
                bearerToken = (String)request.get("authorization");
            } else {
                LOGGER.error("Request does not contain an Authorization header");
            }

            if (bearerToken != null) {
                String[] token = BEARER_TOKEN_REGEX.split(bearerToken);
                if (token.length == 2 && !token[1].isEmpty()) {
                    jwt = token[1];
                } else {
                    LOGGER.error("Authorization header does not contain Bearer token");
                }
            } else {
                LOGGER.error("Request does not contain Authorization header");
            }
        }

        return jwt;
    }

    public String getTenant() {
        return this.tenant;
    }

    public static TokenVendorBuilder builder() {
        return new TokenVendorBuilder();
    }

    public static class TokenVendorBuilder {
        private String role;
        private Region region;
        private int durationSeconds;
        private PolicyGenerator policyGenerator;
        private Map<String, String> headers;

        public TokenVendorBuilder() {
        }

        public TokenVendorBuilder role(String role) {
            this.role = role;
            return this;
        }

        public TokenVendorBuilder region(Region region) {
            this.region = region;
            return this;
        }

        public TokenVendorBuilder durationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public TokenVendorBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public TokenVendorBuilder policyGenerator(PolicyGenerator policyGenerator) {
            this.policyGenerator = policyGenerator;
            return this;
        }

        public JwtTokenVendor build() {
            return new JwtTokenVendor(this);
        }
    }
}
