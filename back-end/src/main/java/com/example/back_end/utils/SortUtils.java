package com.example.back_end.utils;

import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SortUtils {
    public static Sort parseSort(String[] sorts) {
        if (sorts == null || sorts.length == 0) {
            return Sort.unsorted();
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
        
        for (String sortBy : sorts) {
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String fieldName = matcher.group(1);
                String direction = matcher.group(3);
                orders.add(new Sort.Order(
                    direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                    fieldName
                ));
            }
        }
        return Sort.by(orders);
    }
}
