package org.groupweb.vscode;

import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class LanguageProcessor {

    // 多國語言美食詞庫 (用來給爬蟲算分 Scroring 使用)
    private final Map<String, List<String>> FOOD_KEYWORDS = new HashMap<>();

    public LanguageProcessor() {
        // English
        FOOD_KEYWORDS.put("en", Arrays.asList("food", "restaurant", "cuisine", "meal", "dining", "tasty", "delicious",
                "snack", "dessert", "drink", "coffee", "tea", "cake", "bread", "pizza", "burger", "barbecue", "recipe",
                "cook", "menu", "buffet", "street food", "eat", "bake", "spice", "seafood", "breakfast", "lunch", "dinner"));

        // Chinese (Traditional / Simplified)
        FOOD_KEYWORDS.put("zh", Arrays.asList("美食", "餐廳", "料理", "食物", "吃", "喝", "飲料", "甜點", "蛋糕", "咖啡", "晚餐",
                "午餐", "早餐", "夜市", "燒烤", "火鍋", "小吃", "烹飪", "食譜", "美味", "推薦", "必吃", "便當", "壽司", "拉麵",
                "牛排", "海鮮", "炸雞", "飲品", "下午茶"));

        // Japanese
        FOOD_KEYWORDS.put("ja", Arrays.asList("食べ物", "レストラン", "料理", "美味しい", "ご飯", "食事", "スイーツ", "ケーキ",
                "ラーメン", "寿司", "焼肉", "カフェ", "ドリンク", "居酒屋", "お菓子", "和食", "洋食", "飲み物", "コーヒー", "パン",
                "デザート", "グルメ", "ビュッフェ", "食堂", "弁当", "お好み焼き", "カレー", "天ぷら", "魚", "肉"));

        // Korean
        FOOD_KEYWORDS.put("ko", Arrays.asList("음식", "맛집", "요리", "식당", "식사", "맛있는", "커피", "음료", "디저트", "빵", "케이크",
                "바베큐", "한식", "중식", "일식", "양식", "분식", "라면", "불고기", "김치", "떡볶이", "치킨", "냉면", "밥", "고기", "술",
                "카페", "맛", "식사", "음식점"));

        // Spanish
        FOOD_KEYWORDS.put("es", Arrays.asList("comida", "restaurante", "cocina", "plato", "delicioso", "sabroso",
                "café", "té", "postre", "pastel", "pan", "pizza", "hamburguesa", "receta", "mariscos", "barbacoa",
                "carne", "pollo", "pescado", "ensalada", "bebida", "vino", "cerveza", "desayuno", "almuerzo", "cena"));

        // French
        FOOD_KEYWORDS.put("fr", Arrays.asList("nourriture", "restaurant", "cuisine", "plat", "délicieux", "repas",
                "boisson", "dessert", "pain", "vin", "bière", "café", "pâtisserie", "chocolat", "menu", "déjeuner",
                "dîner", "petit déjeuner", "recette", "poisson", "fromage", "gâteau", "crêpe", "boucherie", "fruit"));

        // German
        FOOD_KEYWORDS.put("de", Arrays.asList("essen", "restaurant", "küche", "gericht", "lecker", "mittagessen",
                "abendessen", "frühstück", "kaffee", "tee", "kuchen", "brot", "bier", "wein", "rezept", "speise",
                "dessert", "fisch", "fleisch", "wurst", "käse", "suppe", "salat", "pizza", "hamburger", "bäckerei"));

        // Arabic
        FOOD_KEYWORDS.put("ar", Arrays.asList("طعام", "مطعم", "وجبة", "أكل", "لذيذ", "شهي", "مشروب", "قهوة", "شاي",
                "حلويات", "خبز", "كعكة", "دجاج", "سمك", "لحم", "مطبوخ", "مشوي", "غداء", "عشاء", "فطور", "مطبوخ", "مائدة"));

        // Russian
        FOOD_KEYWORDS.put("ru", Arrays.asList("еда", "ресторан", "кухня", "вкусный", "обед", "ужин", "завтрак", "кофе",
                "чай", "десерт", "пирог", "торт", "пицца", "рецепт", "барбекю", "суп", "рыба", "мясо", "вино", "сыр", "хлеб"));

        // Italian
        FOOD_KEYWORDS.put("it", Arrays.asList("cibo", "ristorante", "cucina", "pasto", "piatto", "delizioso", "caffè",
                "vino", "pane", "pizza", "pasta", "gelato", "dolce", "pesce", "carne", "birra", "ricetta", "formaggio",
                "antipasto", "dessert", "colazione", "pranzo", "cena", "frutta", "verdura"));

        // Portuguese
        FOOD_KEYWORDS.put("pt", Arrays.asList("comida", "restaurante", "cozinha", "prato", "delicioso", "saboroso",
                "bebida", "café", "chá", "sobremesa", "bolo", "pão", "pizza", "carne", "peixe", "frango", "massa",
                "cerveja", "vinho", "receita", "peixe", "lanche"));

        // Hindi
        FOOD_KEYWORDS.put("hi", Arrays.asList("भोजन", "रेस्टोरेंट", "खाना", "स्वादिष्ट", "पेय", "कॉफी", "चाय", "नाश्ता",
                "दोपहर का भोजन", "रात का खाना", "मिठाई", "पकवान", "व्यंजन", "मसाला", "चावल", "सब्जी", "रोटी", "मांस", "करी"));

        // Thai
        FOOD_KEYWORDS.put("th", Arrays.asList("อาหาร", "ร้านอาหาร", "อร่อย", "กาแฟ", "ชา", "ขนม", "ของหวาน", "ข้าว",
                "ผัดไทย", "ต้มยำ", "หมูกระทะ", "บาร์บีคิว", "ซีฟู้ด", "ไก่ทอด", "กาแฟ", "ชาเย็น"));

        // Vietnamese
        FOOD_KEYWORDS.put("vi", Arrays.asList("ẩm thực", "nhà hàng", "đồ ăn", "ngon", "món ăn", "cà phê", "trà",
                "bánh", "phở", "bún", "cơm", "đồ uống", "thịt", "hải sản", "tráng miệng"));

        // Indonesian
        FOOD_KEYWORDS.put("id", Arrays.asList("makanan", "restoran", "lezat", "minuman", "kopi", "teh", "nasi",
                "ayam", "ikan", "daging", "kue", "mie", "sate", "resep", "seafood", "sarapan"));

        // Malay
        FOOD_KEYWORDS.put("ms", Arrays.asList("makanan", "restoran", "minuman", "kopi", "teh", "lazat", "sedap",
                "nasi", "ayam", "ikan", "mi", "kuih", "resepi", "sarapan", "makan malam"));

        // Dutch
        FOOD_KEYWORDS.put("nl", Arrays.asList("eten", "restaurant", "keuken", "gerecht", "lekker", "ontbijt",
                "lunch", "diner", "koffie", "thee", "taart", "brood", "bier", "wijn", "recept"));

        // Swedish
        FOOD_KEYWORDS.put("sv", Arrays.asList("mat", "restaurang", "kök", "rätt", "läcker", "kaffe", "te", "kaka",
                "bröd", "vin", "öl", "dessert", "recept", "middag", "lunch", "frukost"));

        // Finnish
        FOOD_KEYWORDS.put("fi", Arrays.asList("ruoka", "ravintola", "keittiö", "ateria", "herkullinen", "kahvi",
                "tee", "leipä", "kakku", "viini", "olut", "jälkiruoka", "resepti"));

        // Greek
        FOOD_KEYWORDS.put("el", Arrays.asList("φαγητό", "εστιατόριο", "κουζίνα", "νόστιμο", "ποτό", "καφές",
                "τσάι", "επιδόρπιο", "πίτσα", "ψάρι", "κρέας", "ψωμί", "κρασί", "μπύρα", "πρωινό"));

        // Turkish
        FOOD_KEYWORDS.put("tr", Arrays.asList("yemek", "restoran", "mutfak", "lezzetli", "kahve", "çay", "tatlı",
                "ekmek", "pizza", "balık", "et", "tavuk", "sebze", "meyve", "çorba", "pilav", "tarif"));

        // Polish
        FOOD_KEYWORDS.put("pl", Arrays.asList("jedzenie", "restauracja", "kuchnia", "smaczny", "kawa", "herbata",
                "ciasto", "chleb", "wino", "piwo", "zupa", "obiad", "śniadanie", "kolacja"));

        // Czech
        FOOD_KEYWORDS.put("cs", Arrays.asList("jídlo", "restaurace", "kuchyně", "chutné", "káva", "čaj", "koláč",
                "pivo", "víno", "polévka", "sýr", "ryba", "dezert", "recept"));

        // Hungarian
        FOOD_KEYWORDS.put("hu", Arrays.asList("étel", "étterem", "konyha", "ízletes", "kávé", "tea", "desszert",
                "kenyér", "leves", "hús", "hal", "pizza", "sör", "bor", "recept"));

        // Hebrew
        FOOD_KEYWORDS.put("he", Arrays.asList("אוכל", "מסעדה", "ארוחה", "טעים", "קפה", "תה", "עוגה", "לחם",
                "יין", "בירה", "דגים", "בשר", "קינוח", "תפריט"));

        // Romanian
        FOOD_KEYWORDS.put("ro", Arrays.asList("mâncare", "restaurant", "bucătărie", "gustos", "cafea", "ceai",
                "desert", "pâine", "vin", "bere", "pește", "carne", "rețetă"));

        // Ukrainian
        FOOD_KEYWORDS.put("uk", Arrays.asList("їжа", "ресторан", "кухня", "смачно", "кава", "чай", "торт", "хліб",
                "вино", "пиво", "риба", "м'ясо", "сир", "десерт"));

        // Danish
        FOOD_KEYWORDS.put("da", Arrays.asList("mad", "restaurant", "køkken", "lækker", "kaffe", "te", "kage", "brød",
                "vin", "øl", "fisk", "kød", "ost", "dessert", "recept"));

        // Norwegian
        FOOD_KEYWORDS.put("no", Arrays.asList("mat", "restaurant", "kjøkken", "velsmakende", "kaffe", "te", "kake",
                "brød", "vin", "øl", "fisk", "kjøtt", "ost", "dessert"));

        // Tagalog / Filipino
        FOOD_KEYWORDS.put("tl", Arrays.asList("pagkain", "restawran", "masarap", "inumin", "kape", "tsaa", "tinapay",
                "isda", "karne", "kanin", "hapunan", "almusal", "tanghalian", "meryenda"));
    }

    /**
     * Detect language roughly based on Unicode range or letter patterns.
     */
    public String detectLanguage(String text) {
        if (text == null || text.isEmpty()) return "en";
        if (text.matches(".*[\\u4E00-\\u9FFF].*")) return "zh";
        if (text.matches(".*[\\u3040-\\u30FF].*")) return "ja";
        if (text.matches(".*[\\uAC00-\\uD7AF].*")) return "ko";
        if (text.matches(".*[\\u0600-\\u06FF].*")) return "ar";
        if (text.matches(".*[а-яА-Я].*")) return "ru";
        if (text.matches(".*[áéíóúñü].*")) return "es";
        if (text.matches(".*[àâçéèêëîïôûùüÿœ].*")) return "fr";
        return "en"; // Default fallback
    }

    /**
     * Returns food-related keywords based on detected language
     * (用於爬蟲算分 Scoring)
     */
    public List<String> getFoodKeywords(String lang) {
        return FOOD_KEYWORDS.getOrDefault(lang, FOOD_KEYWORDS.get("en"));
    }
}