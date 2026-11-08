package com.petcare.petcarebackend.infrastructure.configuration;

import com.petcare.petcarebackend.domain.enums.OfferingCategory;
import com.petcare.petcarebackend.domain.enums.OfferingType;
import com.petcare.petcarebackend.domain.enums.Role;
import com.petcare.petcarebackend.domain.enums.SlotStatus;
import com.petcare.petcarebackend.domain.enums.TypeProvider;
import com.petcare.petcarebackend.infrastructure.adapter.repository.OfferingRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.ProviderRepository;
import com.petcare.petcarebackend.infrastructure.adapter.repository.TimeSlotRepository;
import com.petcare.petcarebackend.infrastructure.entity.OfferingJpa;
import com.petcare.petcarebackend.infrastructure.entity.ProviderJpa;
import com.petcare.petcarebackend.infrastructure.entity.TimeSlotJpa;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Datos demostrativos para que una instalación limpia tenga servicios navegables.
 * Es idempotente: si ya existe el proveedor demo, no vuelve a insertar registros.
 */
@Configuration
public class DemoDataSeeder {

    private static final String DEMO_EMAIL = "demo.proveedor@petcare.test";

    @Bean
    CommandLineRunner seedDemoData(ProviderRepository providers,
                                   OfferingRepository offerings,
                                   TimeSlotRepository slots) {
        return args -> {
            if (providers.existsByEmail(DEMO_EMAIL)) {
                return;
            }

            ProviderJpa provider = new ProviderJpa(
                    UUID.randomUUID(),
                    "PetCare Servicios Demo",
                    TypeProvider.INDEPENDIENTE,
                    "seed-only-demo",
                    "Proveedor de ejemplo para probar el prototipo funcional.",
                    "+573001112233",
                    DEMO_EMAIL,
                    "Bogotá",
                    5,
                    "Calle 100 # 10-20",
                    Role.PROVIDER
            );
            providers.save(provider);

            List<OfferingSeed> seeds = List.of(
                    new OfferingSeed("Consulta veterinaria básica", "Valoración general y orientación preventiva.", "Llevar el carné de vacunación si está disponible.", "45000", OfferingCategory.VETERINARIA),
                    new OfferingSeed("Vacunación y desparasitación", "Aplicación de vacunas y plan básico de desparasitación.", "Sujeto a valoración previa.", "60000", OfferingCategory.VETERINARIA),
                    new OfferingSeed("Baño y secado", "Baño higiénico, secado y cepillado para perros y gatos.", "La mascota debe estar en condiciones aptas para el servicio.", "35000", OfferingCategory.PELUQUERIA),
                    new OfferingSeed("Corte higiénico", "Corte higiénico y arreglo básico del pelaje.", "Precio referencial; puede variar según tamaño.", "50000", OfferingCategory.PELUQUERIA),
                    new OfferingSeed("Paseo individual", "Paseo supervisado de 45 minutos en zona segura.", "Se requiere información de comportamiento.", "25000", OfferingCategory.PASEO),
                    new OfferingSeed("Paseo activo", "Paseo de 60 minutos con actividad física moderada.", "No disponible para mascotas con restricción veterinaria.", "32000", OfferingCategory.PASEO),
                    new OfferingSeed("Cuidado temporal de medio día", "Acompañamiento y cuidado durante media jornada.", "Entregar alimento e indicaciones especiales.", "55000", OfferingCategory.CUIDADO_TEMPORAL),
                    new OfferingSeed("Cuidado temporal jornada completa", "Cuidado supervisado durante una jornada completa.", "Cupo limitado por día.", "95000", OfferingCategory.CUIDADO_TEMPORAL)
            );

            LocalDate firstDate = LocalDate.now(ZoneId.of("America/Bogota")).plusDays(1);
            for (int index = 0; index < seeds.size(); index++) {
                OfferingSeed seed = seeds.get(index);
                OfferingJpa offering = new OfferingJpa(
                        UUID.randomUUID(), provider, seed.name(), seed.name().toLowerCase(),
                        seed.description(), seed.conditions(), new BigDecimal(seed.price()),
                        seed.category(), OfferingType.SERVICIO, null
                );
                offerings.save(offering);

                LocalDate date = firstDate.plusDays(index % 3);
                slots.save(new TimeSlotJpa(UUID.randomUUID(), offering, date,
                        LocalTime.of(9, 0), LocalTime.of(10, 0), SlotStatus.LIBRE));
                slots.save(new TimeSlotJpa(UUID.randomUUID(), offering, date,
                        LocalTime.of(14, 0), LocalTime.of(15, 0), SlotStatus.LIBRE));
            }
        };
    }

    private record OfferingSeed(String name, String description, String conditions,
                                String price, OfferingCategory category) {
    }
}
