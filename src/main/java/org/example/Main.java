package org.example;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;

public class Main {
    private final static String URL_OLLAMA = "http://localhost:11434";

    private final static String LLM_LLAMA_3_2_VISION = "llama3.2-vision:latest";
    private final static String LLM_MISTRAL = "mistral";
    private final static String OCR_PROMPT = """
        Act as an OCR assistant. Analyze the provided image to do requirements:
        - The image is a French medical prescription
        - Recognize and Extract all visible text in the image as accurately as possible without any additional explanations or comments.
        - Ensure that the extracted text is organized and presented in a structured Markdown format. 
        - Pay close attention to maintaining the original hierarchy and formatting, includeing any headings, subheadings, lists, tables or inline text. 
        - If any text elements are ambiguouse or partally readable, include them with appropriate notes or markers, such as [illegible]. 
        - Preserve the spatial relationships where applicable by mimiching the document layout in Markdown.
        - Don't omit any part of the page including headers, footers, tables, and subtext.
        Provide only the transcription without any additional comments.""";

    private final String TEST_1 = "e-prescription.jpg";
    private final String TEST_2 = "e-prescription_2.1.00.jpg";
    private final String TEST_3 = "e-prescription_2.1.01.jpg";

    public static void main(String[] args) throws IOException {

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(URL_OLLAMA)
                .temperature(0.0)
                .logRequests(true)
                .logResponses(true)
                .modelName(LLM_LLAMA_3_2_VISION)
                .build();

        byte[] imageBytes = Files.readAllBytes(Path.of("./e-prescription_2.1.00.jpg"));
        String base64Data = Base64.getEncoder().encodeToString(imageBytes);

        UserMessage userMessage = UserMessage.from(
                TextContent.from(OCR_PROMPT),
                ImageContent.from(base64Data, "image/jpg")
        );
        ChatResponse response = model.chat(userMessage);

        System.out.println("###### OCR ######");
        System.out.println(response.aiMessage().text());
        System.out.println("#################");

        System.out.println();

        formatOrdonnance(response.aiMessage().text());
    }



    private static void formatOrdonnance(String text) {
        record Ordonnance(String nomPatient, String prenomPatient, List<String> medicaments, List<String> posologies) {
        }

        interface OrdonnanceExtractor {
            Ordonnance extractPersonFrom(String text);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(URL_OLLAMA)
                .modelName(LLM_MISTRAL)
                .temperature(0.0)
                .supportedCapabilities(RESPONSE_FORMAT_JSON_SCHEMA)
                .logRequests(true)
                .build();

        OrdonnanceExtractor personExtractor = AiServices.create(OrdonnanceExtractor.class, chatModel);

        Ordonnance ordonnance = personExtractor.extractPersonFrom(text);
        System.out.println("###### ORDONNANCE ######");
        System.out.println(ordonnance);
        System.out.println("########################");
    }
}