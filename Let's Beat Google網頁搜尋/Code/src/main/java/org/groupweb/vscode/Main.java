package org.groupweb.vscode;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);

        // Define Keywords
        List<Keyword> keywords = new ArrayList<>();
        keywords.add(new Keyword("food", 1.5));
        keywords.add(new Keyword("restaurant", 2.0));
        keywords.add(new Keyword("delicious", 1.2));
        keywords.add(new Keyword("cuisine", 1.0));



    }
}
/*各段原本在BmiApplicationTests.java中，不確定會不會用上，所以先丟這
    import org.junit.jupiter.api.Test;
    import org.springframework.boot.test.context.SpringBootTest;

    @SpringBootTest
    class BmiApplicationTests {

        @Test
        void contextLoads() {
        }

    }
*/