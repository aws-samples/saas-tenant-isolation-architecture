package tenant.vendinglayer.policy;

import com.amazon.aws.partners.saasfactory.policy.OpenScopedPolicyGenerator;
import com.amazon.aws.partners.saasfactory.policy.PolicyGenerator;
import jdk.jshell.spi.ExecutionControl;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilePolicyGenerator implements PolicyGenerator {

    private String tenant;
    private File templateDir;
    private Map<String, String> data;

    public FilePolicyGenerator(File templateDir, String tenant, Map<String, String> data) {
        this.tenant = tenant;
        this.data = data;
        this.templateDir = templateDir;
    }

    public String generatePolicy() {
        return null;
    }

    public PolicyGenerator tenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getTenant() {
        return tenant;
    }
}
