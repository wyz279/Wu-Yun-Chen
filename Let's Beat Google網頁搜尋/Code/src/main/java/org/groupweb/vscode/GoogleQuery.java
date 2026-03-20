package org.groupweb.vscode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

@Service
public class GoogleQuery {

    private final RestTemplate restTemplate;

    @Value("${google.cse.apiKey:}")
    private String apiKey;

    @Value("${google.cse.cx:}")
    private String cx;

    public GoogleQuery() {
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, String>> queryByApiWithDetails(String query) {
        List<Map<String, String>> results = new ArrayList<>();
        if (apiKey == null || apiKey.isEmpty() || cx == null || cx.isEmpty()) return results;

        try {
            
            String q = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
            String apiUrl = "https://www.googleapis.com/customsearch/v1?key=" + apiKey +
                    "&cx=" + cx + "&num=10&q=" + q + "&hl=auto";

            ResponseEntity<Map<String, Object>> resp =
                    restTemplate.getForEntity(apiUrl, (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> body = resp.getBody();
            if (body == null) return results;

            Object itemsObj = body.get("items");
            if (itemsObj instanceof List<?>) {
                for (Object itemObj : (List<?>) itemsObj) {
                    if (itemObj instanceof Map<?, ?>) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;

                        String rawTitle = String.valueOf(itemMap.get("title") != null ? itemMap.get("title") : "");
                        String link = String.valueOf(itemMap.get("link") != null ? itemMap.get("link") : "");
                        String rawSnippet = String.valueOf(itemMap.get("snippet") != null ? itemMap.get("snippet") : "");
                        String date = "";

                        // ★★★ 這裡修正亂碼 ★★★
                        String title = rawTitle;
                        String snippet = rawSnippet;
                        try {
                            title = URLDecoder.decode(rawTitle, StandardCharsets.UTF_8);
                            snippet = URLDecoder.decode(rawSnippet, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            // 解碼失敗就用原本的
                        }

                        // 抓日期
                        Object pagemap = itemMap.get("pagemap");
                        if (pagemap instanceof Map<?, ?> pagemapMap) {
                            Object metatagsObj = pagemapMap.get("metatags");
                            if (metatagsObj instanceof List<?> metatagsList && !metatagsList.isEmpty()) {
                                Object meta0 = metatagsList.get(0);
                                if (meta0 instanceof Map<?, ?> metaMap) {
                                    Object time = metaMap.get("article:published_time");
                                    if (time != null) date = String.valueOf(time);
                                }
                            }
                        }

                        if (!title.isEmpty() && !link.isEmpty()) {
                            Map<String, String> resultItem = new LinkedHashMap<>();
                            resultItem.put("title", title);
                            resultItem.put("link", link);
                            resultItem.put("snippet", snippet);
                            if (!date.isEmpty()) resultItem.put("date", date);
                            results.add(resultItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
    
    // ... (後面 fetchRelatedKeywords 等方法保留原本的即可) ...
    public List<String> fetchRelatedKeywords(String query) {
        List<String> related = new ArrayList<>();
        try {
            String content = fetchContent(query);
            Document doc = Jsoup.parse(content);
            Elements items = doc.select("a.K8tyEc");
            for (Element e : items) {
                String text = e.text();
                if (text != null && !text.isEmpty()) related.add(text);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return related;
    }
    private String fetchContent(String query) throws Exception {
        String encodeKeyword = URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        String url = "https://www.google.com/search?q=" + encodeKeyword + "&oe=utf8&num=20";
        return Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get().html();
    }
    public List<String> getRelatedSuggestions(String query) { return Collections.emptyList(); } // 簡化版
}