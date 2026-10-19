package com.universidad.compras.notificacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring inyecta todos los beans que implementan {@link ObservadorCambioEstado}.
 * Una cuarta reacción (por ejemplo, un aviso al canal interno del área) solo
 * necesita ser un nuevo {@code @Component}; ni esta clase ni el publicador cambian.
 */
@Configuration
public class ConfiguracionNotificacion {

    @Bean
    public PublicadorCambiosEstado publicadorCambiosEstado(List<ObservadorCambioEstado> observadores) {
        return new PublicadorCambiosEstado(observadores);
    }
}
