package com.priyansu.workflow.service.ai;

import com.priyansu.workflow.execution.WorkflowContext;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PromptTemplateService {

    //It finds:{{somethingText}} inside prompt. Then replaces with actual workflow data. [[we can pass prompt like(input.text)=> input: text:} inside /execute endpoint's Body]]
    //ex:Template: Summarize: {{input.text}}   Result:  Summarize: Kafka is distributed...
    public String render(String template, WorkflowContext context) {

        Pattern pattern = Pattern.compile("\\{\\{(.*?)}}");

        Matcher matcher = pattern.matcher(template);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {

            String key = matcher.group(1).trim();

            Object value = resolveValue(key, context);

            matcher.appendReplacement(
                    result,
                    value != null
                            ? Matcher.quoteReplacement(value.toString())
                            : ""
            );
        }

        matcher.appendTail(result);

        return result.toString();
    }


    //Supports nested values:{{input.text}}, {{user.email}}, {{invoice.amount}}
    private Object resolveValue(String path, WorkflowContext context) {

        String[] parts = path.split("\\.");

        Object current = context.get(parts[0]);

        for (int i = 1; i < parts.length; i++) {

            if (current instanceof Map<?, ?> map) {

                current = map.get(parts[i]);

            } else if (current instanceof JsonNode jsonNode) {

                JsonNode child = jsonNode.get(parts[i]);

                if (child == null || child.isNull()) {
                    return null;
                }

                // keep JsonNode if it's an object
                if (child.isObject()) {
                    current = child;
                } else {
                    current = child.asText();
                }

            } else {
                return null;
            }
        }

        return current;
    }
}
