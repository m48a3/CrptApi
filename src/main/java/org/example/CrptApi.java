package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Semaphore requestSemaphore;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.requestSemaphore = new Semaphore(requestLimit);

        long period = switch (timeUnit) {
            case SECONDS -> 1;
            case MINUTES -> 60;
            case HOURS -> 3600;
            default -> throw new IllegalArgumentException("Unsupported time unit");
        };

        new java.util.Timer(true).scheduleAtFixedRate(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        requestSemaphore.release(requestLimit - requestSemaphore.availablePermits());
                    }
                },
                0, TimeUnit.SECONDS.toMillis(period)
        );
    }

    public void createDocument(String apiUrl, Document document, String signature) {
        try {
            requestSemaphore.acquire();

            String requestBody = objectMapper.writeValueAsString(document);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            requestSemaphore.release();
        }
    }

        public static class Document {
            @JsonProperty("description")
            private final Map<String, String> description;

            @JsonProperty("doc_id")
            private final String docId;

            @JsonProperty("doc_status")
            private final String docStatus;

            @JsonProperty("doc_type")
            private final String docType;

            @JsonProperty("importRequest")
            private final boolean importRequest;

            @JsonProperty("owner_inn")
            private final String ownerInn;

            @JsonProperty("participant_inn")
            private final String participantInn;

            @JsonProperty("producer_inn")
            private final String producerInn;

            @JsonProperty("production_date")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private final LocalDate productionDate;

            @JsonProperty("production_type")
            private final String productionType;

            @JsonProperty("products")
            private final List<Product> products;

            @JsonProperty("reg_date")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private final LocalDate regDate;

            @JsonProperty("reg_number")
            private final String regNumber;


        public Document(Map<String, String> description, String docId, String docStatus, String docType, boolean importRequest, String ownerInn, String participantInn, String producerInn, LocalDate productionDate, String productionType, List<Product> products, LocalDate regDate, String regNumber) {
            this.description = description;
            this.docId = docId;
            this.docStatus = docStatus;
            this.docType = docType;
            this.importRequest = importRequest;
            this.ownerInn = ownerInn;
            this.participantInn = participantInn;
            this.producerInn = producerInn;
            this.productionDate = productionDate;
            this.productionType = productionType;
            this.products = products;
            this.regDate = regDate;
            this.regNumber = regNumber;
        }
        public Document(){
            this.description = null;
            this.docId = null;
            this.docStatus = null;
            this.docType = null;
            this.importRequest = false;
            this.ownerInn = null;
            this.participantInn = null;
            this.producerInn = null;
            this.productionDate = null;
            this.productionType = null;
            this.products = null;
            this.regDate = null;
            this.regNumber = null;
        }
    }
        public static class Product {
            @JsonProperty("certificate_document")
            private final String certificateDocument;

            @JsonProperty("certificate_document_date")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private final LocalDate certificateDocumentDate;

            @JsonProperty("certificate_document_number")
            private final String certificateDocumentNumber;

            @JsonProperty("owner_inn")
            private final String ownerInn;

            @JsonProperty("producer_inn")
            private final String producerInn;

            @JsonProperty("production_date")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            private final LocalDate productionDate;

            @JsonProperty("tnved_code")
            private final String tnvedCode;

            @JsonProperty("uit_code")
            private final String uitCode;

            @JsonProperty("uitu_code")
            private final String uituCode;

            public Product(String certificateDocument, LocalDate certificateDocumentDate, String certificateDocumentNumber, String ownerInn, String producerInn, LocalDate productionDate, String tnvedCode, String uitCode, String uituCode) {
                this.certificateDocument = certificateDocument;
                this.certificateDocumentDate = certificateDocumentDate;
                this.certificateDocumentNumber = certificateDocumentNumber;
                this.ownerInn = ownerInn;
                this.producerInn = producerInn;
                this.productionDate = productionDate;
                this.tnvedCode = tnvedCode;
                this.uitCode = uitCode;
                this.uituCode = uituCode;
            }
            public Product(){
                this.certificateDocument = null;
                this.certificateDocumentDate = null;
                this.certificateDocumentNumber = null;
                this.ownerInn = null;
                this.producerInn = null;
                this.productionDate = null;
                this.tnvedCode = null;
                this.uitCode = null;
                this.uituCode = null;
            }

        }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        Document document = new Document();
        String apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        String signature = "test_signature_string";

        crptApi.createDocument(apiUrl, document, signature);
    }
}