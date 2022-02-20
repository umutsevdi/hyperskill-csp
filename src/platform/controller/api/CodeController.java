package platform.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import platform.model.CodeRequest;
import platform.service.CodeService;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/code")
public class CodeController {
    private final CodeService service;
    private UUID newest;

    public CodeController(@Autowired CodeService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CodeRequest> getCode(@PathVariable("id") UUID uid,
                                               HttpServletResponse response) {
        response.addHeader("Content-Type", "application/json");
        try {
            CodeRequest request = service.getCode(uid).asRequest();
            service.view(uid);

            if (request.getTime() < 0 || request.getViews() < 0)
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);

            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/new")
    public ResponseEntity<String> getNewCode(HttpServletResponse response) {
        response.addHeader("Content-Type", "application/json");
        return new ResponseEntity<>("{\"id\" : \"" + this.newest + "\"}", HttpStatus.OK);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<CodeRequest>> getLatest(HttpServletResponse response) {
        response.addHeader("Content-Type", "application/json");
        List<CodeRequest> result = new ArrayList<>();
        service.getLatest().forEach(i -> {
            result.add(i.asRequest());
            try {
                service.view(i.getUuid());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("/new")
    public ResponseEntity<String> sendCode(@RequestBody CodeRequest request,
                                           HttpServletResponse response) {
        response.addHeader("Content-Type", "application/json");
        if (request.getViews() == null) request.setViews(0);
        if (request.getTime() == null) request.setTime(0);
        this.newest = service.addCode(request.getCode(), request.getViews(), request.getTime());
        return new ResponseEntity<>("{\"id\" : \"" + this.newest + "\"}", HttpStatus.OK);
    }
}
