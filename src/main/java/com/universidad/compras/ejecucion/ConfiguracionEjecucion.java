package com.universidad.compras.ejecucion;

import com.universidad.compras.notificacion.PublicadorCambiosEstado;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra como beans los servicios dados (que no pueden anotarse) y el invocador. */
@Configuration
public class ConfiguracionEjecucion {

    @Bean
    public PresupuestoService presupuestoService() {
        return new PresupuestoService();
    }

    @Bean
    public OrdenCompraService ordenCompraService() {
        return new OrdenCompraService();
    }

    @Bean
    public FabricaOperacionesSolicitud fabricaOperacionesSolicitud(PresupuestoService presupuestoService,
                                                                   OrdenCompraService ordenCompraService,
                                                                   PublicadorCambiosEstado publicadorCambiosEstado) {
        return new FabricaOperacionesSolicitud(presupuestoService, ordenCompraService, publicadorCambiosEstado);
    }

    @Bean
    public EjecutorOperaciones ejecutorOperaciones() {
        return new EjecutorOperaciones();
    }
}
