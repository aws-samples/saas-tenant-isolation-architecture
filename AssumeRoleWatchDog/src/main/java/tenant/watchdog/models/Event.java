package tenant.watchdog.models;

public class Event {

    private Detail detail;

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public class Detail {
        private String eventName;
        private RequestParameters requestParameters;

        public RequestParameters getRequestParameters() {
            return requestParameters;
        }

        public void setRequestParameters(RequestParameters requestParameters) {
            this.requestParameters = requestParameters;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }

    public class RequestParameters {

        private String policy;
        private String roleArn;

        public String getRoleArn() {
            return roleArn;
        }

        public void setRoleArn(String roleArn) {
            this.roleArn = roleArn;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }
    }

}
