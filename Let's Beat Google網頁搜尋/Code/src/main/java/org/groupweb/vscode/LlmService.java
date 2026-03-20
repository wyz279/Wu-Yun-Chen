package org.groupweb.vscode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class LlmService {

    @Value("${gemini.api.key}") 
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    public String translateToEnglishSearchTerm(String userInput) {
        // 1. 基本防呆
        if (userInput == null || userInput.trim().isEmpty()) return "food";

        // 使用 gemini-2.5-flash (目前最穩定且免費)
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // 2. 設定 Prompt：教 AI 區分亂碼、非食物和食物
        String prompt = "You are a smart query optimizer for a food search engine. Analyze the input: '" + userInput + "'.\n" +
                        "Follow these rules strictly:\n" +
                        
                        // Rule 1: 正常翻譯 + 處理空格 + 去除雜訊
                        "1. **Translation & Cleaning**: Translate the input into English.\n" +
                        "   - If input has spaces (e.g., '政大 火鍋'), translate all parts (e.g., 'NCCU Hot Pot').\n" +
                        "   - IGNORE random numbers or symbols mixed in text (e.g., '火鍋888' -> 'Hot Pot').\n" +

                        // Rule 2: 偷塞關鍵字 (Expansion) - 對所有結果都適用
                        "2. **Expansion**: ALWAYS append 2-3 high-quality English keywords like 'delicious', 'restaurant', 'best', or 'menu' to the end.\n" +

                        // Rule 3: 非食物處理
                        "3. **Non-Food**: If input is meaningful but NOT food-related (e.g., 'Tesla'), translate it and append 'food' (e.g., 'Tesla food').\n" +

                        // ★★★ Rule 4: 亂碼時隨機生成食物 ★★★
                        "4. **Gibberish/Random**: If input is pure gibberish, random characters, or just numbers (e.g., 'asdf', '12345'), **IGNORE the input and randomly pick ONE popular food name** in English (e.g., 'Pizza', 'Burger', 'Sushi', 'Ramen', 'Tacos').\n" +
                        "   - Example: Input 'asdfg' -> Output 'Burger delicious restaurant'.\n" +
                      

                        "Output ONLY the final space-separated English string, no explanation.";

        // 3. 建構 Request JSON
        Map<String, Object> requestBody = Map.of(
            "contents", List.of(
                Map.of("parts", List.of(
                    Map.of("text", prompt)
                ))
            )
        );

        try {
            // 4. 發送請求
            Map response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            // 5. 解析回傳資料
            // 結構: candidates[0] -> content -> parts[0] -> text
            List<Map> candidates = (List<Map>) response.get("candidates");
            Map content = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            return text.trim();

        } catch (Exception e) {
            e.printStackTrace();
            // 如果 API 失敗 (例如斷網或額度滿)，回傳安全值 "food" 避免程式崩潰
            return "food"; 
        }
    }
}