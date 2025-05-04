package com.example.breeddetectorapp.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {

    @SerializedName("predicted_breed")
    private String predictedBreed;

    @SerializedName("detected_breed")
    private String detectedBreed;

    @SerializedName("match")
    private String match;

    @SerializedName("responses")
    private Responses responses;

    public String getPredictedBreed() {
        return predictedBreed;
    }

    public String getDetectedBreed() {
        // Lấy detected_breed từ "final" trong responses
        if (responses != null && responses.finalResponse != null) {
            Responses.FinalResponse finalResp = responses.finalResponse;
            String detectedBreed = finalResp.detectedBreed; // Lấy detected_breed từ finalResponse
            if (detectedBreed != null && !detectedBreed.isEmpty()) {
                return detectedBreed;
            }
        }
        // Trả về thông báo mặc định nếu không có detected_breed
        return "Không xác định giống.";
    }

    public String getMatch() {
        // Lấy match từ "final" trong responses
        if (responses != null && responses.finalResponse != null) {
            Responses.FinalResponse finalResp = responses.finalResponse;
            String match = finalResp.match; // Lấy match từ finalResponse
            if (match != null && !match.isEmpty()) {
                return match;
            }
        }
        // Trả về thông báo mặc định nếu không có match
        return "Không xác định";
    }

    public String getAnswer() {
        // Lấy kết quả từ "final" trong responses
        if (responses != null && responses.finalResponse != null) {
            Responses.FinalResponse finalResp = responses.finalResponse;
            String answer = finalResp.answer;
            String description = finalResp.description;

            // Kiểm tra và trả về answer hoặc description, nếu có
            if (answer != null && !answer.isEmpty()) {
                return answer;
            } else if (description != null && !description.isEmpty()) {
                return description;
            }
        }
        // Trả về thông báo mặc định nếu không có mô tả
        return "Không có mô tả chi tiết.";
    }

    public static class Responses {
        @SerializedName("final")
        private FinalResponse finalResponse;

        public FinalResponse getFinalResponse() {
            return finalResponse;
        }

        public static class FinalResponse {
            @SerializedName("answer")
            public String answer;

            @SerializedName("description")
            public String description;

            @SerializedName("match")
            public String match;  // Thêm match ở đây

            @SerializedName("detected_breed")
            public String detectedBreed;  // Thêm detected_breed ở đây
        }
    }
}
