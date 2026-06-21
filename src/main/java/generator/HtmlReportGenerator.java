package generator;

import model.TestSample;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class HtmlReportGenerator {

    public static void generateReport(List<TestSample> samples, String outputDir, String scriptName,
            Date testDate, double passThreshold) {
        try {
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Filename Format: Smoke_htmlReport_2025_12_20_17_05
            SimpleDateFormat fileDateFmt = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
            String datePartFile = fileDateFmt.format(testDate);
            String fileNameBase = scriptName + "_htmlReport_" + datePartFile;

            String fileName = fileNameBase + ".html";
            File reportFile = new File(dir, fileName);
            int index = 1;
            while (reportFile.exists()) {
                reportFile = new File(dir, fileNameBase + "_" + index + ".html");
                index++;
            }

            // Title Date Format: 2025-12-20 17:05:30 +0530
            SimpleDateFormat titleDateFmt = new SimpleDateFormat("yyyy-12-dd HH:mm:ss z"); // Match original yyyy-MM-dd HH:mm:ss z
            titleDateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            String titleDate = titleDateFmt.format(testDate);

            List<TestSample> metricSamples = getCalculableSamples(samples);

            System.out.println("HtmlReportGenerator: Total Samples: " + samples.size());
            System.out.println("HtmlReportGenerator: Metric Samples: " + metricSamples.size());

            long total = metricSamples.size();
            long passed = metricSamples.stream().filter(TestSample::isSuccessful).count();
            long failed = total - passed;
            double passRate = total > 0 ? (double) passed / total * 100 : 0;
            long duration = metricSamples.stream().mapToLong(TestSample::getLatency).sum();
            double avgTime = total > 0 ? (double) duration / total : 0;

            Map<String, Map<String, Long>> statusBreakdown = metricSamples.stream()
                    .collect(Collectors.groupingBy(
                            s -> (s.getResponseCode() != null
                                    && !s.getResponseCode().trim().isEmpty())
                                            ? s.getResponseCode()
                                            : "Unknown",
                            Collectors.groupingBy(
                                    s -> s.isSuccessful() ? "Pass" : "Fail",
                                    Collectors.counting())));

            Set<String> failTypes = new HashSet<>();
            Set<String> errorCodes = new HashSet<>();
            for (TestSample s : metricSamples) {
                if (!s.isSuccessful()) {
                    if (s.getFailureType() != null && !s.getFailureType().isEmpty() && !s.getFailureType().equals("Pass")) {
                        failTypes.add(s.getFailureType());
                    }
                    if (s.getResponseCode() != null && !s.getResponseCode().trim().isEmpty()) {
                        errorCodes.add(s.getResponseCode());
                    }
                }
            }
            boolean showRadio = failTypes.contains("API Failure") && failTypes.contains("Assertion(s) Failure");
            boolean showDropdown = errorCodes.size() > 1;

            // Generate status code rows
            StringBuilder statusRows = new StringBuilder();
            List<String> sortedCodes = new ArrayList<>(statusBreakdown.keySet());
            Collections.sort(sortedCodes);
            for (String code : sortedCodes) {
                Map<String, Long> counts = statusBreakdown.get(code);
                long pCnt = counts.getOrDefault("Pass", 0L);
                long fCnt = counts.getOrDefault("Fail", 0L);
                long tCnt = pCnt + fCnt;
                statusRows.append("<div class='status-item'><span>").append(code)
                        .append("</span><span class='text-pass' style='font-weight:600;'>").append(pCnt)
                        .append("</span><span class='text-fail' style='font-weight:600;'>").append(fCnt)
                        .append("</span><span style='font-weight:700;'>").append(tCnt)
                        .append("</span></div>");
            }

            // Generate fail filters
            StringBuilder failFilters = new StringBuilder();
            if ((showRadio || showDropdown) && failed > 0) {
                failFilters.append("<div id='fail-filters' style='margin-bottom:15px; padding:15px; background:var(--bg-summary); border:1px solid var(--border-summary); border-radius:8px; display:flex; gap:20px; align-items:center; flex-wrap:wrap;'>");
                failFilters.append("<span style='font-weight:600; color:var(--text-main);'>Filters:</span>");
                if (showRadio) {
                    failFilters.append("<div style='display:flex; gap:10px; align-items:center;'>");
                    failFilters.append("<label><input type='radio' name='failType' value='All' onclick='let c=document.getElementById(\"failCodeSelect\");if(c)c.value=\"All\";applyFiltersWithLoader();' checked> All Failures</label>");
                    failFilters.append("<label><input type='radio' name='failType' value='API Failure' onchange='applyFiltersWithLoader()'> API Failure</label>");
                    failFilters.append("<label><input type='radio' name='failType' value='Assertion(s) Failure' onchange='applyFiltersWithLoader()'> Assertion Failure</label>");
                    failFilters.append("</div>");
                }
                if (showDropdown) {
                    failFilters.append("<div id='failCodeSelectContainer' style='display:flex; align-items:center; gap:8px;'>");
                    failFilters.append("<select id='failCodeSelect' onchange='applyFiltersWithLoader()' style='padding:5px; border-radius:4px; border:1px solid var(--border-divider); background:var(--bg-card); color:var(--text-body);'>");
                    failFilters.append("<option value='All'>All Error Codes</option>");
                    List<String> sortedErrCodes = new ArrayList<>(errorCodes);
                    Collections.sort(sortedErrCodes);
                    for (String code : sortedErrCodes) {
                        failFilters.append("<option value='").append(code).append("'>").append(code).append("</option>");
                    }
                    failFilters.append("</select></div>");
                }
                failFilters.append("</div>");
            }

            boolean isPass = passRate >= passThreshold;
            String execStatus = isPass ? "PASSED" : "FAILED";
            String statusColor = isPass ? "var(--text-pass)" : "var(--text-fail)";

            // Load resources from classpath
            String template = loadResource("/templates/report.html");
            String css = loadResource("/static/style.css");
            String js = loadResource("/static/script.js");

            // Perform replacements (use replace for literal matches, avoiding replaceAll regex issues)
            String finalHtml = template
                    .replace("{{CSS}}", css)
                    .replace("{{JS}}", js)
                    .replace("{{SCRIPT_NAME}}", scriptName)
                    .replace("{{TITLE_DATE}}", titleDate)
                    .replace("{{EXEC_STATUS}}", execStatus)
                    .replace("{{STATUS_COLOR}}", statusColor)
                    .replace("{{PASS_RATE_SHORT}}", String.format("%.1f", passRate))
                    .replace("{{PASS_RATE_FULL}}", String.format("%.2f", passRate))
                    .replace("{{PASS_RATE_JS}}", String.valueOf(passRate))
                    .replace("{{TOTAL_REQUESTS}}", String.valueOf(total))
                    .replace("{{PASSED_REQUESTS}}", String.valueOf(passed))
                    .replace("{{FAILED_REQUESTS}}", String.valueOf(failed))
                    .replace("{{TOTAL_FAILED}}", String.valueOf(failed))
                    .replace("{{AVG_TIME}}", String.format("%.2f", avgTime))
                    .replace("{{TOTAL_DURATION}}", String.valueOf(duration))
                    .replace("{{STATUS_ROWS}}", statusRows.toString())
                    .replace("{{FAIL_FILTERS}}", failFilters.toString())
                    .replace("{{ALL_REQUESTS_LIST}}", generateListHtml(samples, "all"))
                    .replace("{{PASSED_REQUESTS_LIST}}", generateListHtml(samples, "pass"))
                    .replace("{{FAILED_REQUESTS_LIST}}", generateListHtml(samples, "fail"))
                    .replace("{{JSON_DATA}}", generateJsonData(samples));

            try (FileWriter writer = new FileWriter(reportFile)) {
                writer.write(finalHtml);
            }
            System.out.println("HtmlReportGenerator: Report generated: " + reportFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String loadResource(String path) {
        try (InputStream is = HtmlReportGenerator.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new java.io.FileNotFoundException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static List<TestSample> getCalculableSamples(List<TestSample> roots) {
        List<TestSample> validLeaves = new ArrayList<>();
        for (TestSample s : roots) {
            if (s.getChildren().isEmpty()) {
                validLeaves.add(s);
            } else {
                validLeaves.addAll(getCalculableSamples(s.getChildren()));
            }
        }
        return validLeaves;
    }

    private static String generateListHtml(List<TestSample> samples, String filter) {
        StringBuilder sb = new StringBuilder();
        for (TestSample s : samples) {
            boolean isTransaction = !s.getChildren().isEmpty();
            if (isTransaction) {
                List<TestSample> filteredChildren = s.getChildren().stream().filter(c -> {
                    if (filter.equals("all"))
                        return true;
                    if (filter.equals("pass"))
                        return c.isSuccessful();
                    return !c.isSuccessful();
                }).collect(Collectors.toList());

                if (!filteredChildren.isEmpty()) {
                    boolean txSuccess = s.isSuccessful();
                    String badgeClass = txSuccess ? "badge-pass" : "badge-fail";
                    String panelId = "panel-" + filter + "-" + s.hashCode();
                    sb.append("<button class='accordion' onclick=\"toggleAccordion('")
                            .append(panelId).append("')\">");
                    sb.append("<span>").append(s.getLabel()).append(" (<span class='child-count'>")
                            .append(filteredChildren.size()).append("</span>)</span>");
                    sb.append("<span class='status-badge ").append(badgeClass).append("'>")
                            .append(txSuccess ? "PASS" : "FAIL").append("</span></button>");
                    sb.append("<div id='").append(panelId).append("' class='panel'>");
                    for (TestSample child : filteredChildren)
                        sb.append(generateRowHtml(child));
                    sb.append("</div>");
                }
            } else {
                boolean match = false;
                if (filter.equals("all"))
                    match = true;
                else if (filter.equals("pass") && s.isSuccessful())
                    match = true;
                else if (filter.equals("fail") && !s.isSuccessful())
                    match = true;
                if (match)
                    sb.append(generateRowHtml(s));
            }
        }

        if (sb.length() == 0) {
            sb.append("<div style='text-align:center; padding:40px 20px; color:var(--text-muted); font-size:1.1em; font-style:italic;'>No requests available in this view.</div>");
        }

        return sb.toString();
    }

    private static String generateRowHtml(TestSample s) {
        String statusClass = s.isSuccessful() ? "text-pass" : "text-fail";
        String method = (s.getMethod() != null && !s.getMethod().isEmpty()) ? s.getMethod() : "REQ";
        String specialClass = "";
        boolean hasChildren = !s.getChildren().isEmpty();
        if (hasChildren) {
            method = "Transaction Controller";
            specialClass = "tx";
        }

        String failureIndication = "";
        if (!s.isSuccessful() && s.getFailureType() != null && !s.getFailureType().isEmpty() && !s.getFailureType().equals("Pass")) {
            failureIndication = " <span style='font-size:0.8em; font-weight:bold; color:var(--text-fail); margin-left:10px;'>[" + s.getFailureType() + "]</span>";
        }

        String failTypeAttr = (s.getFailureType() != null) ? s.getFailureType().replace("'", "&#39;") : "";
        String codeAttr = (s.getResponseCode() != null) ? s.getResponseCode().replace("'", "&#39;") : "";

        return "<div class='request-row' data-fail-type='" + failTypeAttr + "' data-code='" + codeAttr + "' onclick=\"openModal('" + s.hashCode() + "')\"><span class='req-method "
                + specialClass + "'>" + method + "</span><span class='req-url'>" + s.getLabel() + failureIndication
                + "</span><span class='req-status " + statusClass + "'>"
                + (s.isSuccessful() ? "PASS" : "FAIL") + "</span></div>";
    }

    private static String generateJsonData(List<TestSample> samples) {
        StringBuilder json = new StringBuilder("[");
        for (TestSample s : samples)
            json.append(sampleToJson(s)).append(",");
        if (json.length() > 1)
            json.setLength(json.length() - 1);
        json.append("]");
        return json.toString();
    }

    private static String sampleToJson(TestSample s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{id:'").append(s.hashCode()).append("',");
        sb.append("label:\"").append(escapeJson(s.getLabel())).append("\",");
        sb.append("url:\"").append(escapeJson(s.getUrl())).append("\",");
        sb.append("method:\"").append(escapeJson(s.getMethod())).append("\",");
        sb.append("reqHeaders:\"").append(escapeJson(s.getRequestHeaders())).append("\",");
        sb.append("reqBody:\"").append(escapeJson(s.getRequestBody())).append("\",");
        sb.append("resCode:\"").append(escapeJson(s.getResponseCode())).append("\",");
        sb.append("resMsg:\"").append(escapeJson(s.getResponseMessage())).append("\",");
        sb.append("resHeaders:\"").append(escapeJson(s.getResponseHeaders())).append("\",");
        sb.append("resBody:\"").append(escapeJson(s.getResponseBody())).append("\",");
        sb.append("latency:").append(s.getLatency()).append(",");
        sb.append("failureType:\"").append(escapeJson(s.getFailureType())).append("\",");
        sb.append("assertions:[");
        for (TestSample.AssertionData ad : s.getAssertions()) {
            sb.append("{name:\"").append(escapeJson(ad.getName())).append("\",");
            sb.append("failure:").append(ad.isFailure()).append(",");
            sb.append("failureMessage:\"").append(escapeJson(ad.getFailureMessage())).append("\"},");
        }
        if (!s.getAssertions().isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("]");
        if (!s.getChildren().isEmpty()) {
            sb.append(",children:[");
            for (TestSample child : s.getChildren())
                sb.append(sampleToJson(child)).append(",");
            if (s.getChildren().size() > 0)
                sb.setLength(sb.length() - 1);
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("`", "\\`").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }
}
