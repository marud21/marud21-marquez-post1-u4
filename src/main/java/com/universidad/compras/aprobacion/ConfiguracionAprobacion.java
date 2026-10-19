package com.universidad.compras.aprobacion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionAprobacion {

    @Bean
    public NivelAprobacion cadenaAprobacion() {
        return CadenasAprobacion.conCumplimientoNormativo();
    }

    @Bean
    public ServicioAprobacion servicioAprobacion(NivelAprobacion cadenaAprobacion) {
        return new ServicioAprobacionPorNiveles(cadenaAprobacion);
    }
}
