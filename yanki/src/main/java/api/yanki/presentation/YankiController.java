package api.yanki.presentation;

import api.yanki.application.YankiService;
import api.yanki.domain.Yanki;
import api.yanki.presentation.mapper.YankiMapper;
import api.yanki.presentation.model.YankiModel;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/yanki")
public class YankiController
{
    @Autowired(required = true)
    private YankiService yankiService;
    @Autowired
    private YankiMapper yankiMapper;

    @Operation(summary = "Listar todos los monederos Yanki registrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos Yanki registrados",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findAll")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackGetAllYanki")
    @TimeLimiter(name = "yankiTimeLimiter")
    @Timed(description = "yankiGetAll")
    public Flux<YankiModel> getAll() {
        log.info("getAll executed");
        return yankiService.findAll()
                .map(yanki -> yankiMapper.entityToModel(yanki));
    }


    @Operation(summary = "Listar todos los monederos Yanki por Id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los monederos yanki por Id",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findById/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackFindById")
    @TimeLimiter(name = "yankiTimeLimiter")
    @Timed(description = "yankisGetById")
    public Mono<ResponseEntity<YankiModel>> findById(@PathVariable String id){
        return yankiService.findById(id)
                .map(yanki -> yankiMapper.entityToModel(yanki))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todos los registros por DNI")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se listaron todos los registros por DNI",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @GetMapping("/findByIdentityDni/{identityDni}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackFindByIdentityDni")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<YankiModel>> findByIdentityDni(@PathVariable String identityDni){
        log.info("findByIdentityDni executed {}", identityDni);
        Mono<Yanki> response = yankiService.findByIdentityDni(identityDni);
        return response
                .map(yanki -> yankiMapper.entityToModel(yanki))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registro de los Monederos Yanki")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se registro el monedero de manera exitosa",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PostMapping
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackCreateYanki")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<YankiModel>> create(@Valid @RequestBody YankiModel request){
        log.info("create executed {}", request);
        return yankiService.create(yankiMapper.modelToEntity(request))
                .map(yanki -> yankiMapper.entityToModel(yanki))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "yanki", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar el monedero Yanki por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se actualizará el registro por el ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @PutMapping("/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackUpdateYanki")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<YankiModel>> updateById(@PathVariable String id, @Valid @RequestBody YankiModel request){
        log.info("updateById executed {}:{}", id, request);
        return yankiService.update(id, yankiMapper.modelToEntity(request))
                .map(yanki -> yankiMapper.entityToModel(yanki))
                .flatMap(c -> Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", "register", "9081", "yanki", c.getId())))
                        .body(c)))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @Operation(summary = "Eliminar Monedero Yanki por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Se elimino el registro por ID",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Yanki.class)) }),
            @ApiResponse(responseCode = "400", description = "Parametros invalidos",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No se encontraron registros",
                    content = @Content) })
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "yankiCircuit", fallbackMethod = "fallbackDeleteYanki")
    @TimeLimiter(name = "yankiTimeLimiter")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id){
        log.info("deleteById executed {}", id);
        return yankiService.delete(id)
                .map( r -> ResponseEntity.ok().<Void>build())
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
