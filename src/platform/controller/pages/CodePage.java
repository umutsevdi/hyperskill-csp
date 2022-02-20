package platform.controller.pages;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import platform.model.Code;
import platform.model.CodeRequest;
import platform.service.CodeService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/code")
@AllArgsConstructor
public class CodePage {
    private final CodeService service;

    @GetMapping("/{id}")
    public ResponseEntity<String> getCodePage(@PathVariable("id") UUID uid, Model model) {
        model.addAttribute("Content-Type", "text/html");
        try {
            Code code = service.getCode(uid);
            CodeRequest request = code.asRequest();
            service.view(uid);
            if (request.getTime() < 0 || request.getViews() < 0) {
                return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
            }
            StringBuilder result =
                    new StringBuilder("<html><head><title> Code </title><link rel=\"stylesheet\"\n" +
                            "href=\"//cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.2.1/build/styles/default.min.css\">\n" +
                            "<script src=\"//cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.2.1/build/highlight.min.js\"></script>\n" +
                            "<script>hljs.initHighlightingOnLoad();</script></head><body><span id=\"load_date\">"
                            + request.getDate() + "</span>");
            if (code.isSecret()) {
                if (code.getMaxView() > 0) {
                    result.append("<span id=\"views_restriction\"> ")
                            .append(request.getViews())
                            .append(" more views allowed </span>");
                }
                if (code.getTime() > 0) {
                    result.append("<span id=\"time_restriction\">The code will be available for ")
                            .append(request.getTime())
                            .append(" seconds </span>");
                }
            }
            result.append("<pre id=\"code_snippet\"><code class=\"language-plaintext\">")
                    .append(code.getCode())
                    .append("</code></pre>\n");
            return new ResponseEntity<>(result.toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<String> getCodeListPage(Model model) {
        model.addAttribute("Content-Type", "text/html");
        List<CodeRequest> responses = new ArrayList<>();

        service.getLatest().forEach(i -> {
            responses.add(i.asRequest());
            try {
                service.view(i.getUuid());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return new ResponseEntity<>(
                htmlBuilder(responses),
                HttpStatus.OK);
    }

    @GetMapping("/new")
    public ResponseEntity<String> getSendCodePage(Model model) {
        model.addAttribute("Content-Type", "text/html");
        return new ResponseEntity<>(
                "<html lang=\"en\"><head><script type=\"text/javascript\">function send() {let object = {\"code\":\n" +
                        "document.getElementById(\"code_snippet\").value};let json = JSON.stringify(object);\n" +
                        "let xhr = new XMLHttpRequest();xhr.open(\"POST\", '/api/code/new', false);\n" +
                        "xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');xhr.send(json);\n" +
                        "if (xhr.status === 200) alert(\"Success!\");}</script>" +
                        "<title>Create</title></head>\n" +
                        "<body>" +
                        "<label for=\"code_snippet\"></label><textarea id=\"code_snippet\"></textarea>\n" +
                        "<input id=\"time_restriction\" type=\"text\"/>" +
                        "<input id=\"views_restriction\" type=\"text\"/>" +
                        "<button id=\"send_snippet\" type=\"submit\" onclick=\"send()\">Submit</button>\n" +
                        "</body></html>",
                HttpStatus.OK);
    }

    private String htmlBuilder(List<CodeRequest> responses) {
        StringBuilder result =
                new StringBuilder("<html><head><title>Latest</title><link rel=\"stylesheet\"\n" +
                        "href=\"//cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.2.1/build/styles/default.min.css\">\n" +
                        "<script src=\"//cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.2.1/build/highlight.min.js\"></script>\n" +
                        "<script>hljs.initHighlightingOnLoad();</script></head><body>");
        responses.forEach(i ->
                result.append("<span id=\"load_date\">")
                        .append(i.getDate())
                        .append("</span><pre id=\"code_snippet\"><code class=\"language-plaintext\">")
                        .append(i.getCode())
                        .append("</code></pre>\n"));
        result.append("</body></html>");
        return result.toString();
    }
}
