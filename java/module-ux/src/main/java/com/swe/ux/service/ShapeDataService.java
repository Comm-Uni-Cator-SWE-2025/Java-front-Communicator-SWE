package com.swe.ux.service;

import com.swe.ux.model.analytics.ShapeCount;

import java.util.ArrayList;
import java.util.List;

public class ShapeDataService {
    private static final List<String> jsonList = new ArrayList<>();
    private int currentIndex = 0;

    static {
        jsonList.add("""
        {
          "freeHand": 12,
          "straightLine": 5,
          "rectangle": 3,
          "ellipse": 2,
          "triangle": 4
        }
        """);

        jsonList.add("""
        {
          "freeHand": 15,
          "straightLine": 8,
          "rectangle": 6,
          "ellipse": 4,
          "triangle": 7
        }
        """);

        jsonList.add("""
        {
          "freeHand": 10,
          "straightLine": 12,
          "rectangle": 5,
          "ellipse": 3,
          "triangle": 6
        }
        """);

        jsonList.add("""
        {
          "freeHand": 18,
          "straightLine": 6,
          "rectangle": 9,
          "ellipse": 5,
          "triangle": 8
        }
        """);

        jsonList.add("""
        {
          "freeHand": 14,
          "straightLine": 10,
          "rectangle": 7,
          "ellipse": 6,
          "triangle": 5
        }
        """);

        jsonList.add("""
        {
          "freeHand": 20,
          "straightLine": 9,
          "rectangle": 8,
          "ellipse": 7,
          "triangle": 10
        }
        """);
    }

    public String fetchNextData() {
        String response = jsonList.get(currentIndex);
        currentIndex = (currentIndex + 1) % jsonList.size();
        return response;
    }

    public ShapeCount parseJson(String json) {
        try {
            int freeHand = extractValue(json, "freeHand");
            int straightLine = extractValue(json, "straightLine");
            int rectangle = extractValue(json, "rectangle");
            int ellipse = extractValue(json, "ellipse");
            int triangle = extractValue(json, "triangle");

            return new ShapeCount(freeHand, straightLine, rectangle, ellipse, triangle);
        } catch (Exception e) {
            System.out.println("Error parsing shape data: " + e.getMessage());
            return new ShapeCount(0, 0, 0, 0, 0);
        }
    }

    private int extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return 0;
        
        startIndex += searchKey.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        
        String valueStr = json.substring(startIndex, endIndex).trim();
        return Integer.parseInt(valueStr);
    }
}
