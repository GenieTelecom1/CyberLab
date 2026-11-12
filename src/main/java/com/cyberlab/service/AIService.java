package com.cyberlab.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Service class responsible for communicating with the Google Gemini API
 * and providing fallback educational responses when offline.
 */
public class AIService {

    /**
     * The default Gemini model. Change this single constant to switch models.
     * Can also be overridden at runtime using the GEMINI_MODEL environment variable.
     */
    public static final String DEFAULT_MODEL = "gemini-3.5-flash-lite";

    private static final String API_ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    private static final String SYSTEM_INSTRUCTION_TEXT =
            "You are CyberLab Assistant, an educational cybersecurity assistant.\n\n"
            + "Your purpose is to help students understand cybersecurity concepts and improve their digital safety.\n\n"
            + "You specialize in:\n"
            + "- Phishing\n"
            + "- Malware\n"
            + "- Social engineering\n"
            + "- Network security\n"
            + "- Password security\n"
            + "- Cybersecurity fundamentals\n\n"
            + "Give clear, accurate, educational answers suitable for students.\n"
            + "Keep answers concise and easy to understand unless the user requests more detail.\n"
            + "Do not provide harmful or illegal instructions. If a user asks about a potentially dangerous "
            + "cybersecurity topic, keep the response focused on defensive education and safe practices.";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String apiKey;
    private final String modelName;

    // Predefined fallback topics and educational responses
    private static final Map<String[], String> FALLBACK_KNOWLEDGE_BASE = new LinkedHashMap<>();

