package api.yanki.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface YankiRepository extends ReactiveMongoRepository<Yanki,String>
{
    Mono<Yanki> findByIdentityDni(String identityDni);
}
