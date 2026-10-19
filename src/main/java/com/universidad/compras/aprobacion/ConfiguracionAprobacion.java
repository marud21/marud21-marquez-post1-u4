package com.universidad.compras.aprobacion;

import com.universidad.compras.notificacion.PublicadorCambiosEstado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionAprobacion {

    @Bean
    public NivelAprobacion cadenaAprobacion() {
        return CadenasAprobacion.conCumplimientoNormativo();
    }

    @Bean
    public ServicioAprobacion servicioAprobacion(NivelAprobacion cadenaAprobacion,
                                                 PublicadorCambiosEstado publicadorCambiosEstado) {
        return new ServicioAprobacionPorNiveles(cadenaAprobacion, publicadorCambiosEstado);
    }
}
