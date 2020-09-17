package tenant.vendinglayer.policy;

import com.amazon.aws.partners.saasfactory.policy.PolicyGenerator;
import com.amazon.aws.partners.saasfactory.template.PolicyTemplateProcessor;
import tenant.vendinglayer.template.PolicyLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilePolicyGenerator implements PolicyGenerator {

    private final File templateDir;
    private final Map<String, String> data;

    public FilePolicyGenerator(File templateDir, Map<String, String> data) {
        this.data = data;
        this.templateDir = templateDir;
    }

    public String generatePolicy() {
        String statements = PolicyLoader.assemblePolicyTemplates(templateDir);

        PolicyTemplateProcessor policyTemplateProcessor =
            PolicyTemplateProcessor.builder()
                .data(data)
                .templates(statements)
                .build();

        return policyTemplateProcessor.getTenantScopedPolicyTemplate();
    }

    public PolicyGenerator tenant(String tenant) {
        data.put("tenant", tenant);
        return this;
    }

    public String getTenant() {
        return data.get("tenant");
    }
}
