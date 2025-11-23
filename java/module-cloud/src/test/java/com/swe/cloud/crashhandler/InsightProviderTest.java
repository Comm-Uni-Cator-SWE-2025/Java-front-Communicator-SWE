package crashhandler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InsightProviderTest {

    @Test
    void testInsightProvider() {
        final InsightProvider insightProvider = new InsightProvider();

        String response = insightProvider.getInsights("null data, Do not generate anything");
    }

}