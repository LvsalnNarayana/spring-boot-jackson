package com.example.spring_boot_jackson.tasks;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * StreamingApiTask
 *
 * Comprehensive, production-minded demonstrations of Jackson's Streaming API (JsonParser & JsonGenerator).
 * - All public methods are zero-arg so they can be invoked via your reflective TopicTask runner.
 * - Methods operate in a streaming-safe manner (constant memory), suitable for very large files (10GB+).
 *
 * Key produced files:
 * - log-1gb.json           -> NDJSON file produced by generateOneGbJsonLogFile()
 * - filtered-errors.ndjson -> output from streamAndFilterLogs()
 * - logs-array.json        -> result of convertNdjsonToJsonArrayStreaming()
 *
 * IMPORTANT:
 * - The generateOneGbJsonLogFile() will write ~1GB and can take significant time depending on disk throughput.
 * - Avoid stdout flooding when running over the full file: example methods limit printed output.
 */
public class StreamingApiTask {

    private final ObjectMapper mapper;

    public StreamingApiTask() {
        this.mapper = new ObjectMapper();
    }

    private String sampleJson() {
        return """
        {
          "name": "Narayana",
          "age": 30,
          "address": {
            "city": "Bangalore",
            "country": "India"
          },
          "hobbies": ["chess", "coding"]
        }
        """;
    }

    /*
     * =======================================================
     *  METHOD: generateOneGbJsonLogFile()
     *  TYPE: File Generation — Streaming Safe
     *  FLOW: JsonGenerator -> NDJSON File (~1GB)
     *
     *  DESCRIPTION:
     *    Produces a line-delimited NDJSON file named "log-1gb.json".
     *    Uses JsonGenerator to create each JSON object, writes each object
     *    followed by a newline. Keeps memory low by buffering only one
     *    line object at a time.
     *
     *  BEFORE:
     *    No file or smaller test file.
     *
     *  AFTER:
     *    "log-1gb.json" exists and is approximately 1GB.
     *
     *  NOTES:
     *    - NDJSON format is ideal for streaming processing.
     *    - Disk throughput is the bottleneck; an SSD will speed up generation.
     *    - Method is zero-argument for compatibility with your runner.
     * =======================================================
     */
    public void generateOneGbJsonLogFile() throws IOException {

        final JsonFactory factory = mapper.getFactory();
        final long targetBytes = 1024L * 1024L * 1024L; // 1GB
        long writtenBytes = 0L;

        // Output path is relative to working directory; change if needed
        File outFile = new File("log-1gb.json");

        try (OutputStream out = new FileOutputStream(outFile)) {

            int counter = 0;
            final String[] levels = {"INFO", "WARN", "ERROR", "DEBUG"};

            // Loop until ~1GB written. Each iteration writes one NDJSON line.
            while (writtenBytes < targetBytes) {

                // Buffer the single JSON object in memory (small).
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(512);
                try (JsonGenerator gen = factory.createGenerator(buffer, JsonEncoding.UTF8)) {

                    gen.writeStartObject();
                    gen.writeStringField("timestamp", Instant.now().toString());
                    gen.writeStringField("level", levels[counter % levels.length]);
                    gen.writeStringField("message", "Sample log message " + counter);
                    gen.writeNumberField("eventId", counter);

                    gen.writeObjectFieldStart("metadata");
                    gen.writeStringField("thread", "worker-" + (counter % 16));
                    gen.writeStringField("source", "StreamingApiTask");
                    gen.writeEndObject();

                    gen.writeEndObject();
                    gen.flush();
                }

                // NDJSON: object + newline
                byte[] jsonLine = (buffer.toString(StandardCharsets.UTF_8) + "\n").getBytes(StandardCharsets.UTF_8);

                out.write(jsonLine);
                writtenBytes += jsonLine.length;
                counter++;

                // Periodic flush and progress report (tunable)
                if (counter % 50_000 == 0) {
                    System.out.println("Generated so far: " + (writtenBytes / (1024L * 1024L)) + " MB");
                    out.flush();
                }
            }

            out.flush();
            System.out.println("Completed: " + outFile.getAbsolutePath() + " (~1GB written).");
        }
    }

