package tenant.export.models;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@DynamoDbBean
public class TenantProduct {

    private String id;
    private String data;

    private static final String tableName = System.getenv("DB_TABLE");

    private DynamoDbTable<TenantProduct> TENANT_TABLE;

    public TenantProduct() {}

    public TenantProduct(AwsCredentialsProvider awsCredentialsProvider, String tenant) {
        DynamoDbClient ddb = DynamoDbClient.builder()
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .credentialsProvider(awsCredentialsProvider)
            .build();

        DynamoDbEnhancedClient DDB_ENHANCED_CLIENT =
            DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        this.id = tenant;
        this.TENANT_TABLE = DDB_ENHANCED_CLIENT.table(tableName, TableSchema.fromBean(TenantProduct.class));
    }

    public TenantProduct(AwsCredentialsProvider awsCredentialsProvider, String tenant, String data) {
        this.id = tenant;
        this.data = data;
        DynamoDbClient ddb = DynamoDbClient.builder()
            .httpClientBuilder(UrlConnectionHttpClient.builder())
            .credentialsProvider(awsCredentialsProvider)
            .build();

        DynamoDbEnhancedClient DDB_ENHANCED_CLIENT =
            DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
        this.TENANT_TABLE = DDB_ENHANCED_CLIENT.table(tableName, TableSchema.fromBean(TenantProduct.class));
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute(value = "tenant-id")
    public String getId() {
        return this.id;
    }

    public void setId(String tenant) {
        this.id = tenant;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public TenantProduct load(TenantProduct tenant) {
        return TENANT_TABLE.getItem(tenant);
    }

    public void save() {
        TENANT_TABLE.putItem(this);
    }
}
