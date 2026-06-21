package model;

public class TestSample {
    private String label;
    private long startTime;
    private long endTime;
    private long latency;
    private boolean successful;
    private String responseCode;
    private String responseMessage;
    private String requestHeaders;
    private String responseHeaders;
    private String requestBody;
    private String responseBody;
    private String threadName;
    private String groupName; // For Transaction Controller
    private String method;
    private String url;
    private String requestParams;
    private String failureType; // Distinguish between "API Failure" and "Assertion(s) Failure"
    private java.util.List<TestSample> children = new java.util.ArrayList<>();
    private java.util.List<AssertionData> assertions = new java.util.ArrayList<>();

    public static class AssertionData {
        private String name;
        private boolean failure;
        private String failureMessage;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public boolean isFailure() { return failure; }
        public void setFailure(boolean failure) { this.failure = failure; }
        public String getFailureMessage() { return failureMessage; }
        public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    }

    public TestSample() {
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(String requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public java.util.List<TestSample> getChildren() {
        return children;
    }

    public void addChild(TestSample child) {
        this.children.add(child);
    }

    public String getFailureType() {
        return failureType;
    }

    public void setFailureType(String failureType) {
        this.failureType = failureType;
    }

    public java.util.List<AssertionData> getAssertions() {
        return assertions;
    }

    public void addAssertion(AssertionData assertion) {
        this.assertions.add(assertion);
    }
}
