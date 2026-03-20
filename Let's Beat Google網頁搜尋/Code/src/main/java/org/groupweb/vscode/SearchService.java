package org.groupweb.vscode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; WebSearcher/1.0)";
    private static final int TIMEOUT_MS = 5000;
    private static final int MAX_LINKS_PER_PAGE = 8;

    // optional web tree root (may be null)
    public WebNode root;

    // optional GoogleQuery bean (if present)
    @Autowired(required = false)
    private GoogleQuery googleQuery;

    @Autowired(required = false)
    private LanguageProcessor languageProcessor;

    // default constructor for Spring
    public SearchService() { }

    public SearchService(WebNode root) {
        this.root = root;
    }

    public void setRoot(WebNode root) {
        this.root = root;
    }

    /**
     * ✅ API 搜尋（使用 Google Custom Search API）
     * 回傳每筆包含 title, link, snippet, date
     */
    public List<Map<String, String>> search(String q) {
        if (q == null || q.trim().isEmpty()) return Collections.emptyList();
        if (googleQuery == null) return Collections.emptyList();

        try {
            return googleQuery.queryByApiWithDetails(q);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * ✅ 將 List<Map<String,String>> 轉成 Map<title, link>
     * Controller 用這個版本。
     */
    public Map<String, String> searchAsMap(String q) {
        List<Map<String, String>> list = search(q);
        Map<String, String> map = new LinkedHashMap<>();
        for (Map<String, String> item : list) {
            String title = item.get("title");
            String link = item.get("link");
            if (title != null && link != null) {
                map.put(title, link);
            }
        }
        return map;
    }

    /**
     * ✅ 進階搜尋：從 Google 結果開始爬網頁，計算節點分數並排序
     */
    public List<Map<String, Object>> searchWithCrawl(String query, String seedUrl, int maxDepth, List<Keyword> keywords) {
        if (seedUrl == null || seedUrl.isEmpty()) return Collections.emptyList();

        WebPage seedPage = new WebPage(seedUrl, seedUrl);
        WebNode rootNode = new WebNode(seedPage);
        WebTree tree = new WebTree(rootNode);

        Set<String> visited = new HashSet<>();
        visited.add(seedUrl);

        // 遞迴擴展節點
        crawlNode(rootNode, 0, maxDepth, keywords, visited);

        // 計算節點分數
        try {
            rootNode.setNodeScore(new ArrayList<>(keywords));
        } catch (IOException e) {
            // ignore scoring errors
        }

        // 排序並回傳
        return rankAndConvert(tree);
    }

    // ================= Helper Methods =================

    private void crawlNode(WebNode node, int depth, int maxDepth, List<Keyword> keywords, Set<String> visited) {
        if (depth >= maxDepth) return;

        try {
            Document doc = Jsoup.connect(node.webPage.url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .ignoreContentType(true)
                    .get();

            Elements links = doc.select("a[href]");
            int added = 0;
            for (Element link : links) {
                if (added >= MAX_LINKS_PER_PAGE) break;

                String href = link.absUrl("href");
                if (href == null || href.isEmpty()) continue;
                if (visited.contains(href)) continue;

                visited.add(href);
                String title = link.text();
                if (title == null || title.isEmpty()) title = href;

                WebPage childPage = new WebPage(href, title);
                WebNode childNode = new WebNode(childPage);
                node.addChild(childNode);
                added++;

                crawlNode(childNode, depth + 1, maxDepth, keywords, visited);
            }
        } catch (Exception e) {
            // ignore unreachable pages
        }
    }

    private List<Map<String, Object>> rankAndConvert(WebTree tree) {
        List<Map<String, Object>> results = new ArrayList<>();
        collectNodeScores(tree.root, results);
        results.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return results;
    }

    private void collectNodeScores(WebNode node, List<Map<String, Object>> results) {
        if (node == null) return;
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("url", node.webPage.url);
        item.put("title", node.webPage.name);
        item.put("score", node.nodeScore);
        results.add(item);

        for (WebNode child : node.children) {
            collectNodeScores(child, results);
        }
    }
}