    /*
     * =======================================================
     *  METHOD: parseLargeFileWithStreaming()
     *  TYPE: Streaming Parse
     *  FLOW: FileInputStream -> JsonParser (token-by-token)
     *
     *  DESCRIPTION:
     *    Demonstrates true streaming parsing: open an InputStream over the NDJSON file
     *    and walk tokens using JsonParser. Each object is processed token-by-token,
     *    only extracting the fields we need. This pattern avoids any per-line allocation.
     *
     *  BEFORE:
     *    A file with many JSON objects in NDJSON form (log-1gb.json).
     *
     *  AFTER:
     *    Each object's selected fields are printed (limited output to avoid flooding).
     *
     *  NOTES:
     *    - Best for extremely large files or when single-line parser allocation is unacceptable.
     *    - Use parser.skipChildren() to bypass nested structures efficiently.
     * =======================================================
     */
    public void parseLargeFileWithStreaming() throws IOException {

        File file = new File("log-1gb.json");
        if (!file.exists()) {
            System.out.println("File not found: log-1gb.json — run generateOneGbJsonLogFile() first.");
            return;
        }

        final JsonFactory factory = mapper.getFactory();
        long items = 0L;

        try (InputStream fis = new BufferedInputStream(new FileInputStream(file), 64 * 1024);
             JsonParser parser = factory.createParser(fis)) {

            System.out.println("Streaming token-by-token parse starting...");

            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                // Each NDJSON object will begin with START_OBJECT
                if (token == JsonToken.START_OBJECT) {

                    Integer eventId = null;
                    String level = null;
                    String message = null;

                    // Read until END_OBJECT for the current top-level object
                    while ((token = parser.nextToken()) != JsonToken.END_OBJECT) {
                        if (token == JsonToken.FIELD_NAME) {
                            String field = parser.getCurrentName();
                            parser.nextToken(); // move to value

                            switch (field) {
                                case "eventId" -> eventId = parser.getIntValue();
                                case "level" -> level = parser.getText();
                                case "message" -> message = parser.getText();
                                default -> {
                                    // For nested objects or arrays, skip to reduce cost
                                    if (parser.getCurrentToken().isStructStart()) {
                                        parser.skipChildren();
                                    }
                                }
                            }
                        }
                    }

                    // Print only the first few items to keep console readable
                    if (items < 10) {
                        System.out.println("Parsed item: eventId=" + eventId + " level=" + level + " msg=" + message);
                    }

                    items++;
                    if (items % 5_000_000 == 0) {
                        System.out.println("Parsed items so far: " + items);
                    }
                }
            }

            System.out.println("Streaming parse finished. Total items parsed: " + items);
        }
    }

    /*
     * =======================================================
     *  METHOD: processNdjsonLineByLine()
     *  TYPE: NDJSON Line Processing
     *  FLOW: BufferedReader -> mapper.readTree(line) -> process node
     *
     *  DESCRIPTION:
     *    A pragmatic approach: read the file line-by-line using BufferedReader,
     *    parse each standalone JSON object into a small JsonNode and operate on it.
     *    This is simpler than token-by-token parsing but still memory-friendly since
     *    only one line's JsonNode exists at a time.
     *
     *  BEFORE:
     *    NDJSON lines, each a JSON object.
     *
     *  AFTER:
     *    Lightweight summary output; counts and occasional sample prints.
     *
     *  NOTES:
     *    - Use this when each NDJSON line is reasonably small.
     *    - If individual lines are huge, use token-by-token parsing instead.
     * =======================================================
     */
    public void processNdjsonLineByLine() throws IOException {

        File file = new File("log-1gb.json");
        if (!file.exists()) {
            System.out.println("File not found: log-1gb.json — generate it with generateOneGbJsonLogFile()");
            return;
        }

        long processed = 0L;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8), 64 * 1024)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                // Parse single line into JsonNode (small transient allocation)
                JsonNode node = mapper.readTree(line);

                // Example processing: read timestamp and level. Keep limited printing.
                if (processed < 5) {
                    System.out.println("Line sample [" + processed + "]: " + node.path("timestamp").asText() + " " + node.path("level").asText());
                }

                processed++;
                if (processed % 5_000_000 == 0) {
                    System.out.println("Lines processed: " + processed);
                }
            }

            System.out.println("Completed line-by-line processing. Total lines: " + processed);
        }
    }

    /*
     * =======================================================
     *  METHOD: streamAndFilterLogs()
     *  TYPE: Stream Filter -> NDJSON Output
     *  FLOW: read line -> filter condition -> write matching lines to output NDJSON
     *
     *  DESCRIPTION:
     *    Reads "log-1gb.json" and writes only matching entries (level == ERROR) to "filtered-errors.ndjson".
     *    This preserves NDJSON semantics for downstream streaming consumers.
     *
     *  BEFORE:
     *    log-1gb.json with mixed levels.
     *
     *  AFTER:
     *    filtered-errors.ndjson containing only ERROR entries.
     *
     *  NOTES:
     *    - Writing raw validated lines is efficient and avoids re-serialization overhead.
     *    - For extra safety you may re-serialize parsed nodes; here we write the raw line.
     * =======================================================
     */
    public void streamAndFilterLogs() throws IOException {

        File input = new File("log-1gb.json");
        if (!input.exists()) {
            System.out.println("File not found: log-1gb.json — generate it first.");
            return;
        }

        File output = new File("filtered-errors.ndjson");
        long processed = 0L;
        long writtenBytes = 0L;
        long writtenLines = 0L;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8), 64 * 1024);
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(output), 64 * 1024)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                JsonNode node = mapper.readTree(line);
                String level = node.path("level").asText("");

                if ("ERROR".equals(level)) {
                    byte[] bytes = (line + "\n").getBytes(StandardCharsets.UTF_8);
                    fos.write(bytes);
                    writtenBytes += bytes.length;
                    writtenLines++;
                }

                processed++;
                if (processed % 5_000_000 == 0) {
                    fos.flush();
                    System.out.println("Processed: " + processed + " | Written lines: " + writtenLines + " | Written MB: " + (writtenBytes / (1024L * 1024L)));
                }
            }

            fos.flush();
            System.out.println("Filtering complete. Total processed: " + processed + " | ERROR lines written: " + writtenLines);
        }
    }

    /*
     * =======================================================
     *  METHOD: convertNdjsonToJsonArrayStreaming()
     *  TYPE: NDJSON -> Single JSON Array (streaming)
     *  FLOW: open generator -> writeStartArray -> copy each object -> writeEndArray
     *
     *  DESCRIPTION:
     *    Produces "logs-array.json" which contains a single JSON array with all objects.
     *    Uses JsonGenerator and per-line JsonParser.copyCurrentStructure() to stream objects into the array.
     *
     *  BEFORE:
     *    log-1gb.json (NDJSON)
     *
     *  AFTER:
     *    logs-array.json -> valid JSON array: [ {...}, {...}, ... ]
     *
     *  NOTES:
     *    - The resulting array file may be very large and less streaming-friendly than NDJSON.
     * =======================================================
     */
    public void convertNdjsonToJsonArrayStreaming() throws IOException {

        File input = new File("log-1gb.json");
        if (!input.exists()) {
            System.out.println("File not found: log-1gb.json");
            return;
        }

        File output = new File("logs-array.json");
        final JsonFactory factory = mapper.getFactory();
        long count = 0L;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8), 64 * 1024);
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(output), 64 * 1024);
             JsonGenerator gen = factory.createGenerator(fos, JsonEncoding.UTF8)) {

            gen.writeStartArray();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                try (JsonParser single = factory.createParser(line)) {
                    // Move the parser to the first token (START_OBJECT)
                    JsonToken t = single.nextToken();
                    if (t == JsonToken.START_OBJECT) {
                        // copyCurrentStructure copies the whole object the parser currently points to
                        gen.copyCurrentStructure(single);
                        count++;
                    }
                }

                if (count % 5_000_000 == 0 && count > 0) {
                    System.out.println("Copied objects: " + count);
                    gen.flush();
                }
            }

            gen.writeEndArray();
            gen.flush();

            System.out.println("Conversion finished. Total objects: " + count + ". Output: " + output.getAbsolutePath());
        }
    }

    /*
     * =======================================================
     *  METHOD: countLogLevelsStreaming()
     *  TYPE: Streaming Aggregation
     *  FLOW: read line -> parse -> increment small counters map -> print
     *
     *  DESCRIPTION:
     *    Counts occurrences of each log level (INFO/WARN/ERROR/DEBUG).
     *    Memory cost is tiny: only the counters HashMap (~few entries).
     *
     *  AFTER:
     *    Prints counts for each level.
     *
     *  NOTES:
     *    - For distributed scale, consider streaming counts into a store (Kafka / Redis / RocksDB).
     * =======================================================
     */
    public void countLogLevelsStreaming() throws IOException {

        File input = new File("log-1gb.json");
        if (!input.exists()) {
            System.out.println("File not found: log-1gb.json");
            return;
        }

        final Map<String, Long> counters = new HashMap<>();
        long processed = 0L;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8), 64 * 1024)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;

                JsonNode node = mapper.readTree(line);
                String level = node.path("level").asText("UNKNOWN");
                counters.put(level, counters.getOrDefault(level, 0L) + 1L);

                processed++;
                if (processed % 5_000_000 == 0) {
                    System.out.println("Processed: " + processed);
                }
            }
        }

        System.out.println("Counts by level: " + counters);
    }

    /*
     * =======================================================
     *  METHOD: demonstrateCreateParser()
     *  TYPE: Basic Streaming Demo
     *  FLOW: JsonFactory.createParser(sampleJson()) -> print tokens
     *
     *  DESCRIPTION:
     *    Very small demo showing JsonParser tokens for an in-memory JSON sample.
     * =======================================================
     */
    public void demonstrateCreateParser() throws IOException {

        JsonFactory factory = mapper.getFactory();
        try (JsonParser parser = factory.createParser(sampleJson())) {
            System.out.println("Tokens for sampleJson():");
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                System.out.println(token + (parser.getCurrentName() != null ? " -> " + parser.getCurrentName() : ""));
            }
        }
    }

    /*
     * =======================================================
     *  METHOD: demonstrateNextTokenAndGetValues()
     *  TYPE: Basic Streaming Demo
     *
     *  DESCRIPTION:
     *    Demonstrates moving the parser to values and extracting primitives.
     * =======================================================
     */
    public void demonstrateNextTokenAndGetValues() throws IOException {

        JsonFactory factory = mapper.getFactory();
        try (JsonParser parser = factory.createParser(sampleJson())) {

            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token == JsonToken.FIELD_NAME) {
                    String name = parser.getCurrentName();
                    parser.nextToken(); // move to value

                    if ("name".equals(name)) {
                        System.out.println("name = " + parser.getText());
                    } else if ("age".equals(name)) {
                        System.out.println("age = " + parser.getIntValue());
                    }
                }
            }
        }
    }

    /*
     * =======================================================
     *  METHOD: demonstrateSkipChildren()
     *  TYPE: Basic Streaming Demo
     *
     *  DESCRIPTION:
     *    Shows skipping nested structure (address) quickly.
     * =======================================================
     */
    public void demonstrateSkipChildren() throws IOException {

        JsonFactory factory = mapper.getFactory();
        try (JsonParser parser = factory.createParser(sampleJson())) {

            while (parser.nextToken() != null) {
                if ("address".equals(parser.getCurrentName())) {
                    parser.nextToken(); // move to START_OBJECT
                    parser.skipChildren();
                    System.out.println("Skipped 'address' structure");
                }
            }
        }
    }

    /*
     * =======================================================
     *  METHOD: demonstrateCreateGenerator()
     *  TYPE: Basic Streaming Write Demo
     *
     *  DESCRIPTION:
     *    Shows creating a JsonGenerator and writing a tiny object.
     * =======================================================
     */
    public void demonstrateCreateGenerator() throws IOException {

        JsonFactory factory = mapper.getFactory();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8)) {

            gen.writeStartObject();
            gen.writeStringField("name", "Narayana");
            gen.writeNumberField("age", 30);
            gen.writeEndObject();
            gen.flush();

            System.out.println("Generated sample JSON: " + out.toString(StandardCharsets.UTF_8));
        }
    }

    /*
     * =======================================================
     *  METHOD: demonstrateWriteArray()
     *  TYPE: Basic Streaming Write Demo
     *
     *  DESCRIPTION:
     *    Demonstrates writing a small array using JsonGenerator.
     * =======================================================
     */
    public void demonstrateWriteArray() throws IOException {

        JsonFactory factory = mapper.getFactory();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8)) {

            gen.writeStartObject();
            gen.writeFieldName("hobbies");
            gen.writeStartArray();
            gen.writeString("coding");
            gen.writeString("travel");
            gen.writeEndArray();
            gen.writeEndObject();
            gen.flush();

            System.out.println("Generated array JSON: " + out.toString(StandardCharsets.UTF_8));
        }
    }
}
