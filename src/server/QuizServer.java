package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Question;
import model.QuizResult;
import model.User;
import service.AuthService;
import service.DataStore;
import service.QuestionBank;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class QuizServer {

    private static final int PORT = 8080;
    
    // Services
    private static final AuthService auth = new AuthService();
    private static final QuestionBank bank = new QuestionBank();
    
    // Simple session storage (token -> username)
    private static final Map<String, String> sessions = new HashMap<>();

    public static void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/", new ApiHandler());

        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool()); // Multiple threads
        server.start();
        System.out.println("Web Server clearly running on http://localhost:" + PORT);
    }

    // =========================================================
    // Static File Hosting
    // =========================================================
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Security: basic directory traversal prevention
            if(path.contains("..")) {
                sendResponse(exchange, 403, "Forbidden");
                return;
            }

            File file = new File("public" + path);
            if (file.exists() && file.isFile()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().add("Content-Type", getMimeType(path));
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } else {
                sendResponse(exchange, 404, "File not found");
            }
        }

        private String getMimeType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            return "text/plain";
        }
    }

    // =========================================================
    // API logic
    // =========================================================
    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Optional CORS support if tested on different port
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            
            if ("OPTIONS".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String reqBody = getBody(exchange);
                
                // --- AUTH ---
                if (path.equals("/api/login") && "POST".equals(method)) {
                    String user = extractJson(reqBody, "username");
                    String pass = extractJson(reqBody, "password");
                    User u = auth.login(user, pass);
                    if (u != null) {
                        String token = UUID.randomUUID().toString();
                        sessions.put(token, u.getUsername());
                        sendResponse(exchange, 200, "{\"success\":true, \"token\":\"" + token + "\", \"isAdmin\":" + u.isAdmin() + ", \"username\":\"" + u.getUsername() + "\"}");
                    } else {
                        sendResponse(exchange, 401, "{\"success\":false, \"message\":\"Invalid credentials\"}");
                    }
                    return;
                }
                
                if (path.equals("/api/register") && "POST".equals(method)) {
                    String user = extractJson(reqBody, "username");
                    String pass = extractJson(reqBody, "password");
                    if (user == null || pass == null || pass.length() < 6) {
                        sendResponse(exchange, 400, "{\"success\":false, \"message\":\"Invalid payload\"}");
                        return;
                    }
                    if (auth.register(user, pass)) {
                        sendResponse(exchange, 200, "{\"success\":true}");
                    } else {
                        sendResponse(exchange, 200, "{\"success\":false, \"message\":\"Username taken\"}");
                    }
                    return;
                }
                
                // Get authenticated user
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                String token = authHeader != null ? authHeader.replace("Bearer ", "").trim() : null;
                String username = sessions.get(token);
                
                if (username == null) {
                    sendResponse(exchange, 401, "{\"error\": \"Unauthorized\"}");
                    return;
                }

                // --- ADMIN ENDPOINTS ---
                if (path.equals("/api/questions") && "GET".equals(method)) {
                    List<Question> qs = bank.getAllQuestions();
                    sendResponse(exchange, 200, toJsonArray(qs));
                    return;
                }
                
                if (path.equals("/api/questions") && "POST".equals(method)) {
                    // Add question
                    String text = extractJson(reqBody, "text");
                    String cat = extractJson(reqBody, "category");
                    int diff = Integer.parseInt(extractJson(reqBody, "difficulty"));
                    int correct = Integer.parseInt(extractJson(reqBody, "correctIdx"));
                    String oA = extractJson(reqBody, "optA");
                    String oB = extractJson(reqBody, "optB");
                    String oC = extractJson(reqBody, "optC");
                    String oD = extractJson(reqBody, "optD");
                    
                    bank.addQuestion(text, new String[]{oA, oB, oC, oD}, correct, cat, diff);
                    sendResponse(exchange, 200, "{\"success\":true}");
                    return;
                }
                
                if (path.equals("/api/questions/delete") && "POST".equals(method)) {
                    int id = Integer.parseInt(extractJson(reqBody, "id"));
                    bank.removeQuestion(id);
                    sendResponse(exchange, 200, "{\"success\":true}");
                    return;
                }
                
                if (path.equals("/api/results") && "GET".equals(method)) {
                    List<QuizResult> results = DataStore.loadResults();
                    sendResponse(exchange, 200, resultsToJsonArray(results));
                    return;
                }

                if (path.equals("/api/stats") && "GET".equals(method)) {
                    int total = bank.getTotalCount();
                    String json = "{\"total\":" + total + "}";
                    sendResponse(exchange, 200, json);
                    return;
                }

                // --- STUDENT ENDPOINTS ---
                if (path.equals("/api/quiz/generate") && "POST".equals(method)) {
                    String mode = extractJson(reqBody, "mode");
                    List<Question> qs;
                    if ("category".equals(mode)) {
                        qs = bank.getByCategory(extractJson(reqBody, "category"));
                    } else if ("difficulty".equals(mode)) {
                        qs = bank.getByDifficulty(Integer.parseInt(extractJson(reqBody, "difficulty")));
                    } else {
                        qs = bank.getAllQuestions(); // Full Quiz
                    }
                    // Shuffle
                    Collections.shuffle(qs);
                    sendResponse(exchange, 200, toJsonArray(qs));
                    return;
                }

                if (path.equals("/api/quiz/submit") && "POST".equals(method)) {
                    // Manual parsing of submitted data: {"elapsed": 30, "answers": [...]}
                    int elapsed = Integer.parseInt(extractJson(reqBody, "elapsed"));
                    String ansMapRegex = "\"answers\"\\s*:\\s*\\[(.*?)\\]";
                    Matcher m = Pattern.compile(ansMapRegex).matcher(reqBody);
                    List<Integer> ansList = new ArrayList<>();
                    if (m.find()) {
                        String[] parts = m.group(1).split(",");
                        for (String p : parts) {
                            if (!p.trim().isEmpty()) {
                                ansList.add(Integer.parseInt(p.trim()));
                            }
                        }
                    }
                    
                    // The client sends the questions back in ID order so they must reconstruct the list
                    // Actually, simpler: client sends {"questionIds": [1,2], "answers": [0,1], "elapsed": 45}
                    String qIdRegex = "\"questionIds\"\\s*:\\s*\\[(.*?)\\]";
                    Matcher mQ = Pattern.compile(qIdRegex).matcher(reqBody);
                    List<Integer> qIds = new ArrayList<>();
                    if (mQ.find()) {
                        String[] parts = mQ.group(1).split(",");
                        for (String p : parts) {
                            if (!p.trim().isEmpty()) {
                                qIds.add(Integer.parseInt(p.trim()));
                            }
                        }
                    }
                    
                    // Build question list 
                    List<Question> submittedQs = new ArrayList<>();
                    for(int id : qIds) {
                        for(Question q : bank.getAllQuestions()) {
                            if (q.getId() == id) submittedQs.add(q);
                        }
                    }
                    
                    int[] answersArr = new int[ansList.size()];
                    for (int i=0; i<ansList.size(); i++) answersArr[i] = ansList.get(i);
                    
                    QuizResult result = new QuizResult(username, submittedQs, answersArr, elapsed);
                    DataStore.appendResult(result);
                    
                    // Return result summary
                    String resJson = "{\"score\":" + result.getScore() + ", \"total\":" + result.getTotalQuestions() + 
                                     ", \"percentage\":" + result.getPercentage() + ", \"grade\":\"" + result.getGrade() + "\"}";
                    sendResponse(exchange, 200, resJson);
                    return;
                }
                
                sendResponse(exchange, 404, "{\"error\": \"Not Found\"}");
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Server Error\"}");
            }
        }
    }

    // =========================================================
    // Helpers
    // =========================================================
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static String getBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) out.append(line);
        return out.toString();
    }

    private static String extractJson(String json, String key) {
        // Very basic manual JSON string extractor for exact structure {"key": "val"} or {"key": 1}
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        
        // Try without quotes
        p = Pattern.compile("\"" + key + "\"\\s*:\\s*([^,}]+)");
        m = p.matcher(json);
        if (m.find()) return m.group(1).trim();
        return null;
    }
    
    // Manual JSON serialization
    private static String toJsonArray(List<Question> questions) {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i<questions.size(); i++) {
            Question q = questions.get(i);
            sb.append("{");
            sb.append("\"id\":").append(q.getId()).append(",");
            sb.append("\"text\":\"").append(escape(q.getQuestionText())).append("\",");
            sb.append("\"category\":\"").append(escape(q.getCategory())).append("\",");
            sb.append("\"difficulty\":").append(q.getDifficultyLevel()).append(",");
            sb.append("\"correctIdx\":").append(q.getCorrectOptionIndex()).append(",");
            sb.append("\"options\":[");
            String[] opts = q.getOptions();
            for(int j=0; j<opts.length; j++) {
                sb.append("\"").append(escape(opts[j])).append("\"");
                if (j < opts.length-1) sb.append(",");
            }
            sb.append("]");
            sb.append("}");
            if (i < questions.size()-1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String resultsToJsonArray(List<QuizResult> results) {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i<results.size(); i++) {
            QuizResult r = results.get(i);
            sb.append("{");
            sb.append("\"username\":\"").append(escape(r.getUsername())).append("\",");
            sb.append("\"score\":").append(r.getScore()).append(",");
            sb.append("\"total\":").append(r.getTotalQuestions()).append(",");
            sb.append("\"percentage\":").append(r.getPercentage()).append(",");
            sb.append("\"grade\":\"").append(r.getGrade()).append("\",");
            sb.append("\"time\":\"").append(r.getFormattedTime()).append("\",");
            sb.append("\"date\":\"").append(r.getAttemptTimeFormatted()).append("\"");
            sb.append("}");
            if (i < results.size()-1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
    
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