    static {
        registerFallback(
                new String[]{"phish", "phishing", "credential harvest", "fake login"},
                "🎣 What is Phishing?\n\n"
                + "Phishing is a deceptive social engineering attack where malicious actors impersonate trusted organizations "
                + "(such as banks, universities, or tech services) via email, SMS, or fake websites.\n\n"
                + "• Objective: Trick you into revealing sensitive credentials, passwords, or credit card details.\n"
                + "• Red Flags: Urgent tone, unfamiliar sender address, generic greetings, and suspicious links.\n"
                + "• Defense: Always inspect the sender's actual email domain, avoid clicking unsolicited links, and verify requests directly through official channels."
        );

        registerFallback(
                new String[]{"malware", "virus", "trojan", "worm", "spyware", "keylogger"},
                "🦠 What is Malware?\n\n"
                + "Malware (short for 'malicious software') is any software intentionally designed to cause damage to a computer, server, client, or computer network.\n\n"
                + "Common categories include:\n"
                + "• Viruses: Attach to legitimate files and replicate when executed.\n"
                + "• Worms: Self-replicating programs that spread across networks without human action.\n"
                + "• Trojans: Disguised as legitimate software to deceive users.\n"
                + "• Spyware & Keyloggers: Secretly monitor and record your activities and keystrokes.\n\n"
                + "• Defense: Keep your OS and software updated, use a reputable antivirus program, and only download files from verified sources."
        );

        registerFallback(
                new String[]{"ransomware", "ransom", "encrypted files", "extortion"},
                "🔒 What is Ransomware?\n\n"
                + "Ransomware is a severe type of malware that encrypts a victim's files, databases, or entire computer systems, making them inaccessible.\n\n"
                + "• Mechanism: The attacker demands a ransom payment (often in cryptocurrency) in exchange for the decryption key.\n"
                + "• Crucial Warning: Security experts and law enforcement recommend never paying the ransom, as it funds cybercrime and offers no guarantee of recovery.\n"
                + "• Defense: Maintain regular offline backups (3-2-1 backup strategy) and patch vulnerabilities promptly."
        );

        registerFallback(
                new String[]{"social engineering", "pretexting", "baiting", "human hacking"},
                "🎭 What is Social Engineering?\n\n"
                + "Social engineering is the psychological manipulation of people into performing actions or divulging confidential information.\n\n"
                + "Instead of attacking software vulnerabilities, attackers exploit human emotions such as trust, urgency, fear, or curiosity.\n\n"
                + "Common techniques:\n"
                + "• Pretexting: Creating an invented scenario to steal info (e.g., posing as IT support).\n"
                + "• Baiting: Leaving an infected USB stick in a parking lot labeled 'Salary List'.\n"
                + "• Defense: Cultivate healthy skepticism, verify identities independently, and adhere to security protocols."
        );

        registerFallback(
                new String[]{"strong password", "create a password", "password security", "passphrase", "good password"},
                "🔑 How to Create a Strong Password:\n\n"
                + "1. Use Length over Complexity: A 16+ character passphrase (e.g., 'Coffee!Rainbow#Bicycle42') is vastly harder to crack than a short, complex string.\n"
                + "2. Never Reuse Passwords: Every critical account (email, banking, university) must have a unique password.\n"
                + "3. Avoid Personal Info: Never include names, birthdays, pet names, or predictable sequences ('123456', 'Password1').\n"
                + "4. Use a Password Manager: Tools like Bitwarden or 1Password generate and store strong, unique passwords securely.\n"
                + "5. Enable Multi-Factor Authentication (MFA) on all accounts."
        );

        registerFallback(
                new String[]{"vpn", "virtual private network", "tunnel", "public wifi"},
                "🌐 What is a VPN (Virtual Private Network)?\n\n"
                + "A VPN creates an encrypted connection (tunnel) between your device and a remote server operated by the VPN provider.\n\n"
                + "• Purpose: Masks your real IP address and encrypts your internet traffic.\n"
                + "• When to use: Especially beneficial when connecting to untrusted public Wi-Fi networks (cafes, airports) to prevent packet sniffing.\n"
                + "• Limitation: A VPN protects data in transit, but it does NOT protect against malware downloads or phishing scams."
        );

        registerFallback(
                new String[]{"two-factor", "2fa", "mfa", "multi-factor", "authenticator"},
                "🛡️ What is Two-Factor Authentication (2FA)?\n\n"
                + "Two-Factor Authentication (2FA) adds a second layer of defense beyond just a password.\n\n"
                + "It requires two of three fundamental factors:\n"
                + "1. Something you know (Password / PIN)\n"
                + "2. Something you have (Smartphone / Authenticator App / Hardware Security Key)\n"
                + "3. Something you are (Fingerprint / Face ID)\n\n"
                + "Even if an attacker steals your password, they cannot access your account without the second factor. Prefer Authenticator Apps over SMS codes whenever possible."
        );

        registerFallback(
                new String[]{"suspicious email", "identify email", "fake email", "spot phishing", "email security"},
                "✉️ How to Identify a Suspicious Email:\n\n"
                + "1. Inspect the Sender's Full Address: Look closely at the domain name after '@' (e.g., support@paypa1-security.com instead of paypal.com).\n"
                + "2. Sense of Artificial Urgency: Threats of account suspension or urgent demands for action are classic warning signs.\n"
                + "3. Hover Over Links Before Clicking: Check where the URL really leads before clicking.\n"
                + "4. Unexpected Attachments: Beware of files ending in .zip, .exe, .scr, or macro-enabled documents (.docm).\n"
                + "5. Generic Greetings: Legitimate services usually address you by your real name rather than 'Dear Valued Customer'."
        );

        registerFallback(
                new String[]{"firewall", "packet filter", "network filter", "port blocking"},
                "🧱 What is a Firewall?\n\n"
                + "A firewall is a network security device or software program that monitors and filters incoming and outgoing network traffic based on an organization's previously established security policies.\n\n"
                + "• Analogy: Think of it as a security checkpoint or bouncer at the border of your network.\n"
                + "• Types: Host-based software firewalls (running on individual PCs) and network-based hardware firewalls (guarding entire subnets).\n"
                + "• Function: Blocks unauthorized incoming connections, closes unused ports, and prevents compromised devices from communicating with malicious command servers."
        );

        registerFallback(
                new String[]{"why is cybersecurity important", "importance of cybersecurity", "why cybersecurity", "cyber security important", "digital safety"},
                "💡 Why is Cybersecurity Important?\n\n"
                + "Cybersecurity protects sensitive data, computer networks, and critical digital infrastructure from unauthorized access, disruption, or destruction.\n\n"
                + "Key reasons it matters:\n"
                + "1. Protecting Personal Privacy: Prevents identity theft and personal data breaches.\n"
                + "2. Safeguarding Critical Systems: Hospitals, electrical grids, and transportation rely on cyber defenses.\n"
                + "3. Economic Resilience: Cyberattacks cost global economies trillions of dollars annually.\n"
                + "4. Trust in Digital Society: Ensures secure online education, commerce, and communication."
        );
    }

    private static void registerFallback(String[] keywords, String response) {
        FALLBACK_KNOWLEDGE_BASE.put(keywords, response);
    }

    /**
     * Initializes the AIService.
     * Resolves the API key and model from:
     * 1. config.properties (current folder)
     * 2. /config.properties (classpath resource)
     * 3. Environment variables (GEMINI_API_KEY, GEMINI_MODEL)
     */
    public AIService() {
        this.apiKey = resolveApiKey();
        this.modelName = resolveModelName();

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.gson = new Gson();
    }

