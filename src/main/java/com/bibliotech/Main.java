package com.bibliotech;

import com.bibliotech.exception.*;
import com.bibliotech.model.*;
import com.bibliotech.repository.*;
import com.bibliotech.service.*;
import com.bibliotech.util.GestorArchivos;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Inicialización de Repositorios en Memoria
        InMemoryRecursoRepository recursoRepo = new InMemoryRecursoRepository();
        InMemorySocioRepository socioRepo = new InMemorySocioRepository();
        InMemoryPrestamoRepository prestamoRepo = new InMemoryPrestamoRepository();

        // 2. Inicialización de Servicios y Validadores
        PrestamoService prestamoService = new PrestamoService(recursoRepo, prestamoRepo);
        ValidadorSocio validador = new ValidadorSocio();

        List<String> historial = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        // 3. Carga de Persistencia
        System.out.println("Sincronizando base de datos local...");
        try {
            GestorArchivos.cargarLibros().forEach(recursoRepo::guardar);
            GestorArchivos.cargarSocios().forEach(socioRepo::guardar);

            GestorArchivos.cargarPrestamos(socioRepo, recursoRepo).forEach(prestamoRepo::guardar);
            System.out.println("Carga finalizada con éxito.");
        } catch (Exception e) {
            System.out.println("Aviso: No se pudieron cargar datos previos o los archivos no existen todavía.");
        }

        System.out.println("\n=====================================");
        System.out.println("   SISTEMA BIBLIOTECH - MENDOZA      ");
        System.out.println("=====================================");

        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestión de Libros (Registrar / Buscar / Listar)");
            System.out.println("2. Gestión de Socios (Registrar)");
            System.out.println("3. Préstamos (Registrar / Devolver)");
            System.out.println("4. Ver Historial de Transacciones");
            System.out.println("5. Salir y Guardar Cambios");
            System.out.print("Seleccione una opción: ");

            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        System.out.println("1a. Registrar Libro | 1b. Buscar Libros | 1c. Catálogo completo");
                        String subOpc1 = scanner.nextLine();
                        if (subOpc1.equalsIgnoreCase("1a")) {
                            System.out.print("ISBN: "); String isbn = scanner.nextLine();
                            System.out.print("Título: "); String titulo = scanner.nextLine();
                            System.out.print("Autor: "); String autor = scanner.nextLine();
                            System.out.print("Año: "); int anio = Integer.parseInt(scanner.nextLine());
                            System.out.print("Categoría: "); String cat = scanner.nextLine();
                            System.out.print("Páginas: "); int pag = Integer.parseInt(scanner.nextLine());
                            recursoRepo.guardar(new Libro(isbn, titulo, autor, anio, cat, pag));
                            System.out.println("Recurso registrado en el sistema.");
                        } else if (subOpc1.equalsIgnoreCase("1b")) {
                            System.out.print("Ingrese término (título/autor/categoría): ");
                            String t = scanner.nextLine();
                            recursoRepo.buscarAvanzada(t).forEach(System.out::println);
                        } else if (subOpc1.equalsIgnoreCase("1c")) {
                            System.out.println("\n--- Libros en Catálogo ---");
                            List<Recurso> todos = recursoRepo.buscarTodos();
                            if (todos.isEmpty()) System.out.println("Catálogo vacío.");
                            else todos.forEach(r -> System.out.println("- [" + r.isbn() + "] " + r.titulo() + " (" + r.autor() + ")"));
                        }
                        break;

                    case "2":
                        System.out.print("DNI: "); String dni = scanner.nextLine();
                        validador.validarDni(dni);
                        System.out.print("Nombre completo: "); String nom = scanner.nextLine();
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
                        if (subOpc3.equalsIgnoreCase("3a")) {
                            System.out.print("DNI del Socio: "); String dniS = scanner.nextLine();
                            Socio s = socioRepo.buscarPorId(dniS).orElseThrow(() -> new BibliotecaException("Socio no encontrado."));
                            System.out.print("ISBN del Recurso: "); String isbnL = scanner.nextLine();
                            prestamoService.registrarPrestamo(s, isbnL);
                            historial.add("PRÉSTAMO: Socio " + s.nombre() + " retiró ISBN " + isbnL);
                            System.out.println("Operación realizada con éxito.");
                        } else if (subOpc3.equalsIgnoreCase("3b")) {
                            System.out.print("ISBN a devolver: "); String isbnD = scanner.nextLine();
                            long retraso = prestamoService.gestionarDevolucion(isbnD);
                            historial.add("DEVOLUCIÓN: ISBN " + isbnD + " (Mora: " + retraso + " días)");

                            if (retraso > 0) {
                                System.out.println("Alerta: Devolución fuera de término (" + retraso + " días).");
                                System.out.println("El socio ha sido suspendido por " + (retraso * 2) + " días.");
                            } else {
                                System.out.println("Devolución aceptada en término.");
                            }
                        }
                        break;

                    case "4":
                        System.out.println("\n--- Historial de Transacciones ---");
                        if (historial.isEmpty()) System.out.println("No hay movimientos en la sesión actual.");
                        else historial.forEach(System.out::println);
                        break;

                    case "5":
                        System.out.println("Sincronizando cambios con los archivos CSV...");
                        GestorArchivos.guardarLibros(recursoRepo.buscarTodos());
                        GestorArchivos.guardarSocios(socioRepo.buscarTodos());
                        GestorArchivos.guardarPrestamos(prestamoRepo.buscarTodos());
                        salir = true;
                        System.out.println("Base de datos actualizada. ¡Hasta luego!");
                        break;

                    default:
                        System.out.println("Opción no válida.");
                        break;
                }
            } catch (BibliotecaException e) {
                System.out.println("ERROR DE NEGOCIO: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR DEL SISTEMA: " + e.getMessage());
            }
        }
        scanner.close();
    }
}