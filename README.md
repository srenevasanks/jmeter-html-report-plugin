# JMeter API Test Report Plugin

Generate interactive API-focused HTML reports directly from Apache JMeter test executions.

The JMeter API Test Report Plugin provides a lightweight reporting solution for API automation and performance testing teams. It generates a consolidated HTML dashboard containing execution statistics, response time metrics, failure analysis, request and response payload details, and interactive filtering capabilities.

Designed specifically for API testing, the plugin helps testers and developers quickly identify failed requests, analyze errors, and share execution results without navigating through multiple JMeter listeners or log files.

---

## Why This Exists

Apache JMeter provides built-in reporting capabilities, but API teams often require faster access to failed requests, assertion failures, response details, and execution summaries.

This plugin focuses on simplifying API test result analysis by providing:

* API-centric reporting
* Failure-focused troubleshooting
* Interactive filtering
* Request and response visibility
* Shareable HTML reports
* Dark mode support

---

## Features

### Test Summary

* Total Requests
* Passed Requests
* Failed Requests
* Success Percentage
* Test Execution Duration

### Performance Metrics

* Average Response Time
* Minimum Response Time
* Maximum Response Time
* Throughput

### API Troubleshooting

* Failure-only filtering
* Response code filtering
* Error message visibility
* Assertion failure tracking
* Request-level execution drill-down
* Failed API identification

### Request & Response Analysis

* Request payload viewer
* Response payload viewer
* Request method visibility
* Response code visibility
* Timestamp tracking

### Reporting

* Automatic HTML report generation
* Interactive report navigation
* Dark mode support
* Single-file report output
* Easy sharing with QA, Developers, Product Owners, and Stakeholders

---

## Screenshots

### Dashboard Summary

[Add Screenshot]

### Failure Analysis

[Add Screenshot]

### Response Code Filtering

[Add Screenshot]

### Request & Response Details

[Add Screenshot]

---

## Requirements

| Requirement   | Version |
| ------------- | ------- |
| Apache JMeter | 5.5+    |
| Java          | 8+      |
| Maven         | 3.x     |

---

## Compatibility

Verified with:

* Apache JMeter 5.5
* Apache JMeter 5.6
* Apache JMeter 5.6.1
* Apache JMeter 5.6.2
* Apache JMeter 5.6.3

---

## Installation

### Option A: JMeter Plugins Manager

1. Open JMeter
2. Navigate to:

Options → Plugins Manager

3. Search for:

JMeter API Test Report Plugin

4. Click:

Apply Changes and Restart JMeter

---

### Option B: Manual Installation

Download the latest JAR from the Releases page.

Copy the JAR into:

<JMETER_HOME>/lib/ext/

Restart JMeter.

---

## Usage

1. Open your JMeter Test Plan.
2. Add the listener:

Add → Listener → API Test Report Listener

3. Configure the output directory.
4. Execute your test.
5. Open the generated report.html file in your browser.

---

## Report Sections

### Summary Dashboard

Provides an overview of test execution health, including request counts, success percentage, and execution duration.

### Performance Metrics

Displays response time statistics and throughput metrics.

### Failure Analysis

Quickly identify failed requests and investigate associated errors using:

* Failure-only filter
* Response code filter
* Error message visibility

### Request & Response Details

Inspect request and response information directly from the report, including:

* Request payload
* Response payload
* Response code
* Execution status
* Timestamp

---

## Build From Source

Clone the repository:

```bash
git clone https://github.com/<your-github-id>/jmeter-api-test-report-plugin.git
```

Navigate to the project:

```bash
cd jmeter-api-test-report-plugin
```

Build the plugin:

```bash
mvn clean package
```

Generated artifact:

```text
target/jmeter-api-test-report-plugin-1.0.0.jar
```

---

## Support and Bug Reports

Please report issues through GitHub Issues.

When reporting a bug, include:

* Plugin version
* Apache JMeter version
* Java version
* Operating system
* Sample JMX (if possible)
* Screenshot of the report
* Relevant error logs

---

## Roadmap

### Version 1.1

* PDF export support
* Enhanced API failure categorization
* Improved report performance for large executions

### Version 1.2

* Failure trend visualization
* Advanced filtering options
* Additional API execution metrics

---

## Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push your branch
5. Open a Pull Request

---

## License

Apache License 2.0

---

## Author

S. Vasan

GitHub:
https://github.com/<your-github-id>
