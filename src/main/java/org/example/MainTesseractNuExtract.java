package org.example;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.langchain4j.model.chat.Capability.RESPONSE_FORMAT_JSON_SCHEMA;
import static org.example.MainLlamaVisionMistral.*;

public class MainTesseractNuExtract {
    private final static String URL_OLLAMA = "http://localhost:11434";
    private final static String LLM_NUEXTRACT = "nuextract";

    public static void main(String[] args) throws Exception {
//        System.setProperty("TESSDATA_PREFIX", "C:/GIT/poc-ocr-llm/src/main/resources/tessdata/");
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(FOLDER + "tessdata/");
        tesseract.setLanguage("fra");

        String text = tesseract.doOCR(Path.of(FOLDER + TEST_1).toFile());

        System.out.println("###### OCR ######");
        System.out.print(text);
        System.out.println("#################");

        System.out.println();

        formatOrdonnance(text);
    }

    private static void formatOrdonnance(String text) {
        record Ordonnance(String nomPatient, String prenomPatient, List<String> medicaments, List<String> posologies) {
        }

        interface OrdonnanceExtractor {
            Ordonnance extractPersonFrom(String text);
        }

        ChatLanguageModel chatModel = OllamaChatModel.builder()
                .baseUrl(URL_OLLAMA)
                .modelName(LLM_NUEXTRACT)
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