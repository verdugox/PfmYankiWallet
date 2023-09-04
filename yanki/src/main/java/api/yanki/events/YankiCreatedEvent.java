package api.yanki.events;

import api.yanki.domain.Yanki;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class YankiCreatedEvent extends Event<Yanki> {

}
