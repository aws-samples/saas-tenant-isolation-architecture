package tenant.export.errors;

public class GatewayError {

    private String errorType;
    private String httpStatus;
    private String requestId;
    private String message;

    public GatewayError(String errorType, String httpStatus, String requestId, String message) {
        this.errorType = errorType;
        this.httpStatus = httpStatus;
        this.requestId = requestId;
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
