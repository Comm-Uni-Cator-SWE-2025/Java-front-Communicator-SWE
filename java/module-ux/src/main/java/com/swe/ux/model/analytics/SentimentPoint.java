/**
 *  Contributed by Kishore.
 */

package com.swe.ux.model.analytics;

public class SentimentPoint {
    private final String time;
    private final double sentiment;

    public SentimentPoint(String time, double sentiment) {
        this.time = time;
        this.sentiment = sentiment;
    }

    public String getTime() {
        return time;
    }

    public double getSentiment() {
        return sentiment;
    }
}
