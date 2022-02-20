package platform.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import platform.model.Code;
import platform.repository.CodeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CodeService {
    private CodeRepository repository;

    public List<Code> getLatest() {
        List<Code> codeList = repository.findAll();
        codeList.removeIf(i -> i.isSecret());
        List<Code> result = new ArrayList<>();
        for (int i = codeList.size(); i > 0; i--)
            result.add(codeList.get(i - 1));
        return result.size() > 10 ? result.subList(0, 10) : result;
    }

    public Code getCode(UUID index) throws Exception {
        return repository.findById(index).orElseThrow(() -> new Exception("NotFoundException"));
    }

    public UUID addCode(String code, int maxViews, int maxTime) {
        return repository.save(new Code(code, maxViews, maxTime)).getUuid();
    }

    public void view(UUID index) throws Exception {
        Code code = repository.findById(index).orElseThrow(() -> new Exception("NotFoundException"));
        code.setViews(code.getViews() + 1);
        if (code.isSecret() && code.viewsLeft() == 0 && code.timeLeft() == 0)
            repository.deleteById(code.getUuid());
        else
            repository.save(code);
    }
}
