
/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package tenant.vendinglayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import tenant.vendinglayer.token.JwtTokenVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.lingala.zip4j.ZipFile;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import tenant.vendinglayer.policy.FilePolicyGenerator;

public class TokenVendor {

    private static final Logger logger = LoggerFactory.getLogger(TokenVendor.class);

    private String tenant;

    private static final String tmp = "/tmp";
    private static final Path templateFilePath = Paths.get(tmp + "/templates.zip");

    private static final String ROLE = System.getenv("ROLE");
    private static final Region AWS_REGION = Region.of(System.getenv("AWS_REGION"));
    private static final String DB_TABLE = System.getenv("DB_TABLE");

    private static final String TEMPLATE_BUCKET = System.getenv("TEMPLATE_BUCKET");
    private static final String TEMPLATE_KEY = System.getenv("TEMPLATE_KEY");
    private static final String templateDirPath = tmp + "/" + TEMPLATE_KEY.split("/")[0];
    private final File templateDir;

    public TokenVendor() {
        // we only download the policies from S3 if the templates don't exist on the
        // filesystem already from a previous lambda invocation
        if(Files.notExists(templateFilePath)) {
            logger.info("Templates zip file not found, downloading from S3...");
            S3Client s3 = S3Client.builder().httpClientBuilder(UrlConnectionHttpClient.builder()).build();
            s3.getObject(GetObjectRequest.builder().bucket(TEMPLATE_BUCKET).key(TEMPLATE_KEY).build(),
                ResponseTransformer.toFile(templateFilePath));
            try {
                ZipFile zipFile = new ZipFile(templateFilePath.toFile());
                zipFile.extractAll(templateDirPath);
                logger.info("Templates zip file successfully unzipped.");
            } catch (IOException e) {
                logger.error("Could not unzip template file.", e);
                throw new RuntimeException(e.getMessage());
            }
        }
        this.templateDir = new File(templateDirPath);
    }

    /**
     * Does not validate the JWT token with an IdP
     * extracts the tenant from the JWT claims
     * and uses the tenant to vend the token.
     *
     * @param headers the HTTP headers which contain an authorization header.
     * @return the scoped credentials
     */
    public AwsCredentialsProvider vendTokenJwt(Map<String, String> headers) {
        Map<String, String> policyData = new HashMap<>();
        policyData.put("table", DB_TABLE);

        FilePolicyGenerator policyGenerator = new FilePolicyGenerator(templateDir, policyData);

        JwtTokenVendor jwtTokenVendor = JwtTokenVendor.builder()
            .policyGenerator(policyGenerator)
            .durationSeconds(900)
            .headers(headers)
            .role(ROLE)
            .region(AWS_REGION)
            .build();

        AwsCredentialsProvider awsCredentialsProvider = jwtTokenVendor.vendToken();
        tenant = jwtTokenVendor.getTenant();

        logger.info("Vending JWT security token for tenant {}", tenant);

        return awsCredentialsProvider;
    }

    public String getTenant() {
        return tenant;
    }

}
