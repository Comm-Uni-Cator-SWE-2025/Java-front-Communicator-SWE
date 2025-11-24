/*
 * Contributed by Kishore.
 */

package com.swe.ux.model.analytics;

/**
 * Represents a sentiment data point with time and sentiment value.
 */
public class SentimentPoint {

    /** The time stamp. */
    private final String time;

    /** The sentiment value. */
    private final double sentiment;

    /**
     * Creates a new sentiment point.
     *
     * @param timeStamp the time stamp
     * @param sentimentValue the sentiment value
     */
    public SentimentPoint(final String timeStamp, final double sentimentValue) {
        this.time = timeStamp;
        this.sentiment = sentimentValue;
    }

    /**
     * Gets the time stamp.
     *
     * @return the time stamp
     */
    public String getTime() {
        return time;
    }

    /**
     * Gets the sentiment value.
     *
     * @return the sentiment value
     */
    public double getSentiment() {
        return sentiment;
    }
}
