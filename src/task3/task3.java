package task3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.*;

public class task3 {

    private final static String TITLE_TESTS = "tests";
    private final static String TITLE_VALUES = "values";

    public static void main(String[] args) {
        try(
                BufferedReader valuesReader = new BufferedReader(new FileReader(Paths.get(args[0]).toAbsolutePath().toString()));
                BufferedReader testsReader = new BufferedReader(new FileReader(Paths.get(args[1]).toAbsolutePath().toString()));
                BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(args[2]).toAbsolutePath().toString()))
        ) {
            StringBuilder sbTests = new StringBuilder();
            String line;
            while ((line = testsReader.readLine()) != null) {
                sbTests.append(line);
            }
            StringBuilder sbValues = new StringBuilder();
            while ((line = valuesReader.readLine()) != null) {
                sbValues.append(line);
            }
            Parser parserTests = new Parser(sbTests.toString());
            Parser parserValues = new Parser(sbValues.toString());

            Map<String, Object> mapTests = parserTests.parse();
            Map<String, Object> mapValues = parserValues.parse();

            List<Test> tests = Converter.convertObjectListToTest((ArrayList<Object>) mapTests.get(TITLE_TESTS));
            List<Value> values = Converter.convertObjectListToValues((ArrayList<Object>) mapValues.get(TITLE_VALUES));

            writer.write(Reporter.report(tests, values));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Reporter {

        public static String report(List<Test> test, List<Value> value) {
            Map<Integer, String> valueMap = new HashMap<>();
            for (Value val : value) {
                valueMap.put(val.id, val.value);
            }
            List<Report> reportList = new ArrayList<>();
            for (Test t : test) {
                reportList.add(Converter.convertTestToReport(t, valueMap));
            }
            Reports reports = new Reports(reportList);
            return Converter.convertReportsToJson(reports);
        }
    }

    public enum JsonToken {
        LEFT_BRACE("{"), RIGHT_BRACE("}"),
        LEFT_BRACKET("["), RIGHT_BRACKET("]"),
        COLON(":"), COMMA(","),
        STRING("string"), NUMBER("number");

        private final String value;

        JsonToken(String value) { this.value = value; }

        @Override
        public String toString() {
            return "JsonToken{" +
                    "value='" + value + '\'' +
                    '}';
        }
    }

    private static class Converter {

        public static List<Test> convertObjectListToTest(ArrayList<Object> json) {
            ArrayList<Test> result = new ArrayList<>();
            for (Object obj : json) {
                Test test = convertObjectToTest(obj);
                result.add(test);
            }
            return result;
        }

        private static Test convertObjectToTest(Object obj) {
            Test test = null;
            if (obj instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) obj;
                test = new Test(
                        (int) map.get("id"),
                        (String) map.get("title"),
                        "",
                        map.containsKey("values") ? convertObjectListToTest((ArrayList<Object>) map.get("values")) : null
                );
            }
            return test;
        }

        public static List<Value> convertObjectListToValues(ArrayList<Object> json) {
            ArrayList<Value> result = new ArrayList<>();
            for (Object obj : json) {
                if (obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    Value value = new Value(
                            (int) map.get("id"),
                            (String) map.get("value")
                    );
                    result.add(value);
                }
            }
            return result;
        }

        private static Report convertTestToReport(Test test, Map<Integer, String> valueMap) {
            String combinedValue = valueMap.getOrDefault(test.id, test.value);
            List<Report> innerReports = new ArrayList<>();
            if (test.values != null) {
                for (Test childTest : test.values) {
                    innerReports.add(convertTestToReport(childTest, valueMap));
                }
            }
            return new Report(test.id, test.title, combinedValue, innerReports);
        }

        public static String convertReportsToJson(Reports reports) {
            StringBuilder json = new StringBuilder();
            json.append("{\"reports\": [");

            for (int i = 0; i < reports.reports.size(); i++) {
                Report report = reports.reports.get(i);
                json.append(convertReportToJson(report));

                if (i < reports.reports.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]}");
            return json.toString();
        }

        public static String convertReportToJson(Report report) {
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"id\": ").append(report.id).append(",");
            json.append("\"title\": \"").append(report.title).append("\",");
            json.append("\"value\": \"").append(report.value).append("\"");

            if (report.values != null && !report.values.isEmpty()) {
                json.append(",\"values\": [");
                for (int i = 0; i < report.values.size(); i++) {
                    json.append(convertReportToJson(report.values.get(i)));
                    if (i < report.values.size() - 1) {
                        json.append(",");
                    }
                }
                json.append("]");
            }

            json.append("}");
            return json.toString();
        }
    }

    private static class Parser {
        private final String json;
        private int index = 0;
        private JsonToken token;

        public Parser(String json) {
            if (json != null) {
                this.json = json;
                currentToken();
                if (token != JsonToken.LEFT_BRACE) {
                    throw new IllegalArgumentException("Document should start with '{'");
                }
            } else {
                throw new IllegalArgumentException("Document is null");
            }
        }

        public Map<String, Object> parse() {
            return parseObject();
        }

        private Object parseValue() {
            nextToken();
            switch (token) {
                case JsonToken.COLON -> {
                    nextToken();
                    parseValue();
                }
                case JsonToken.NUMBER -> {
                    return parseNumber();
                }
                case JsonToken.STRING -> {
                    return parseString();
                }
                case JsonToken.LEFT_BRACKET -> {
                    return parseArray();
                }
                case JsonToken.LEFT_BRACE -> {
                    return parseObject();
                }
                default -> throw new IllegalStateException("Unexpected value: " + token);
            }
            return null;
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> obj = new HashMap<>();

            while (token != JsonToken.RIGHT_BRACE) {
                String key = (String) parseValue();
                nextToken(JsonToken.COLON);
                Object value = parseValue();
                obj.put(key, value);
                nextToken();
            }

            return obj;
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            while (token != JsonToken.RIGHT_BRACKET) {
                Object value = parseValue();
                array.add(value);
                nextToken();
            }
            return array;
        }

        private int parseNumber() {
            StringBuilder sb = new StringBuilder();

            while (Character.isDigit(json.charAt(index))) {
                sb.append(json.charAt(index++));
            }
            index--;
            return Integer.parseInt(sb.toString());
        }

        private String parseString() {
//            nextToken(JsonToken.STRING);
            int start = index + 1;
            nextToken(JsonToken.STRING);
            int end = index;
            return json.substring(start, end);
        }

        private void currentToken() {
            if (index < json.length()) {
                skipWhitespace();
                char c = json.charAt(index);
                switch (c) {
                    case '{': token = JsonToken.LEFT_BRACE; break;
                    case '}': token = JsonToken.RIGHT_BRACE; break;
                    case '[': token = JsonToken.LEFT_BRACKET; break;
                    case ']': token = JsonToken.RIGHT_BRACKET; break;
                    case ':': token = JsonToken.COLON; break;
                    case ',': token = JsonToken.COMMA; break;
                    case '"': token = JsonToken.STRING; break;
                    default: {
                        if (Character.isDigit(c) && token != JsonToken.STRING) {
                            token = JsonToken.NUMBER;
                        } else {
//                            throw new IllegalStateException(String.valueOf(c) + " index: " + index);
                            index++;
                            currentToken();
                        }
                    }
                }
            }
        }

        private void nextToken() {
            index++;
            currentToken();
        }

        private void nextToken(JsonToken token) {
            index++;
            currentToken();
            if (this.token != token) {
                throw new IllegalStateException("Expected " + token + " token");
            }
        }

        private void skipWhitespace() {
            for (; index < json.length(); index++) {
                char c = json.charAt(index);
                if (!Character.isWhitespace(c)) {
                    break;
                }
            }
        }
    }

    public static class Test {
        int id;
        String title;
        String value;
        List<Test> values;

        Test(int id, String title, String value) {
            this.id = id;
            this.title = title;
            this.value = value;
            this.values = null;
        }

        Test(int id, String title, String value, List<Test> values) {
            this.id = id;
            this.title = title;
            this.value = value;
            this.values = values;
        }

        @Override
        public String toString() {
            return "{" +
                    "\n\t\"id\": " + id +
                    ", \n\t\"title\": \"" + title + '\"' +
                    ", \n\t\"value\": \"" + value + '\"' +
                    ", \n\t\"values\": " + values +
                    "\n}";
        }
    }

    public static class Value {
        int id;
        String value;

        public Value(int id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        public String toString() {
            return "{" +
                    "\n\t\"id\":" + id +
                    ", \n\t\"value\": " + value + '\"' +
                    "\n}";
        }
    }

    public static class Report {
        int id;
        String title;
        String value;
        List<Report> values;

        Report(int id, String title, String value) {
            this.id = id;
            this.title = title;
            this.value = value;
            this.values = new ArrayList<>();
        }

        Report(int id, String title, String value, List<Report> values) {
            this.id = id;
            this.title = title;
            this.value = value;
            this.values = values;
        }

        @Override
        public String toString() {
            return "{" +
                    "\n\t\"id\": " + id +
                    ", \n\t\"title\": \"" + title + '\"' +
                    ", \n\t\"value\": \"" + value + '\"' +
                    ", \n\t\"values\": " + values +
                    "\n}";
        }
    }

    public static class Reports {
        List<Report> reports;

        Reports(List<Report> reports) {
            this.reports = reports;
        }
    }
}
