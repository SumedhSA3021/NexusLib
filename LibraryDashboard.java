import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LibraryDashboard {
    private static LibraryService service = new LibraryService();

    public static void main(String[] args) throws IOException {
        // Start a background server listening on localhost port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("====================================================");
        System.out.println("  WE-देसी WEB BACKEND SERVER RUNNING ON PORT 8080   ");
        System.out.println("====================================================");
        System.out.println("👉 Keep this Eclipse program running continuously!");
        System.out.println("👉 Now you can open your HTML file inside your browser.");

        // Connection Endpoint 1: Book Availability Search
        server.createContext("/api/search", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                enableCORS(exchange);
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> params = getQueryParams(exchange.getRequestURI().getQuery());
                    String title = params.getOrDefault("title", "");

                    System.out.println("[Web Request] Searching for book title: " + title);
                    
                    // Route execution command to your core service framework
                    service.BookSearch(title); 

                    String response = "{\"status\": \"Processed successfully. Check Eclipse console for details.\"}";
                    sendJSONResponse(exchange, response);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        // Connection Endpoint 2: Student Transactions (Issue/Return)
        server.createContext("/api/student", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                enableCORS(exchange);
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> params = parsePostData(exchange);
                    String action = params.getOrDefault("action", "");
                    String stuId = params.getOrDefault("stuId", "");
                    String bookTitle = params.getOrDefault("bookTitle", "");

                    System.out.println("[Web Request] Student Action: " + action + " | ID: " + stuId + " | Book: " + bookTitle);

                    if ("borrow".equalsIgnoreCase(action)) {
                        service.Student_Search(stuId, bookTitle);
                    } else if ("return".equalsIgnoreCase(action)) {
                        service.Student_Return(stuId, bookTitle);
                    }

                    String response = "{\"status\": \"Transaction processed cleanly.\"}";
                    sendJSONResponse(exchange, response);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        // Connection Endpoint 3: Faculty Transactions (Issue/Return)
        server.createContext("/api/faculty", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                enableCORS(exchange);
                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    Map<String, String> params = parsePostData(exchange);
                    String action = params.getOrDefault("action", "");
                    String facId = params.getOrDefault("facId", "");
                    String bookTitle = params.getOrDefault("bookTitle", "");

                    System.out.println("[Web Request] Faculty Action: " + action + " | ID: " + facId + " | Book: " + bookTitle);

                    if ("borrow".equalsIgnoreCase(action)) {
                        service.Faculty_Search(facId, bookTitle);
                    } else if ("return".equalsIgnoreCase(action)) {
                        service.Faculty_Return(facId, bookTitle);
                    }

                    String response = "{\"status\": \"Transaction processed cleanly.\"}";
                    sendJSONResponse(exchange, response);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        });

        server.setExecutor(null);
        server.start();
    }

    // --- UTILITY HELPER METHODS FOR NETWORKING ---

    private static void enableCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendJSONResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> getQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private static Map<String, String> parsePostData(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return getQueryParams(body);
    }
}