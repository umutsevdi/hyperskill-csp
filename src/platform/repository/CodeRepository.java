package platform.repository;

import platform.model.Code;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public interface CodeRepository extends CrudRepository<Code, UUID> {
    @Override
    Optional<Code> findById(UUID integer);

    @Override
    List<Code> findAll();

    @Override
    <S extends Code> S save(S entity);

    @Override
    void delete(Code entity);

    @Override
    default void deleteById(UUID uuid) {

    }
}
