package org.groupweb.vscode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WordCounter {

    // attributes
    public WebNode root;

    // constructor
    public WordCounter(WebNode root) {
        this.root = root;
    }

    // Compute score based on keyword occurrences in page content.
    public double computeScore(ArrayList<Keyword> keywords) throws IOException {
        if (root == null || root.webPage == null || root.webPage.url == null) return 0.0;

        // Fetch page content
        Document doc = Jsoup.connect(root.webPage.url)
                .userAgent("Mozilla/5.0 (compatible; WebCrawler/1.0)")
                .timeout(5000)
                .get();

        String text = doc.text().toLowerCase(Locale.ROOT);
        double score = 0.0;

        if (keywords != null) {
            for (Keyword k : keywords) {
                if (k == null || k.getName() == null) continue;
                String key = k.getName().toLowerCase(Locale.ROOT).trim();
                if (key.isEmpty()) continue;
                int count = countOccurrences(text, key);
                score += count * k.getWeight();
            }
        }

        return score;
    }

    // simple substring occurrence counter
    private int countOccurrences(String text, String token) {
        int idx = 0, cnt = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            cnt++;
            idx += token.length();
        }
        return cnt;
    }
}