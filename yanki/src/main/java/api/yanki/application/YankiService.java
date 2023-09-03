package api.yanki.application;

import api.yanki.config.CircuitResilienceListener;
import api.yanki.domain.Yanki;
import api.yanki.domain.YankiRepository;
import api.yanki.presentation.mapper.YankiMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
public class YankiService
{
    @Autowired
    private YankiRepository yankiRepository;
    @Autowired
    private CircuitResilienceListener circuitResilienceListener;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;
    @Autowired
    private YankiMapper yankiMapper;

    @Autowired
    private ReactiveHashOperations<String, String, Yanki> hashOperations;

    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackGetAllYanki")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Flux<Yanki> findAll(){
        log.debug("findAll executed");

        // Intenta obtener todos los monederos yanki desde el caché de Redis
        Flux<Yanki> cachedYanki = hashOperations.values("YankiRedis")
                .flatMap(yanki -> Mono.justOrEmpty((Yanki) yanki));

        // Si hay datos en la caché de Redis, retornarlos
        return cachedYanki.switchIfEmpty(yankiRepository.findAll()
                .flatMap(yanki -> {
                    // Almacena cada monedero yanki en la caché de Redis
                    return hashOperations.put("YankiRedis", yanki.getId(), yanki)
                            .thenReturn(yanki);
                }));

    }

    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<Yanki> findById(String yankiId)
    {
        log.debug("findById executed {}" , yankiId);
        return  hashOperations.get("YankiRedis",yankiId)
                .switchIfEmpty(yankiRepository.findById(yankiId)
                        .flatMap(yanki -> hashOperations.put("YankiRedis",yanki.getId(),yanki)
                                .thenReturn(yanki)));
    }

    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackGetAllItems")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<Yanki> findByIdentityDni(String identityDni){
        log.debug("findByIdentityDni executed {}" , identityDni);
        return yankiRepository.findByIdentityDni(identityDni);
    }

    @CircuitBreaker(name = "userCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "userTimeLimiter")
    public Mono<Yanki> create(Yanki yanki){
        log.debug("create executed {}",yanki);
        return yankiRepository.save(yanki);
    }

    @CircuitBreaker(name = "userCircuit", fallbackMethod = "fallbackUpdateUser")
    @TimeLimiter(name = "userTimeLimiter")
    public Mono<Yanki> update(String yankiId, Yanki yanki){
        log.debug("update executed {}:{}", yankiId, yanki);
        return yankiRepository.findById(yankiId)
                .flatMap(dbYanki -> {
                    yankiMapper.update(dbYanki, yanki);
                    return yankiRepository.save(dbYanki);
                });
    }

    @CircuitBreaker(name = "userCircuit", fallbackMethod = "fallbackDeleteUser")
    @TimeLimiter(name = "userTimeLimiter")
    public Mono<Yanki>delete(String yankiId){
        log.debug("delete executed {}",yankiId);
        return yankiRepository.findById(yankiId)
                .flatMap(existingYanki -> yankiRepository.delete(existingYanki)
                        .then(Mono.just(existingYanki)));
    }
}
