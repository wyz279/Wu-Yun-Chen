package org.groupweb.vscode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;

@RestController
public class SearchController {

    private final SearchService searchService;
    private final LlmService llmService;             // 1. 改用 LlmService
    private final LanguageProcessor languageProcessor; // 用來拿通用的英文食物詞庫

    @Autowired
    public SearchController(SearchService searchService, 
                            LlmService llmService,
                            LanguageProcessor languageProcessor) {
        this.searchService = searchService;
        this.llmService = llmService;
        this.languageProcessor = languageProcessor;
    }

    /**
     * 一般搜尋 (不爬蟲)
     */
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam(name = "q") String q) {
        if (q == null || q.trim().isEmpty()) return Collections.emptyList();

        // 1. LLM 翻譯：把 "火鍋" -> "Hot Pot"
        String translatedQuery = llmService.translateToEnglishSearchTerm(q);
        System.out.println("DEBUG: LLM 翻譯 [" + q + "] -> [" + translatedQuery + "]");

        // 2. 用英文去搜並組裝前端需要的欄位
        List<Map<String, String>> rawResults = searchService.search(translatedQuery);
        List<Map<String, Object>> enriched = new ArrayList<>();

        for (int i = 0; i < rawResults.size(); i++) {
            Map<String, String> item = rawResults.get(i);
            Map<String, Object> dto = new LinkedHashMap<>();

            dto.put("title", item.getOrDefault("title", ""));
            dto.put("link", item.getOrDefault("link", ""));
            dto.put("snippet", item.getOrDefault("snippet", ""));
            dto.put("date", item.getOrDefault("date", ""));

            double score = Math.max(0.0, 100.0 - i * 2.5); // 簡單排名分數
            dto.put("score", score);

            enriched.add(dto);
        }

        return enriched;
    }

    /**
     * 進階搜尋 (爬蟲 + 算分)
     */
    @GetMapping("/crawlSearch")
    public List<Map<String, Object>> crawlSearch(
            @RequestParam(name = "q") String q,
            @RequestParam(name = "depth", defaultValue = "2") int depth) {

        if (q == null || q.trim().isEmpty()) return Collections.emptyList();

        try {
            // ==========================================
            // Step 1: LLM 翻譯 (核心修改)
            // ==========================================
            // 把使用者的輸入 (不管是中文、印尼文) 通通轉成英文關鍵字
            String translatedQuery = llmService.translateToEnglishSearchTerm(q);
            System.out.println("DEBUG: LLM 翻譯 [" + q + "] -> [" + translatedQuery + "]");
            
            
            // ==========================================
            // Step 2: Google 搜尋 (只用核心翻譯字)
            // ==========================================
            // 邏輯：LLM 回傳 "核心字 | 擴充字"，我們搜尋時只用 "核心字"，避免條件太嚴格搜不到
            String[] sections = translatedQuery.split("\\|");
            String coreQuery = sections[0].trim(); // 只拿 "NCCU Hot Pot"

            System.out.println("DEBUG: Google 搜尋關鍵字: [" + coreQuery + "]");
            
           
            Map<String, String> googleResults = searchService.searchAsMap(coreQuery);
            
            if (googleResults == null || googleResults.isEmpty()) {
                return Collections.emptyList();
            }
            // 取得第一筆網址當作爬蟲起點
            String seedUrl = googleResults.values().iterator().next(); 
          
           // ==========================================
// Step 3: 建立「評分用」的關鍵字列表 (修正版)
// ==========================================
List<Keyword> scoringKeywords = new ArrayList<>();

// 3.1 處理 LLM 翻譯的核心字 (例如 "NCCU Hot Pot") -> 權重給最高 5.0
// ★★★ 關鍵修正：拆開字串，讓 "NCCU" 和 "Hot" 可以分開算分 ★★★
String[] coreTokens = translatedQuery.split("\\s+"); // 用空白切割
for (String token : coreTokens) {
    // 排除太短的字 (如 "a", "the")
    if (token.length() > 2) { 
        scoringKeywords.add(new Keyword(token, 5.0)); // 核心字給 5 分
    }
}

// 3.2 加入 Google 相關搜尋詞，權重 1.0
GoogleQuery googleQuery = new GoogleQuery();
List<String> relatedKeywords = googleQuery.fetchRelatedKeywords(translatedQuery);
System.out.println("DEBUG: Google 相關搜尋詞 -> " + relatedKeywords);
    if (relatedKeywords != null) {
        for (String relatedWord : relatedKeywords) {
            scoringKeywords.add(new Keyword(relatedWord, 1.0));
        }
        }

// 3.2 處理通用美食字 (例如 "delicious") -> 權重給低一點 1.0
// 你的 LanguageProcessor 已經有這個功能，保留即可
List<String> enFoodWords = languageProcessor.getFoodKeywords("en");
if (enFoodWords != null) {
    for (String word : enFoodWords) {
        scoringKeywords.add(new Keyword(word, 1.0)); // 擴充字給 1 分
    }
}

            // ==========================================
            // Step 4: 執行爬蟲與評分
            // ==========================================
            // 傳入的是英文網址 (seedUrl) 和 英文關鍵字 (scoringKeywords)
            return searchService.searchWithCrawl(translatedQuery, seedUrl, depth, scoringKeywords);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}