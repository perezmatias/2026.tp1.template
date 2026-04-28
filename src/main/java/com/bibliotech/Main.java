package com.bibliotech;

import com.bibliotech.exception.*;
import com.bibliotech.model.*;
import com.bibliotech.repository.*;
import com.bibliotech.service.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // Inicialización de componentes
        InMemoryRecursoRepository recursoRepo = new InMemoryRecursoRepository();
        InMemorySocioRepository socioRepo = new InMemorySocioRepository();
        InMemoryPrestamoRepository prestamoRepo = new InMemoryPrestamoRepository();

        PrestamoService prestamoService = new PrestamoService(recursoRepo, prestamoRepo);
        ValidadorSocio validador = new ValidadorSocio();

        List<String> historial = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        System.out.println("SISTEMA BIBLIOTECH - GESTIÓN INTEGRAL");

        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestión de Libros (Registrar / Buscar / Listar)");
            System.out.println("2. Gestión de Socios (Registrar)");
            System.out.println("3. Préstamos (Registrar / Devolver)");
            System.out.println("4. Ver Historial");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        System.out.println("1a. Registrar Libro | 1b. Buscar Libros | 1c. Ver todos los libros");
                        String subOpc1 = scanner.nextLine();
                        if (subOpc1.equals("1a")) {
                            System.out.print("ISBN: "); String isbn = scanner.nextLine();
                            System.out.print("Título: "); String titulo = scanner.nextLine();
                            System.out.print("Autor: "); String autor = scanner.nextLine();
                            System.out.print("Año: "); int anio = Integer.parseInt(scanner.nextLine());
                            System.out.print("Categoría: "); String cat = scanner.nextLine();
                            System.out.print("Páginas: "); int pag = Integer.parseInt(scanner.nextLine());
                            recursoRepo.guardar(new Libro(isbn, titulo, autor, anio, cat, pag));
                            System.out.println("Libro registrado.");
                        } else if (subOpc1.equals("1b")) {
                            System.out.print("Término de búsqueda (título/autor/categoría): ");
                            String t = scanner.nextLine();
                            recursoRepo.buscarAvanzada(t).forEach(System.out::println);
                        } else if (subOpc1.equals("1c")) {
                            System.out.println("\n--- Catálogo Completo ---");
                            List<Recurso> todosLosRecursos = recursoRepo.buscarTodos();
                            if (todosLosRecursos.isEmpty()) {
                                System.out.println("No hay libros registrados en el sistema.");
                            } else {
                                todosLosRecursos.forEach(r ->
                                        System.out.println("- [" + r.isbn() + "] " + r.titulo() + " | Autor: " + r.autor() + " | Categoría: " + r.categoria())
                                );
                            }
                        } else {
                            System.out.println("Opción no válida.");
                        }
                        break;

                    case "2":
                        System.out.print("DNI: "); String dni = scanner.nextLine();
                        validador.validarDni(dni);
                        System.out.print("Nombre: "); String nom = scanner.nextLine();
                        System.out.print("Email: "); String mail = scanner.nextLine();
                        validador.validarEmail(mail);
                        System.out.print("Tipo (E: Estudiante / D: Docente): ");
                        String tipo = scanner.nextLine();
                        Socio nuevoSocio = tipo.equalsIgnoreCase("E") ?
                                new Estudiante(dni, nom, mail) : new Docente(dni, nom, mail);
                        socioRepo.guardar(nuevoSocio);
                        System.out.println("Socio registrado con éxito.");
                        break;

                    case "3":
                        System.out.println("3a. Nuevo Préstamo | 3b. Devolución");
                        String subOpc3 = scanner.nextLine();
                        if (subOpc3.equals("3a")) {
                            System.out.print("DNI del Socio: "); String dniS = scanner.nextLine();
                            Socio s = socioRepo.buscarPorId(dniS).orElseThrow(() -> new BibliotecaException("Socio no encontrado."));
                            System.out.print("ISBN del Libro: "); String isbnL = scanner.nextLine();
                            prestamoService.registrarPrestamo(s, isbnL);
                            historial.add("PRÉSTAMO: " + s.nombre() + " llevó " + isbnL);
                            System.out.println("Préstamo exitoso.");
                        } else if (subOpc3.equals("3b")) {
                            System.out.print("ISBN a devolver: "); String isbnD = scanner.nextLine();
                            long retraso = prestamoService.gestionarDevolucion(isbnD);
                            historial.add("DEVOLUCIÓN: " + isbnD + " (Retraso: " + retraso + " días)");

                            // Sancion
                            if (retraso > 0) {
                                long diasSancion = retraso * 2;
                                System.out.println("Devolución procesada con " + retraso + " días de retraso.");
                                System.out.println("ATENCIÓN: El socio ha sido sancionado por " + diasSancion + " días.");
                            } else {
                                System.out.println("Devolución procesada en término. ¡Gracias!");
                            }
                        } else {
                            System.out.println("Opción no válida.");
                        }
                        break;

                    case "4":
                        System.out.println("\n--- Historial de Transacciones ---");
                        if (historial.isEmpty()) {
                            System.out.println("No hay transacciones registradas todavía.");
                        } else {
                            historial.forEach(System.out::println);
                        }
                        break;

                    case "5":
                        salir = true;
                        System.out.println("Cerrando el sistema... ¡Hasta luego!");
                        break;

                    default:
                        System.out.println("Opción no válida. Por favor, intentá de nuevo.");
                        break;
                }
            } catch (BibliotecaException e) {
                System.out.println("ERROR DE NEGOCIO: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }
}