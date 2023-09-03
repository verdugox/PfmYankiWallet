package api.yanki.presentation.mapper;

import api.yanki.domain.Yanki;
import api.yanki.presentation.model.YankiModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface YankiMapper
{
    Yanki modelToEntity (YankiModel model);
    YankiModel entityToModel(Yanki event);
    @Mapping(target = "id", ignore=true)
    void update(@MappingTarget Yanki entity, Yanki updateEntity);
}
