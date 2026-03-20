package org.groupweb.vscode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FoodQueryNormalizer {

    @Autowired
    private LanguageProcessor languageProcessor;

    public String normalizeToFood(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return "美食";
        }
        
        // 移除特殊符號，只留文字
        String cleaned = userInput.replaceAll("[^\\p{L}\\p{N}\\s]+", "").trim();

        // 偵測語言
        String lang = languageProcessor.detectLanguage(cleaned);
        
        // ★★★ 關鍵：只加一個詞，不要加一長串 ★★★
        String suffix = "";
        switch (lang) {
            case "zh": suffix = " 美食"; break;
            case "ja": suffix = " グルメ"; break;
            case "ko": suffix = " 맛집"; break;
            default:   suffix = " food"; break;
        }

        return cleaned + suffix;
    }
}