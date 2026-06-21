package listener;

import generator.HtmlReportGenerator;
import model.TestSample;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.assertions.AssertionResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;

public class CustomHtmlReportListener extends AbstractListenerElement
        implements SampleListener, TestStateListener, Serializable, NoThreadClone {

    private static final long serialVersionUID = 1L;
    public static final String REPORT_PATH = "CustomHtmlReportListener.report_path";
    public static final String PASS_THRESHOLD = "CustomHtmlReportListener.pass_threshold";
    private static final List<TestSample> samples = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sampleOccurred(SampleEvent e) {
        SampleResult r = e.getResult();

        TestSample s = convertToTestSample(r);

        if (r.getSubResults() != null && r.getSubResults().length > 0) {
            s.setGroupName(r.getSampleLabel()); // It is a transaction
            for (SampleResult sub : r.getSubResults()) {
                s.addChild(convertToTestSample(sub));
            }
        }

        samples.add(s);
    }

    private TestSample convertToTestSample(SampleResult r) {
        TestSample s = new TestSample();
        s.setLabel(r.getSampleLabel());
        s.setUrl(r.getUrlAsString());
        s.setStartTime(r.getStartTime());
        s.setEndTime(r.getEndTime());
        s.setLatency(r.getTime());
        s.setSuccessful(r.isSuccessful());
        s.setResponseCode(r.getResponseCode());
        s.setResponseMessage(r.getResponseMessage());
        s.setRequestHeaders(r.getRequestHeaders());
        s.setResponseHeaders(r.getResponseHeaders());
        s.setRequestBody(r.getSamplerData());
        s.setResponseBody(r.getResponseDataAsString());
        s.setThreadName(r.getThreadName());

        String samplerData = r.getSamplerData();
        if (samplerData != null) {
            if (samplerData.startsWith("GET"))
                s.setMethod("GET");
            else if (samplerData.startsWith("POST"))
                s.setMethod("POST");
            else if (samplerData.startsWith("PUT"))
                s.setMethod("PUT");
            else if (samplerData.startsWith("DELETE"))
                s.setMethod("DELETE");
            else if (samplerData.startsWith("PATCH"))
                s.setMethod("PATCH");
            else
                s.setMethod("REQ");
        } else {
            s.setMethod("REQ");
        }

        AssertionResult[] assertionResults = r.getAssertionResults();
        boolean hasAssertionFailures = false;
        if (assertionResults != null) {
            for (AssertionResult ar : assertionResults) {
                TestSample.AssertionData ad = new TestSample.AssertionData();
                ad.setName(ar.getName());
                ad.setFailure(ar.isFailure() || ar.isError());
                ad.setFailureMessage(ar.getFailureMessage());
                s.addAssertion(ad);
                if (ad.isFailure()) {
                    hasAssertionFailures = true;
                }
            }
        }
        
        if (!r.isSuccessful()) {
            boolean isApiFailure = false;
            try {
                int code = Integer.parseInt(r.getResponseCode());
                if (code >= 400 || !r.isResponseCodeOK()) {
                    isApiFailure = true;
                }
            } catch (NumberFormatException nfe) {
                isApiFailure = true;
            }
            
            if (isApiFailure) {
                s.setFailureType("API Failure");
            } else if (hasAssertionFailures) {
                s.setFailureType("Assertion(s) Failure");
            } else {
                s.setFailureType("API Failure");
            }
        } else {
            if (hasAssertionFailures) {
                s.setSuccessful(false);
                s.setFailureType("Assertion(s) Failure");
            } else {
                s.setFailureType("Pass");
            }
        }

        return s;
    }

    @Override
    public void sampleStarted(SampleEvent e) {
    }

    @Override
    public void sampleStopped(SampleEvent e) {
    }

    @Override
    public void testStarted() {
        samples.clear();
    }

    @Override
    public void testStarted(String host) {
        testStarted();
    }

    @Override
    public void testEnded() {
        // Generate Report
        String reportPath = getPropertyAsString(REPORT_PATH);
        if (reportPath == null || reportPath.trim().isEmpty() || !new java.io.File(reportPath).exists() || !new java.io.File(reportPath).isDirectory()) {
            reportPath = FileServer.getFileServer().getBaseDir();
            if (reportPath == null || reportPath.isEmpty()) {
                reportPath = System.getProperty("user.dir");
            }
        }

        String thresholdStr = getPropertyAsString(PASS_THRESHOLD);
        double passThreshold = 90.0;
        if (thresholdStr != null && !thresholdStr.trim().isEmpty()) {
            try {
                passThreshold = Double.parseDouble(thresholdStr);
                if (passThreshold <= 0 || passThreshold > 100) {
                    passThreshold = 90.0;
                }
            } catch (NumberFormatException e) {
                // Ignore, keep default
            }
        }

        String scriptName = FileServer.getFileServer().getScriptName();
        if (scriptName == null || scriptName.isEmpty()) {
            scriptName = "Test_Report";
        } else {
            // Remove extension
            if (scriptName.endsWith(".jmx")) {
                scriptName = scriptName.substring(0, scriptName.length() - 4);
            }
        }

        // Capture completion time
        Date now = new Date();

        HtmlReportGenerator.generateReport(new ArrayList<>(samples), reportPath, scriptName, now, passThreshold);
    }

    @Override
    public void testEnded(String host) {
        testEnded();
    }
}
