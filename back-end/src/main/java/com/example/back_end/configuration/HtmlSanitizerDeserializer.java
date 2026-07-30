package com.example.back_end.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

/**
 * Tự động làm sạch dữ liệu văn bản (HTML/XSS Sanitizer) được gửi lên dạng JSON.
 * Lớp này được tự động đăng ký với Jackson ObjectMapper nhờ
 * annotation @JsonComponent.
 * Nó loại bỏ hoàn toàn các thẻ HTML độc hại trong các trường chuỗi ký tự (String)
 * nhưng cho phép các thẻ định dạng văn bản an toàn kèm CSS style cho các trường Rich Text (như feedback, description, notes).
 * Bỏ qua hoàn toàn mật khẩu hoặc token.
 */
@JsonComponent
public class HtmlSanitizerDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null) {
            return null;
        }
        String fieldName = p.getCurrentName();
        if (fieldName != null) {
            String lowerField = fieldName.toLowerCase();
            if (lowerField.contains("password")
                    || lowerField.contains("token")
                    || lowerField.contains("secret")
                    || lowerField.contains("key")) {
                return value;
            }

            // Cho phép thẻ định dạng và inline CSS style cho các trường rich text như lời nhận xét, mô tả, ghi chú
            if (lowerField.contains("feedback")
                    || lowerField.contains("description")
                    || lowerField.contains("notes")) {
                Safelist richTextSafelist = Safelist.relaxed()
                        .addTags("div", "span", "u", "strike")
                        .addAttributes("span", "style")
                        .addAttributes("p", "style")
                        .addAttributes("div", "style")
                        .addAttributes("strong", "style")
                        .addAttributes("em", "style")
                        .addAttributes("u", "style")
                        .addAttributes("strike", "style");
                return Jsoup.clean(value, richTextSafelist);
            }
        }
        return Jsoup.clean(value, Safelist.none());
    }
}
