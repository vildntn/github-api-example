package org.githubApiExp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GitHubAPIExample {
    public static void main(String[] args) {
        // GitHub Personal Access Token
        String personalAccessToken = "your_personal_access_token";

        // Kullanıcı adı
        String username = "username";

        // GitHub GraphQL API URL
        String apiUrl = "https://api.github.com/graphql";

        // GraphQL sorgusu
        String query = """
                query($username: String!) {
                    user(login: $username) {
                        repositories(first: 10, orderBy: {field: CREATED_AT, direction: DESC}) {
                            nodes {
                                name
                                createdAt
                                url
                            }
                        }
                    }
                }
                """;

        // GraphQL değişkenleri
        String variables = """
                {
                    "username": "%s"
                }
                """.formatted(username);

        // POST isteği için JSON gövdesi
        String requestBody = """
                {
                    "query": %s,
                    "variables": %s
                }
                """.formatted(
                escapeJsonString(query),
                variables
        );

        try {
            // HttpClient ile istek oluşturma
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + personalAccessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // İstek gönderme ve yanıt alma
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            // JSON yanıtı işleme
            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                JsonArray repositories = jsonResponse
                        .getAsJsonObject("data")
                        .getAsJsonObject("user")
                        .getAsJsonObject("repositories")
                        .getAsJsonArray("nodes");

                System.out.println("Son oluşturulan depolar:");
                for (var element : repositories) {
                    JsonObject repository = element.getAsJsonObject();
                    System.out.printf("- %s (Oluşturma Tarihi: %s)\n",
                            repository.get("name").getAsString(),
                            repository.get("createdAt").getAsString());
                }
            } else {
                System.err.println("API isteği başarısız oldu: " + response.statusCode());
                System.err.println("Hata mesajı: " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    // JSON içindeki özel karakterleri kaçış karakteriyle değiştirme
    private static String escapeJsonString(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n");
    }
}