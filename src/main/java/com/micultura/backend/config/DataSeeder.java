package com.micultura.backend.config;

import com.micultura.backend.entity.Categoria;
import com.micultura.backend.entity.Evento;
import com.micultura.backend.repository.CategoriaRepository;
import com.micultura.backend.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MC-09-04 — Seed de eventos de Tenerife.
 * Inserts 6 categories and 25+ culturally credible events on first boot.
 * Fully idempotent: skips categories/events that already exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final EventoRepository    eventoRepository;

    // ── Unsplash placeholders (deterministic, no sign-in required) ────────────
    private static final String IMG_MUSICA      = "https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800&q=80";
    private static final String IMG_TEATRO      = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=800&q=80";
    private static final String IMG_ARTE        = "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?w=800&q=80";
    private static final String IMG_FESTIVAL    = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&q=80";
    private static final String IMG_CINE        = "https://images.unsplash.com/photo-1485846234645-a62644f84728?w=800&q=80";
    private static final String IMG_GASTRO      = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800&q=80";
    private static final String IMG_MUSICA2     = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800&q=80";
    private static final String IMG_DANZA       = "https://images.unsplash.com/photo-1518834107812-67b0b7c58434?w=800&q=80";
    private static final String IMG_JAZZ        = "https://images.unsplash.com/photo-1415201364774-f6f0bb35f28f?w=800&q=80";

    @Override
    public void run(String... args) {
        seedCategorias();
        seedEventos();
    }

    // ── 1. Categories ─────────────────────────────────────────────────────────

    private void seedCategorias() {
        List<Object[]> defs = List.of(
            new Object[]{"Música",        "Conciertos, festivales y actuaciones musicales en vivo",  "🎵"},
            new Object[]{"Teatro",        "Obras de teatro, danza y artes escénicas",                "🎭"},
            new Object[]{"Arte",          "Exposiciones, galerías y artes visuales",                  "🎨"},
            new Object[]{"Festival",      "Festivales culturales, populares y de calle",              "🎪"},
            new Object[]{"Cine",          "Proyecciones, ciclos de cine y festivales audiovisuales",  "🎬"},
            new Object[]{"Gastronomía",   "Ferias gastronómicas, catas y mercados de productos",     "🍽️"}
        );

        for (Object[] def : defs) {
            String nombre = (String) def[0];
            if (!categoriaRepository.existsByNombre(nombre)) {
                categoriaRepository.save(
                    Categoria.builder()
                        .nombre(nombre)
                        .descripcion((String) def[1])
                        .icono((String) def[2])
                        .build()
                );
                log.info("Seeded category: {}", nombre);
            }
        }
    }

    // ── 2. Events ─────────────────────────────────────────────────────────────

    private void seedEventos() {
        Map<String, Categoria> cats = categoriaRepository.findAll().stream()
            .collect(Collectors.toMap(Categoria::getNombre, Function.identity()));

        Categoria musica   = cats.get("Música");
        Categoria teatro   = cats.get("Teatro");
        Categoria arte     = cats.get("Arte");
        Categoria festival = cats.get("Festival");
        Categoria cine     = cats.get("Cine");
        Categoria gastro   = cats.get("Gastronomía");

        // Helper: today = 2026-05-15 (current date per system context)
        LocalDate base = LocalDate.of(2026, 5, 15);

        List<Evento> eventos = List.of(

            // ── MÚSICA ──────────────────────────────────────────────────────

            Evento.builder()
                .titulo("Noche de Ópera en el Auditorio")
                .descripcion("La Orquesta Sinfónica de Tenerife interpreta las grandes arias de Verdi y Puccini en una velada única bajo la cúpula del Auditorio Adán Martín. Una experiencia imprescindible para los amantes de la ópera clásica italiana.")
                .fecha(base.plusDays(5))
                .hora(LocalTime.of(20, 30))
                .ubicacion("Auditorio de Tenerife Adán Martín, Avenida de la Constitución 1, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA)
                .precio(new BigDecimal("28.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("Festival de Jazz del Atlántico")
                .descripcion("Cuatro días de jazz contemporáneo y fusión latina con artistas internacionales y locales. El escenario principal se ubica en el Parque García Sanabria, con entrada libre para todos los conciertos vespertinos.")
                .fecha(base.plusDays(12))
                .hora(LocalTime.of(18, 0))
                .ubicacion("Parque García Sanabria, Santa Cruz de Tenerife")
                .latitud(28.4672).longitud(-16.2572)
                .categoria(musica)
                .imagenUrl(IMG_JAZZ)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Concierto Sinfónico: Beethoven & Brahms")
                .descripcion("La Orquesta Sinfónica de Tenerife presenta un programa íntegramente romántico con la Sinfonía n.º 5 de Beethoven y el Concierto para violín de Brahms. Dirección invitada del maestro Carlos Kalmar.")
                .fecha(base.plusDays(19))
                .hora(LocalTime.of(19, 30))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA2)
                .precio(new BigDecimal("18.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("Flamenco en las Terrazas de La Laguna")
                .descripcion("Espectáculo de flamenco puro en el corazón histórico de San Cristóbal de La Laguna, declarada Patrimonio de la Humanidad. Una hora de baile, cante y toque con la compañía Raíces del Sur.")
                .fecha(base.plusDays(8))
                .hora(LocalTime.of(21, 0))
                .ubicacion("Plaza del Adelantado, San Cristóbal de La Laguna")
                .latitud(28.4874).longitud(-16.3159)
                .categoria(musica)
                .imagenUrl(IMG_DANZA)
                .precio(new BigDecimal("12.00"))
                .build(),

            Evento.builder()
                .titulo("Rock en el Puerto — Ciclo Verano")
                .descripcion("El Paseo de San Telmo de Puerto de la Cruz acoge cada viernes de junio a bandas canarias de rock, indie y pop alternativo. Entrada gratuita, ambiente familiar y vistas al Atlántico.")
                .fecha(base.plusDays(16))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Paseo de San Telmo, Puerto de la Cruz")
                .latitud(28.4157).longitud(-16.5469)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA)
                .precio(BigDecimal.ZERO)
                .build(),

            // ── TEATRO ──────────────────────────────────────────────────────

            Evento.builder()
                .titulo("La Casa de Bernarda Alba — Teatro Guimerá")
                .descripcion("La compañía Teatro del Ángel trae al escenario histórico del Teatro Guimerá la obra maestra de Federico García Lorca. Una producción de alto voltaje emocional con escenografía minimalista y reparto de lujo.")
                .fecha(base.plusDays(3))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Teatro Guimerá, Calle Imeldo Serís 1, Santa Cruz de Tenerife")
                .latitud(28.4629).longitud(-16.2530)
                .categoria(teatro)
                .imagenUrl(IMG_TEATRO)
                .precio(new BigDecimal("14.00"))
                .enlaceCompra("https://www.teatroguimera.es")
                .build(),

            Evento.builder()
                .titulo("Danza Contemporánea: «Islas»")
                .descripcion("La compañía MAPA Dance estrena «Islas», una pieza de danza contemporánea inspirada en la geografía volcánica del archipiélago canario. Coreografía de Aitana Hernández, música en directo de Javier Fran.")
                .fecha(base.plusDays(25))
                .hora(LocalTime.of(19, 0))
                .ubicacion("Espacio Escénico TEA, Avenida de San Sebastián 10, Santa Cruz")
                .latitud(28.4670).longitud(-16.2510)
                .categoria(teatro)
                .imagenUrl(IMG_DANZA)
                .precio(new BigDecimal("10.00"))
                .enlaceCompra("https://www.teatroguimera.es")
                .build(),

            Evento.builder()
                .titulo("Monólogos de Humor Canario")
                .descripcion("Cuatro cómicos de la isla suben al escenario del Casino Taoro para una noche de monólogos que celebra el humor costumbrista canario. Apta para todos los públicos. Aforo limitado a 250 personas.")
                .fecha(base.plusDays(10))
                .hora(LocalTime.of(21, 30))
                .ubicacion("Casino Taoro, Parque Taoro s/n, Puerto de la Cruz")
                .latitud(28.4216).longitud(-16.5430)
                .categoria(teatro)
                .imagenUrl(IMG_TEATRO)
                .precio(new BigDecimal("8.00"))
                .build(),

            Evento.builder()
                .titulo("Títeres en el Parque — Función Infantil")
                .descripcion("Compañía La Llave Maestra presenta «El Dragón y la Bruma», una obra de títeres para niños entre 3 y 10 años ambientada en un volcán mágico canario. Sesiones a las 11h y 13h.")
                .fecha(base.plusDays(7))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Parque La Granja, San Cristóbal de La Laguna")
                .latitud(28.4889).longitud(-16.3195)
                .categoria(teatro)
                .imagenUrl(IMG_TEATRO)
                .precio(new BigDecimal("4.00"))
                .build(),

            // ── ARTE ────────────────────────────────────────────────────────

            Evento.builder()
                .titulo("Exposición: «Atlántico Profundo» — TEA")
                .descripcion("El TEA Tenerife Espacio de las Artes inaugura una muestra colectiva de artistas canarios contemporáneos que exploran la relación entre el archipiélago y el océano Atlántico. Piezas de instalación, fotografía y videoarte.")
                .fecha(base.plusDays(1))
                .hora(LocalTime.of(10, 0))
                .ubicacion("TEA — Tenerife Espacio de las Artes, Avenida de San Sebastián 10, Santa Cruz")
                .latitud(28.4670).longitud(-16.2510)
                .categoria(arte)
                .imagenUrl(IMG_ARTE)
                .precio(new BigDecimal("6.00"))
                .enlaceCompra("https://www.teatenerife.es")
                .build(),

            Evento.builder()
                .titulo("Murales Urbanos: Ruta Guiada por Santa Cruz")
                .descripcion("Recorrido guiado por los murales de arte urbano más emblemáticos de Santa Cruz de Tenerife. La ruta dura aproximadamente 2 horas y está conducida por la artista local Verónica Plasencia.")
                .fecha(base.plusDays(14))
                .hora(LocalTime.of(10, 30))
                .ubicacion("Punto de encuentro: Plaza de España, Santa Cruz de Tenerife")
                .latitud(28.4631).longitud(-16.2526)
                .categoria(arte)
                .imagenUrl(IMG_ARTE)
                .precio(new BigDecimal("5.00"))
                .build(),

            Evento.builder()
                .titulo("Feria de Artesanía Canaria — CajaCanarias")
                .descripcion("Más de 40 artesanos de las siete islas exhiben y venden cerámica, cestería, bordados, joyería en plata y otros oficios tradicionales. Talleres gratuitos para niños sábados y domingos de 11h a 13h.")
                .fecha(base.plusDays(21))
                .hora(LocalTime.of(10, 0))
                .ubicacion("Espacio Cultural CajaCanarias, Calle Méndez Núñez 60, Santa Cruz")
                .latitud(28.4641).longitud(-16.2538)
                .categoria(arte)
                .imagenUrl(IMG_ARTE)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Fotografía Volcánica — Galería Norte")
                .descripcion("El fotógrafo Pedro Nolasco presenta su último trabajo documental sobre los paisajes lávicos del Teide y la Caldera de Las Cañadas. 45 imágenes en gran formato, algunas tomadas desde dron.")
                .fecha(base.plusDays(30))
                .hora(LocalTime.of(18, 30))
                .ubicacion("Galería de Arte Norte, Calle Bethencourt Alfonso 30, Santa Cruz")
                .latitud(28.4650).longitud(-16.2555)
                .categoria(arte)
                .imagenUrl(IMG_ARTE)
                .precio(BigDecimal.ZERO)
                .build(),

            // ── FESTIVAL ────────────────────────────────────────────────────

            Evento.builder()
                .titulo("Festival Internacional de Música de Canarias — Clausura")
                .descripcion("Concierto de clausura del FIMC en el Auditorio Adán Martín con la participación de la Orquesta Filarmónica de Berlín. El programa incluye obras de Mahler y Shostakóvich. Evento de talla mundial en Tenerife.")
                .fecha(base.plusDays(6))
                .hora(LocalTime.of(20, 30))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(new BigDecimal("35.00"))
                .enlaceCompra("https://www.festivaldecanarias.com")
                .build(),

            Evento.builder()
                .titulo("Mercado Medieval de La Laguna")
                .descripcion("El casco histórico de La Laguna se transforma en un mercado medieval con herreros, juglares, artesanos, cuentacuentos y degustación de productos artesanales. Ambiente familiar durante todo el fin de semana.")
                .fecha(base.plusDays(22))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Casco Histórico, San Cristóbal de La Laguna")
                .latitud(28.4874).longitud(-16.3159)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Festival de Música Electrónica — Siam Park")
                .descripcion("Primer festival de música electrónica en recinto acuático de Canarias. DJs internacionales actuarán junto a las instalaciones del Siam Park en una experiencia diurna única con pools y zonas de baile al sol.")
                .fecha(base.plusDays(35))
                .hora(LocalTime.of(12, 0))
                .ubicacion("Siam Park, Autovía del Sur km 28, Adeje")
                .latitud(28.0617).longitud(-16.7241)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(new BigDecimal("45.00"))
                .enlaceCompra("https://www.siampark.net")
                .build(),

            Evento.builder()
                .titulo("Noche de San Juan en la Playa de las Teresitas")
                .descripcion("La noche más mágica del año en la playa más bonita de Santa Cruz. Hogueras, conciertos en directo, pirotecnia y baño ritual a medianoche. Acceso libre, se recomienda llegar antes de las 21h para aparcar.")
                .fecha(LocalDate.of(2026, 6, 23))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Playa de Las Teresitas, San Andrés, Santa Cruz de Tenerife")
                .latitud(28.5088).longitud(-16.1929)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Fiestas de Santa Cruz — Verbena Popular")
                .descripcion("Las fiestas patronales de Santa Cruz de Tenerife incluyen verbenas, elección de la reina, fuegos artificiales y actuaciones de orquestas en el Recinto Ferial. El evento más esperado del verano capitalino.")
                .fecha(LocalDate.of(2026, 7, 25))
                .hora(LocalTime.of(22, 0))
                .ubicacion("Recinto Ferial, Santa Cruz de Tenerife")
                .latitud(28.4631).longitud(-16.2526)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            // ── CINE ────────────────────────────────────────────────────────

            Evento.builder()
                .titulo("Tenerife Noir — Festival de Cine Negro")
                .descripcion("V edición del festival de cine negro y policiaco de Tenerife con proyecciones en versión original, masterclasses con directores y una sección especial dedicada al neo-noir canario. Sede principal en el espacio TEA.")
                .fecha(base.plusDays(28))
                .hora(LocalTime.of(18, 0))
                .ubicacion("TEA — Tenerife Espacio de las Artes, Santa Cruz")
                .latitud(28.4670).longitud(-16.2510)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(new BigDecimal("7.00"))
                .enlaceCompra("https://www.teatenerife.es")
                .build(),

            Evento.builder()
                .titulo("Cine al Aire Libre — Ciclo Verano Guímar")
                .descripcion("El Espacio Cultural del Municipio de Güímar proyecta cada miércoles de julio películas de autor en su patio exterior. Esta semana: «El cuaderno de Sara» de Mota. Sillas disponibles o traer manta.")
                .fecha(base.plusDays(45))
                .hora(LocalTime.of(21, 30))
                .ubicacion("Espacio Cultural Municipal, Güímar")
                .latitud(28.3133).longitud(-16.4031)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(new BigDecimal("3.00"))
                .build(),

            Evento.builder()
                .titulo("Proyección: Documental «El Teide desde Adentro»")
                .descripcion("Estreno del documental que narra la historia geológica y humana del volcán más alto de España. Producción canaria con imágenes del interior de los tubos volcánicos nunca antes vistas. Coloquio con el director tras la proyección.")
                .fecha(base.plusDays(18))
                .hora(LocalTime.of(19, 0))
                .ubicacion("Auditorio Municipal de La Laguna, Calle Núñez de la Peña 3")
                .latitud(28.4887).longitud(-16.3142)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(new BigDecimal("5.00"))
                .build(),

            Evento.builder()
                .titulo("Cortos Canarios — Maratón de Cortometrajes")
                .descripcion("Ocho horas ininterrumpidas de cortometrajes realizados en Canarias durante el último año. 32 piezas seleccionadas por el jurado, con entrega de premios al finalizar. El público puede votar al mejor corto del público.")
                .fecha(base.plusDays(40))
                .hora(LocalTime.of(10, 0))
                .ubicacion("Multicines Yelmo, Centro Comercial Meridiano, Santa Cruz")
                .latitud(28.4694).longitud(-16.2535)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(new BigDecimal("10.00"))
                .build(),

            // ── GASTRONOMÍA ─────────────────────────────────────────────────

            Evento.builder()
                .titulo("Feria del Vino de Tenerife")
                .descripcion("La Feria del Vino reúne a más de 60 bodegas de la Denominación de Origen Tacoronte-Acentejo y otras zonas vinícolas de la isla. Catas guiadas, maridajes, conferencias sobre viticultura volcánica y venta directa al público.")
                .fecha(base.plusDays(11))
                .hora(LocalTime.of(12, 0))
                .ubicacion("Recinto Ferial, Santa Cruz de Tenerife")
                .latitud(28.4631).longitud(-16.2526)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(new BigDecimal("8.00"))
                .enlaceCompra("https://www.feriadelvino.tf")
                .build(),

            Evento.builder()
                .titulo("Mercado de Productores — La Laguna Agroecológica")
                .descripcion("Mercado semanal de productores agroecológicos de Tenerife en la Plaza del Cristo. Frutas tropicales, quesos artesanos, mieles del Teide, papas antiguas y mucho más. Directo del campo a tu cesta.")
                .fecha(base.plusDays(4))
                .hora(LocalTime.of(9, 0))
                .ubicacion("Plaza del Cristo, San Cristóbal de La Laguna")
                .latitud(28.4891).longitud(-16.3143)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Jornadas Gastronómicas del Atún Rojo")
                .descripcion("Los restaurantes del Puerto de la Cruz se unen para celebrar la temporada del atún rojo del Atlántico con menús degustación especiales. Showcooking con chefs locales cada tarde en el paseo marítimo.")
                .fecha(base.plusDays(33))
                .hora(LocalTime.of(13, 0))
                .ubicacion("Paseo Marítimo, Puerto de la Cruz")
                .latitud(28.4180).longitud(-16.5480)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(new BigDecimal("20.00"))
                .build(),

            Evento.builder()
                .titulo("Cata de Quesos Artesanos de Canarias")
                .descripcion("Sesión de cata guiada con los mejores quesos artesanos de las siete islas, maridados con vinos y mojos tradicionales canarios. Aforo máximo de 30 personas. Incluye tabla de quesos para llevar.")
                .fecha(base.plusDays(26))
                .hora(LocalTime.of(18, 0))
                .ubicacion("Bodega Monje, Calle Cruz de Leandro 36, El Sauzal")
                .latitud(28.4799).longitud(-16.4344)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(new BigDecimal("22.00"))
                .enlaceCompra("https://www.bodegasmonje.com")
                .build(),

            Evento.builder()
                .titulo("Festival Sabor Canario — Los Cristianos")
                .descripcion("Dos días de cocina canaria de autor y tradicional en el paseo marítimo de Los Cristianos. Doce restaurantes de la isla sur compiten por el premio al mejor plato canario del año. Entrada gratuita, degustaciones desde 2 euros.")
                .fecha(base.plusDays(50))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Paseo Marítimo Los Cristianos, Arona")
                .latitud(28.0527).longitud(-16.7155)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(BigDecimal.ZERO)
                .build(),

            // ── REAL EVENTS — TENERIFE 2026 ─────────────────────────────────
            // Sourced from publicly announced programmes (Auditorio de Tenerife,
            // Tenerife Music Festival, Cook Music Fest, FIMUCITÉ, FICMEC,
            // MUECA, GastroCanarias, traditional patron-saint festivities).

            Evento.builder()
                .titulo("Tenerife Music Festival — Día Urban: Rels B & Nathy Peluso")
                .descripcion("Primera jornada del festival más grande del año en Santa Cruz. La noche urbana arranca con Nathy Peluso y cierra con Rels B presentando su nuevo disco. Recinto al aire libre con vistas al puerto.")
                .fecha(LocalDate.of(2026, 6, 12))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Recinto Portuario, Santa Cruz de Tenerife")
                .latitud(28.4756).longitud(-16.2356)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA)
                .precio(new BigDecimal("55.00"))
                .enlaceCompra("https://tenerifemusicfestival.com")
                .build(),

            Evento.builder()
                .titulo("Tenerife Music Festival — Día Pop: Camilo & Pablo Alborán")
                .descripcion("Segunda jornada del festival con la cara más mainstream del pop en español. Camilo y Pablo Alborán comparten cartel en una de las citas musicales del verano en Canarias.")
                .fecha(LocalDate.of(2026, 6, 13))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Recinto Portuario, Santa Cruz de Tenerife")
                .latitud(28.4756).longitud(-16.2356)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA2)
                .precio(new BigDecimal("60.00"))
                .enlaceCompra("https://tenerifemusicfestival.com")
                .build(),

            Evento.builder()
                .titulo("Cook Music Fest — Reguetón en el Puerto")
                .descripcion("Don Omar regresa a Europa exclusivamente para Cook Music Fest, junto a Farruko, Myke Towers, Lola Índigo y Rubén Blades. Tres días de reguetón, latino y mestizaje frente al Atlántico.")
                .fecha(LocalDate.of(2026, 7, 17))
                .hora(LocalTime.of(19, 30))
                .ubicacion("Recinto Portuario, Santa Cruz de Tenerife")
                .latitud(28.4756).longitud(-16.2356)
                .categoria(musica)
                .imagenUrl(IMG_FESTIVAL)
                .precio(new BigDecimal("75.00"))
                .enlaceCompra("https://cookmusicfest.com")
                .build(),

            Evento.builder()
                .titulo("FIMUCITÉ 20 — Banda Sonora del Cine")
                .descripcion("Vigésima edición del Festival Internacional de Música de Cine de Tenerife. Conciertos sinfónicos con grandes temas del cine y la televisión, conducidos por la Orquesta Sinfónica de Tenerife.")
                .fecha(LocalDate.of(2026, 7, 5))
                .hora(LocalTime.of(20, 30))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA2)
                .precio(new BigDecimal("25.00"))
                .enlaceCompra("https://fimucite.com")
                .build(),

            Evento.builder()
                .titulo("I Festival de Timple")
                .descripcion("Primera edición del festival dedicado al timple, el instrumento de cuerda emblemático de Canarias. Talleres, ponencias y conciertos de los principales intérpretes del archipiélago.")
                .fecha(LocalDate.of(2026, 5, 22))
                .hora(LocalTime.of(19, 0))
                .ubicacion("Teatro Leal, San Cristóbal de La Laguna")
                .latitud(28.4882).longitud(-16.3157)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA)
                .precio(new BigDecimal("15.00"))
                .build(),

            Evento.builder()
                .titulo("MAPAS 2026: «Bogotá» — Andrea Peña & Artists")
                .descripcion("Apertura del ciclo Mapas 2026 del Auditorio de Tenerife con la compañía canadiense Andrea Peña & Artists presentando «Bogotá», una pieza híbrida entre danza contemporánea y arte visual.")
                .fecha(LocalDate.of(2026, 6, 30))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(teatro)
                .imagenUrl(IMG_DANZA)
                .precio(new BigDecimal("15.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("MAPAS 2026: «Hamlet, Prince of Denmark» — Robert Lepage")
                .descripcion("El director canadiense Robert Lepage y Ex Machina presentan una relectura íntima del Hamlet shakespeariano para un único intérprete, Guillaume Côté. Cita imprescindible del programa Mapas.")
                .fecha(LocalDate.of(2026, 7, 4))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(teatro)
                .imagenUrl(IMG_TEATRO)
                .precio(new BigDecimal("15.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("MAPAS 2026: «Hammer» — Alexander Ekman")
                .descripcion("El coreógrafo sueco Alexander Ekman estrena «Hammer» en el Auditorio de Tenerife. Pieza de gran formato que cruza humor, virtuosismo técnico y reflexión sobre el cuerpo en escena.")
                .fecha(LocalDate.of(2026, 7, 11))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(teatro)
                .imagenUrl(IMG_DANZA)
                .precio(new BigDecimal("15.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("Les Ballets Espagnols de La Argentina")
                .descripcion("Compañía con base en París que rescata el patrimonio coreográfico de Antonia Mercé «La Argentina», pionera de la danza española. Programa con piezas clásicas en versión orquestal.")
                .fecha(LocalDate.of(2026, 6, 5))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Auditorio de Tenerife Adán Martín, Santa Cruz")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(teatro)
                .imagenUrl(IMG_DANZA)
                .precio(new BigDecimal("25.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("MUECA — Festival de Artes de Calle")
                .descripcion("Cuatro días en los que Puerto de la Cruz se transforma en un gran escenario al aire libre. Teatro, circo, danza, payasos y música en vivo de compañías internacionales. Acceso libre.")
                .fecha(LocalDate.of(2026, 5, 9))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Casco histórico, Puerto de la Cruz")
                .latitud(28.4157).longitud(-16.5469)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Día de Canarias — Plaza de España")
                .descripcion("Celebración del Día de Canarias con folclore, gastronomía típica, talleres infantiles y conciertos de grupos canarios en la Plaza de España. Entrada libre, ambiente familiar.")
                .fecha(LocalDate.of(2026, 5, 30))
                .hora(LocalTime.of(12, 0))
                .ubicacion("Plaza de España, Santa Cruz de Tenerife")
                .latitud(28.4631).longitud(-16.2526)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Veranos del Taoro")
                .descripcion("Ciclo de conciertos en los jardines del Casino Taoro, con propuestas que cruzan jazz, bossa nova y música canaria contemporánea. Vistas privilegiadas al Atlántico y al Teide al atardecer.")
                .fecha(LocalDate.of(2026, 6, 19))
                .hora(LocalTime.of(21, 0))
                .ubicacion("Jardines del Casino Taoro, Puerto de la Cruz")
                .latitud(28.4216).longitud(-16.5430)
                .categoria(festival)
                .imagenUrl(IMG_JAZZ)
                .precio(new BigDecimal("12.00"))
                .build(),

            Evento.builder()
                .titulo("Fiestas del Carmen — Procesión Marítima")
                .descripcion("La patrona de Puerto de la Cruz sale del Muelle Pesquero en procesión marítima acompañada por decenas de barcas engalanadas. Acto popular con verbenas, fuegos y mucho ambiente vecinal.")
                .fecha(LocalDate.of(2026, 7, 16))
                .hora(LocalTime.of(19, 0))
                .ubicacion("Muelle Pesquero, Puerto de la Cruz")
                .latitud(28.4185).longitud(-16.5495)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Fiestas de la Candelaria — Patrona de Canarias")
                .descripcion("La Villa de Candelaria recibe a miles de peregrinos en la fiesta mayor de la patrona del archipiélago. Misa, procesión, fuegos artificiales y representación teatral de la aparición de la Virgen.")
                .fecha(LocalDate.of(2026, 8, 14))
                .hora(LocalTime.of(19, 0))
                .ubicacion("Plaza de la Patrona de Canarias, Candelaria")
                .latitud(28.3525).longitud(-16.3700)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Romería de Tegueste")
                .descripcion("Una de las romerías más concurridas del norte de Tenerife. Carretas tiradas por bueyes, trajes típicos, parrandas en directo y degustación de productos tradicionales canarios por todo el pueblo.")
                .fecha(LocalDate.of(2026, 5, 17))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Casco urbano, Tegueste")
                .latitud(28.5236).longitud(-16.3413)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("FICMEC — Festival Internacional de Cine Medioambiental")
                .descripcion("28ª edición del FICMEC, dedicado al cine sobre ecología, naturaleza y medioambiente. Proyecciones gratuitas al aire libre, mesas redondas y feria agroecológica complementaria.")
                .fecha(LocalDate.of(2026, 5, 30))
                .hora(LocalTime.of(20, 30))
                .ubicacion("Plaza de la Libertad, Garachico")
                .latitud(28.3737).longitud(-16.7639)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(BigDecimal.ZERO)
                .enlaceCompra("https://ficmec.es")
                .build(),

            Evento.builder()
                .titulo("GastroCanarias 2026 — Salón Gastronómico")
                .descripcion("Undécima edición del salón profesional de la gastronomía canaria. 17.000 m² de exposición con más de 200 stands de productores, bodegas, chefs y restauradores del archipiélago. Catas y showcooking en directo.")
                .fecha(LocalDate.of(2026, 5, 20))
                .hora(LocalTime.of(10, 0))
                .ubicacion("Recinto Ferial de Tenerife, Santa Cruz")
                .latitud(28.4631).longitud(-16.2526)
                .categoria(gastro)
                .imagenUrl(IMG_GASTRO)
                .precio(new BigDecimal("8.00"))
                .enlaceCompra("https://salongastronomicodecanarias.com")
                .build(),

            // ── EXTRA JUNIO 2026 ────────────────────────────────────────────

            Evento.builder()
                .titulo("Corpus Christi en La Laguna — Alfombras")
                .descripcion("San Cristóbal de La Laguna celebra el Corpus Christi con 68 alfombras de flores, sal y materiales naturales decorando las calles del casco histórico, por donde pasa la procesión del Santísimo Sacramento. Una tradición centenaria del Patrimonio de la Humanidad.")
                .fecha(LocalDate.of(2026, 6, 7))
                .hora(LocalTime.of(11, 0))
                .ubicacion("Casco histórico, San Cristóbal de La Laguna")
                .latitud(28.4874).longitud(-16.3159)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Corpus Christi en La Orotava — Alfombras de Flores")
                .descripcion("La gran tapiz de tierras volcánicas del Teide tiñe la plaza del Ayuntamiento de La Orotava en la cita declarada de Interés Turístico Nacional. Decenas de alfombras florales únicas elaboradas por las hermandades locales durante toda la noche anterior.")
                .fecha(LocalDate.of(2026, 6, 14))
                .hora(LocalTime.of(10, 0))
                .ubicacion("Plaza del Ayuntamiento, La Orotava")
                .latitud(28.3905).longitud(-16.5236)
                .categoria(festival)
                .imagenUrl(IMG_FESTIVAL)
                .precio(BigDecimal.ZERO)
                .build(),

            Evento.builder()
                .titulo("Dúo del Valle — Recital de Piano a Cuatro Manos")
                .descripcion("Los hermanos Víctor y Luis del Valle abren el ciclo Primavera Musical del Auditorio con un programa romántico a cuatro manos: obras de Schubert, Brahms y Ravel. Duración aproximada 75 minutos sin descanso.")
                .fecha(LocalDate.of(2026, 6, 2))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Sala de Cámara, Auditorio de Tenerife")
                .latitud(28.4636).longitud(-16.2518)
                .categoria(musica)
                .imagenUrl(IMG_MUSICA2)
                .precio(new BigDecimal("18.00"))
                .enlaceCompra("https://www.auditoriodetenerife.com")
                .build(),

            Evento.builder()
                .titulo("FICMEC — Sede Icod de los Vinos")
                .descripcion("Segunda sede del Festival Internacional de Cine Medioambiental de Canarias. Proyecciones gratuitas al aire libre con largometrajes y cortos seleccionados, mesa redonda sobre cambio climático y feria agroecológica complementaria.")
                .fecha(LocalDate.of(2026, 6, 6))
                .hora(LocalTime.of(20, 0))
                .ubicacion("Plaza de la Pila, Icod de los Vinos")
                .latitud(28.3717).longitud(-16.7100)
                .categoria(cine)
                .imagenUrl(IMG_CINE)
                .precio(BigDecimal.ZERO)
                .enlaceCompra("https://ficmec.es")
                .build(),

            Evento.builder()
                .titulo("La Misa — Ritual de Techno al Aire Libre")
                .descripcion("Ceremonia inmersiva de techno al amanecer en un emplazamiento secreto del sur. Puesta en escena oscura, cargada de energía, pensada como un ritual sagrado para la pista. Producción de Farra World, ubicación se revela 24 h antes del evento.")
                .fecha(LocalDate.of(2026, 6, 27))
                .hora(LocalTime.of(23, 0))
                .ubicacion("Ubicación secreta — Sur de Tenerife")
                .latitud(28.0617).longitud(-16.7241)
                .categoria(musica)
                .imagenUrl(IMG_FESTIVAL)
                .precio(new BigDecimal("35.00"))
                .enlaceCompra("https://farra.world")
                .build()
        );

        // Idempotent: skip events whose title is already in the DB. Lets new
        // entries added to this list land on the next boot without wiping
        // existing data.
        java.util.Set<String> existingTitles = eventoRepository.findAll().stream()
            .map(Evento::getTitulo)
            .collect(Collectors.toSet());

        List<Evento> toInsert = eventos.stream()
            .filter(e -> !existingTitles.contains(e.getTitulo()))
            .toList();

        if (toInsert.isEmpty()) {
            log.info("All {} defined events already present — nothing to seed.", eventos.size());
            return;
        }

        eventoRepository.saveAll(toInsert);
        log.info("Seeded {} new events ({} already present).",
                toInsert.size(), eventos.size() - toInsert.size());
    }
}
