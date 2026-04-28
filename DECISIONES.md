# Decisiones de Diseño - Proyecto BiblioTech

Este documento detalla el proceso de toma de decisiones y el abordaje técnico aplicado para resolver cada uno de los requerimientos funcionales del sistema BiblioTech. Durante todo el ciclo de desarrollo se respetó el flujo de trabajo en GitHub, utilizando ramas independientes (`feature/*`) para cada Issue, protegiendo la rama `main` y garantizando una arquitectura basada en capas y principios SOLID.

---

## 3.A. Gestión de Recursos

### Issue 1: Registro de libros (ISBN, título, autor, año y categoría)
**El desafío:** Establecer la arquitectura base y registrar libros asegurando la inmutabilidad de los datos exigida en la rúbrica.
**La decisión técnica:** Implementamos el patrón `Repository` para aislar el almacenamiento y utilizamos el componente `record Libro` para la entidad[cite: 128].
**El por qué:** El uso de `record` permite crear un DTO (Data Transfer Object) inmutable de forma limpia, cumpliendo con la exigencia de usar "Modern Java" y evitando la creación de clases con lógica repetitiva de getters y setters[cite: 128].

### Issue 2: Soporte de diferentes tipos de ejemplares (Físicos y E-books)
**El desafío:** El sistema debía permitir el ingreso de libros tradicionales y digitales, cada uno con atributos propios, sin romper el principio Abierto/Cerrado (OCP).
**La decisión técnica:** Diseñamos una interfaz `Recurso` que define los contratos comunes y es implementada por los records `Libro` y `Ebook`[cite: 5, 14, 128].
**El por qué:** Este enfoque polimórfico permite que el repositorio maneje listas genéricas de tipo `Recurso`[cite: 93]. Así, la incorporación de nuevos tipos de materiales en el futuro no requerirá modificar la estructura de almacenamiento existente.

### Issue 3: Búsqueda avanzada por múltiples criterios
**El desafío:** Permitir al usuario encontrar recursos filtrando por título, autor o categoría de forma eficiente y sin distinguir mayúsculas de minúsculas.
**La decisión técnica:** Se implementó el método `buscarAvanzada` en el repositorio utilizando el API de `Streams` de Java y el método `toLowerCase()` para la normalización de textos [cite: 98-99].
**El por qué:** El uso de Streams permite realizar filtrados sobre colecciones de forma declarativa y legible[cite: 99]. Al aplicar la búsqueda sobre la interfaz `Recurso`, el motor funciona de igual manera para todos los tipos de ejemplares.

---

## 3.B. Gestión de Usuarios (Socios)

### Issue 4: Registro de socios con validación de datos
**El desafío:** Asegurar que el DNI sea numérico y el email tenga un formato válido antes de persistir los datos.
**La decisión técnica:** Se creó la clase `ValidadorSocio` que utiliza expresiones regulares (Regex) para validar el formato de email y reglas de longitud para el DNI [cite: 208-212].
**El por qué:** Delegar la validación a una clase específica cumple con el Principio de Responsabilidad Única (SRP). [cite_start]En caso de error, se lanza una `ValidacionSocioException` que es capturada por el menú principal para informar al usuario sin interrumpir el programa [cite: 114, 61-62].

### Issue 5: Categorización de socios y límites de préstamo
**El desafío:** Aplicar límites de retiro distintos (3 para Estudiantes y 5 para Docentes) sin utilizar estructuras condicionales basadas en el tipo de objeto.
**La decisión técnica:** Se definió la interfaz `Socio` con el método `obtenerTopePrestamos()`, el cual es implementado de forma específica por los records `Estudiante` y `Docente`[cite: 140, 80, 111].
**El por qué:** Mediante polimorfismo, el servicio de préstamos simplemente consulta el tope al objeto socio sin necesidad de conocer su clase concreta[cite: 196]. Esto facilita la extensión del sistema a nuevas categorías de usuarios.

---

## 3.C. Ciclo de Préstamo

### Issue 6: Registro de préstamos (Disponibilidad y Límites)
**El desafío:** Validar que el recurso exista, no esté prestado actualmente y que el socio tenga cupo disponible, todo de manera atómica.
**La decisión técnica:** El método `registrarPrestamo` del `PrestamoService` centraliza estas validaciones consultando los repositorios inyectados por constructor [cite: 188-198]. [cite_start]Se utilizó `Optional` para manejar las búsquedas y evitar el uso de `null`[cite: 192].
**El por qué:** La inyección de dependencias permite desacoplar la lógica de negocio de la implementación del repositorio, mientras que el uso de `Optional` garantiza la robustez del código ante recursos no encontrados[cite: 186, 192].

### Issue 7: Gestión de devoluciones con cálculo de retraso
**El desafío:** Identificar el préstamo activo de un recurso y calcular automáticamente la mora en días al momento de devolverlo.
**La decisión técnica:** Se utilizó la API `java.time` y `ChronoUnit.DAYS.between` para comparar la fecha actual con la fecha de vencimiento almacenada en el préstamo [cite: 200-201].
**El por qué:** `java.time` es la solución moderna y estándar de Java para el manejo de fechas por su inmutabilidad y precisión[cite: 200]. [cite_start]Esta lógica permite disparar el sistema de sanciones de forma inmediata si se detecta un retraso.

### Issue 8: Registro histórico de transacciones
**El desafío:** Mantener una bitácora de todas las operaciones realizadas (préstamos y devoluciones) para auditoría del sistema.
**La decisión técnica:** Se implementó una `List<String>` en la clase `Main` que actúa como acumulador de eventos de texto durante la sesión[cite: 28, 53, 55].
**El por qué:** Centralizar el historial en el orquestador permite registrar la actividad de distintos servicios de manera cronológica, facilitando su posterior visualización y persistencia en los archivos de datos [cite: 59-61].

---

## 8. Bonus (Opcional)

### Issue 9: Sistema de Sanciones por mora
**El desafío:** Aplicar una penalización automática a los socios que entregan recursos fuera de término, impidiéndoles realizar nuevos préstamos durante un tiempo determinado.
**La decisión técnica:** Se implementó un `Map<String, LocalDate>` dentro de `PrestamoService` para registrar la fecha de finalización de las suspensiones vinculadas al DNI del socio. [cite_start]Al gestionar una devolución tardía, se calcula el fin de la sanción sumando a la fecha actual el doble de los días de retraso.
**El por qué:** Centralizar las sanciones en un mapa permite verificar el estado del socio en tiempo real antes de registrar cualquier nuevo préstamo . [cite_start]Si el socio intenta retirar un libro antes de que expire su sanción, el sistema lanza una `BibliotecaException` bloqueando la operación.

### Issue 10: Persistencia de datos en archivos CSV
**El desafío:** Lograr que la información del sistema no se pierda al cerrar la aplicación y reconstruir las relaciones de memoria al iniciar.
**La decisión técnica:** Se desarrolló la clase `GestorArchivos` para manejar la lectura y escritura de cuatro archivos CSV diferenciados . [cite_start]Para los recursos, se implementó un sistema de etiquetas ("L" para Libro, "E" para Ebook) para instanciar el tipo correcto .
**El por qué:** El orden de carga es crítico: primero se cargan los catálogos base (socios y recursos) y finalmente los préstamos. [cite_start]Al cargar los préstamos, el sistema usa los identificadores guardados para buscar las referencias reales en los repositorios, garantizando la integridad de la memoria .