    private String resolveApiKey() {
        // 1. Check local config.properties file
        String key = loadPropertyFromFile("config.properties", "GEMINI_API_KEY");
        if (isValid(key)) return key;

        // 2. Check classpath resource /config.properties
        key = loadPropertyFromClasspath("/config.properties", "GEMINI_API_KEY");
        if (isValid(key)) return key;

        // 3. Check environment variable
        key = System.getenv("GEMINI_API_KEY");
        if (isValid(key)) return key;

        // 4. Check system property
        key = System.getProperty("GEMINI_API_KEY");
        if (isValid(key)) return key;

        return null;
    }

    private String resolveModelName() {
        // 1. Check local config.properties file
        String model = loadPropertyFromFile("config.properties", "GEMINI_MODEL");
        if (isValid(model)) return model;

        // 2. Check classpath resource /config.properties
        model = loadPropertyFromClasspath("/config.properties", "GEMINI_MODEL");
        if (isValid(model)) return model;

        // 3. Check environment variable
        model = System.getenv("GEMINI_MODEL");
        if (isValid(model)) return model;

        // 4. Check system property
        model = System.getProperty("GEMINI_MODEL");
        if (isValid(model)) return model;

        return DEFAULT_MODEL;
    }

    private static String loadPropertyFromFile(String filePath, String propertyKey) {
        try {
            java.util.Properties props = com.cyberlab.util.ConfigFiles.load(filePath);
            String val = props.getProperty(propertyKey);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String loadPropertyFromClasspath(String resourcePath, String propertyKey) {
        try (InputStream is = AIService.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                String val = props.getProperty(propertyKey);
                if (val != null && !val.isBlank()) {
                    return val.trim();
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static boolean isValid(String val) {
        return val != null && !val.isBlank();
    }

    /**
     * Checks whether a valid Gemini API key is configured in the environment.
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Returns the name of the active Gemini model.
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sends the user's question asynchronously to the Gemini API.
     * If the API key is missing, internet is unavailable, or an API error occurs,
     * it gracefully falls back to predefined educational responses.
     *
     * @param userPrompt The user's question
     * @return A CompletableFuture with the assistant's answer
     */
    public CompletableFuture<String> sendMessageAsync(String userPrompt) {
        return CompletableFuture.supplyAsync(() -> sendMessage(userPrompt));
    }

    /**
     * Synchronously sends a message to Gemini or returns fallback if unavailable.
     */
    public String sendMessage(String userPrompt) {
        if (userPrompt == null || userPrompt.isBlank()) {
            return "Please type a cybersecurity question to get started.";
        }

        // Check if API key is present
        if (!isApiKeyConfigured()) {
            String fallback = findFallbackResponse(userPrompt);
            if (fallback != null) {
                return "[Offline Educational Mode]\n(Note: GEMINI_API_KEY environment variable is not set. Showing verified educational response)\n\n"
                        + fallback;
            }
            return "⚠️ Gemini API key not detected.\n\n"
                    + "To enable live AI answers, set the GEMINI_API_KEY environment variable.\n\n"
                    + "Meanwhile, you can ask about any of these built-in topics in offline mode:\n"
                    + "• What is phishing?\n"
                    + "• What is malware?\n"
                    + "• What is ransomware?\n"
                    + "• What is social engineering?\n"
                    + "• How do I create a strong password?\n"
                    + "• What is a VPN?\n"
                    + "• What is two-factor authentication (2FA)?\n"
                    + "• How can I identify a suspicious email?\n"
                    + "• What is a firewall?\n"
                    + "• Why is cybersecurity important?";
        }

        // Attempt live call to Gemini API
        try {
            return callGeminiApi(userPrompt);
        } catch (HttpTimeoutException e) {
            return handleFailureWithFallback(userPrompt, "Request timed out while contacting the Gemini AI service.");
        } catch (IOException e) {
            return handleFailureWithFallback(userPrompt, "Network connection error: Unable to reach the Gemini API. Check your internet connection.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return handleFailureWithFallback(userPrompt, "Request was interrupted.");
        } catch (Exception e) {
            return handleFailureWithFallback(userPrompt, "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Executes the HTTP request to the Gemini API.
     */
    private String callGeminiApi(String userPrompt) throws IOException, InterruptedException {
        String url = String.format(API_ENDPOINT_TEMPLATE, modelName) + "?key=" + apiKey.trim();

        // Build Gemini request payload according to the API specification
        JsonObject requestBody = new JsonObject();

        // System instruction for educational cybersecurity role
        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemTextPart = new JsonObject();
        systemTextPart.addProperty("text", SYSTEM_INSTRUCTION_TEXT);
        systemParts.add(systemTextPart);
        systemInstruction.add("parts", systemParts);
        requestBody.add("system_instruction", systemInstruction);

        // Contents (user prompt)
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        contentObj.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", userPrompt);
        parts.add(textPart);
        contentObj.add("parts", parts);
        contents.add(contentObj);
        requestBody.add("contents", contents);

        String jsonPayload = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return parseSuccessResponse(responseBody);
        } else {
            return parseErrorResponse(statusCode, responseBody, userPrompt);
        }
    }

    /**
     * Parses a successful response from Gemini and extracts the text answer.
     */
    private String parseSuccessResponse(String jsonResponse) {
        try {
            JsonObject root = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (root.has("candidates")) {
                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates.size() > 0) {
                    JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                    if (firstCandidate.has("content")) {
                        JsonObject content = firstCandidate.getAsJsonObject("content");
                        if (content.has("parts")) {
                            JsonArray parts = content.getAsJsonArray("parts");
                            if (parts.size() > 0) {
                                JsonElement textElem = parts.get(0).getAsJsonObject().get("text");
                                if (textElem != null) {
                                    return textElem.getAsString().trim();
                                }
                            }
                        }
                    }
                }
            }
            return "The AI responded, but no text content was found in the response.";
        } catch (Exception e) {
            return "Received an invalid or unparseable response from the Gemini API.";
        }
    }

    /**
     * Parses an API error code/message and provides a helpful educational fallback if available.
     */
    private String parseErrorResponse(int statusCode, String errorJson, String userPrompt) {
        String detailMessage = "";
        try {
            JsonObject root = JsonParser.parseString(errorJson).getAsJsonObject();
            if (root.has("error")) {
                JsonObject errObj = root.getAsJsonObject("error");
                if (errObj.has("message")) {
                    detailMessage = errObj.get("message").getAsString();
                }
            }
        } catch (Exception ignored) {
            detailMessage = errorJson;
        }

        String fallback = findFallbackResponse(userPrompt);
        StringBuilder sb = new StringBuilder();

        if (statusCode == 400) {
            sb.append("⚠️ API Request Error (400 Bad Request): ").append(detailMessage).append("\n\n");
        } else if (statusCode == 401 || statusCode == 403) {
            sb.append("⚠️ Authentication Error (").append(statusCode).append("): Your Gemini API key appears to be invalid or unauthorized.\n\n");
        } else if (statusCode == 404) {
            sb.append("⚠️ Model Not Found (404): The model '").append(modelName).append("' was not found or is unavailable for this key.\n\n");
        } else if (statusCode == 429) {
            sb.append("⚠️ Rate Limit Exceeded (429): You have sent too many requests. Please wait a moment.\n\n");
        } else if (statusCode >= 500) {
            sb.append("⚠️ Gemini Service Error (").append(statusCode).append("): The Gemini service is temporarily overloaded or unavailable.\n\n");
        } else {
            sb.append("⚠️ API Error (").append(statusCode).append("): ").append(detailMessage).append("\n\n");
        }

        if (fallback != null) {
            sb.append("📚 [Offline Fallback Answer Found]\n\n").append(fallback);
        } else {
            sb.append("Please try asking another cybersecurity question or verify your API configuration.");
        }

        return sb.toString();
    }

    /**
     * Helper for network/timeout errors with fallback educational content.
     */
    private String handleFailureWithFallback(String userPrompt, String technicalError) {
        String fallback = findFallbackResponse(userPrompt);
        if (fallback != null) {
            return "📡 " + technicalError + "\n\n"
                    + "📚 [Offline Educational Mode - Answer Provided Below]:\n\n"
                    + fallback;
        }
        return "📡 " + technicalError + "\n\nPlease check your internet connection and try again.";
    }

    /**
     * Searches for a matching educational response based on keywords.
     */
    public String findFallbackResponse(String userPrompt) {
        if (userPrompt == null) return null;
        String normalized = userPrompt.toLowerCase().trim();

        for (Map.Entry<String[], String> entry : FALLBACK_KNOWLEDGE_BASE.entrySet()) {
            for (String keyword : entry.getKey()) {
                if (normalized.contains(keyword.toLowerCase())) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns an unmodifiable list of supported offline fallback topics for educational display.
     */
    public static Map<String[], String> getFallbackKnowledgeBase() {
        return Collections.unmodifiableMap(FALLBACK_KNOWLEDGE_BASE);
    }
}
