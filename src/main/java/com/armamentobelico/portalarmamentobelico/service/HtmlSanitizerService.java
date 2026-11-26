package com.armamentobelico.portalarmamentobelico.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    private static final Set<String> CLASSES_PERMITIDAS = Set.of(
            "interview-question",
            "interview-answer");

    private static final Safelist SAFELIST = new Safelist()
            .addTags("p", "br", "strong", "b", "em", "i", "u", "h2", "h3", "blockquote", "ul", "ol", "li", "a", "span")
            .addAttributes("a", "href")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addAttributes("p", "class")
            .addAttributes("span", "class");

    public String sanitizar(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }

        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        String limpo = Jsoup.clean(html, "", SAFELIST, outputSettings);

        Document doc = Jsoup.parseBodyFragment(limpo);
        doc.outputSettings(outputSettings);

        for (Element el : doc.select("p[class], span[class]")) {
            List<String> validas = el.classNames().stream()
                    .filter(CLASSES_PERMITIDAS::contains)
                    .sorted()
                    .collect(Collectors.toList());
            if (validas.isEmpty()) {
                el.removeAttr("class");
            } else {
                el.attr("class", String.join(" ", validas));
            }
        }

        for (Element link : doc.select("a")) {
            String href = link.attr("href").trim();
            if (href.isEmpty()) {
                link.removeAttr("href");
                link.removeAttr("rel");
                link.removeAttr("target");
            } else {
                link.attr("rel", "noopener noreferrer");
                link.attr("target", "_blank");
            }
        }

        // Normaliza espaços não-quebráveis (&nbsp;) emitidos pelo editor rich-text para evitar overflow horizontal
        doc.traverse((node, depth) -> {
            if (node instanceof TextNode textNode) {
                String whole = textNode.getWholeText();
                if (whole.indexOf('\u00A0') >= 0) {
                    textNode.text(whole.replace('\u00A0', ' '));
                }
            }
        });

        return doc.body().html().trim();
    }

    public boolean temTextoVisivel(String html) {
        if (html == null || html.isBlank()) {
            return false;
        }
        String limpo = sanitizar(html);
        if (limpo.isBlank()) {
            return false;
        }
        String texto = Jsoup.parseBodyFragment(limpo)
                .text()
                .replace('\u00A0', ' ')
                .trim();
        return !texto.isEmpty();
    }
}
