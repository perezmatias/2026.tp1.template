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
        InMemoryRecursoRepository recursoRepo = new InMemoryRecursoRepository();
        InMemorySocioRepository socioRepo = new InMemorySocioRepository();
        InMemoryPrestamoRepository prestamoRepo = new InMemoryPrestamoRepository();

        PrestamoService prestamoService = new PrestamoService(recursoRepo, prestamoRepo);
        ValidadorSocio validador = new ValidadorSocio();
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        System.out.println("Sincronizando base de datos local...");
        try {
            GestorArchivos.cargarLibros().forEach(recursoRepo::guardar);
            GestorArchivos.cargarSocios().forEach(socioRepo::guardar);
            GestorArchivos.cargarPrestamos(socioRepo, recursoRepo).forEach(prestamoRepo::guardar);
            System.out.println("Carga de entidades finalizada.");
        } catch (Exception e) { System.out.println("Aviso: No se pudieron cargar datos previos."); }

        List<String> historial = GestorArchivos.cargarHistorial();

        System.out.println("\n=====================================");
        System.out.println("   SISTEMA BIBLIOTECH - MENDOZA      ");
        System.out.println("=====================================");

        while (!salir) {
            System.out.println("\n--- MENÚ PRINCIPAL ---");
            System.out.println("1. Gestión de Libros/E-books");
            System.out.println("2. Gestión de Socios");
            System.out.println("3. Préstamos");
            System.out.println("4. Ver Historial de Transacciones");
            System.out.println("5. Salir y Guardar Cambios");
            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        System.out.println("1a. Registrar | 1b. Buscar | 1c. Catálogo completo");
                        String subOpc1 = scanner.nextLine();
                        if (subOpc1.equalsIgnoreCase("1a")) {
                            System.out.print("ISBN: "); String isbn = scanner.nextLine();
                            System.out.print("Título: "); String titulo = scanner.nextLine();
                            System.out.print("Autor: "); String autor = scanner.nextLine();
                            System.out.print("Año: "); int anio = Integer.parseInt(scanner.nextLine());
                            System.out.print("Categoría: "); String cat = scanner.nextLine();
                            System.out.print("Tipo (L: Libro / E: E-book): "); String t = scanner.nextLine();
                            if (t.equalsIgnoreCase("E")) {
                                System.out.print("Formato: "); String f = scanner.nextLine();
                                System.out.print("Tamaño MB: "); double mb = Double.parseDouble(scanner.nextLine());
                                recursoRepo.guardar(new Ebook(isbn, titulo, autor, anio, cat, mb, f));
                            } else {
                                System.out.print("Páginas: "); int p = Integer.parseInt(scanner.nextLine());
                                recursoRepo.guardar(new Libro(isbn, titulo, autor, anio, cat, p));
                            }
                            System.out.println("Recurso registrado.");
                        } else if (subOpc1.equalsIgnoreCase("1b")) {
                            System.out.print("Término: "); String t = scanner.nextLine();
                            recursoRepo.buscarAvanzada(t).forEach(System.out::println);
                        } else if (subOpc1.equalsIgnoreCase("1c")) {
                            recursoRepo.buscarTodos().forEach(r -> System.out.println("- [" + r.isbn() + "] " + r.titulo() + " (" + r.getClass().getSimpleName() + ")"));
                        }
                        break;
                    case "2":
                        System.out.println("2a. Registrar | 2b. Ver Socios");
                        String subOpc2 = scanner.nextLine();
                        if (subOpc2.equalsIgnoreCase("2a")) {
                            System.out.print("DNI: "); String dni = scanner.nextLine();
                            validador.validarDni(dni);
                            System.out.print("Nombre: "); String n = scanner.nextLine();
                            System.out.print("Email: "); String e = scanner.nextLine();
                            validador.validarEmail(e);
                            System.out.print("Tipo (E/D): "); String tipo = scanner.nextLine();
                            socioRepo.guardar(tipo.equalsIgnoreCase("E") ? new Estudiante(dni, n, e) : new Docente(dni, n, e));
                            System.out.println("Socio registrado.");
                        } else if (subOpc2.equalsIgnoreCase("2b")) {
                            socioRepo.buscarTodos().forEach(s -> System.out.println("- DNI: " + s.dni() + " | " + s.nombre()));
                        }
                        break;
                    case "3":
                        System.out.println("3a. Nuevo | 3b. Devolución | 3c. Listar Activos");
                        String subOpc3 = scanner.nextLine();
                        if (subOpc3.equalsIgnoreCase("3a")) {
                            System.out.print("DNI: "); String dS = scanner.nextLine();
                            Socio s = socioRepo.buscarPorId(dS).orElseThrow(() -> new BibliotecaException("No existe."));
                            System.out.print("ISBN: "); String iL = scanner.nextLine();
                            prestamoService.registrarPrestamo(s, iL);
                            historial.add("PRÉSTAMO: " + s.nombre() + " retiró ISBN " + iL);
                            System.out.println("Éxito.");
                        } else if (subOpc3.equalsIgnoreCase("3b")) {
                            System.out.print("ISBN: "); String iD = scanner.nextLine();
                            long ret = prestamoService.gestionarDevolucion(iD);
                            historial.add("DEVOLUCIÓN: " + iD + " (Mora: " + ret + ")");
                            if (ret > 0) System.out.println("Sanción: " + (ret*2) + " días.");
                            else System.out.println("En término.");
                        } else if (subOpc3.equalsIgnoreCase("3c")) {
                            prestamoRepo.buscarTodos().forEach(p -> System.out.println("- " + p.socio().nombre() + " tiene " + p.recurso().titulo()));
                        }
                        break;
                    case "4":
                        historial.forEach(System.out::println);
                        break;
                    case "5":
                        GestorArchivos.guardarLibros(recursoRepo.buscarTodos());
                        GestorArchivos.guardarSocios(socioRepo.buscarTodos());
                        GestorArchivos.guardarPrestamos(prestamoRepo.buscarTodos());
                        GestorArchivos.guardarHistorial(historial);
                        salir = true;
                        System.out.println("¡Hasta luego!");
                        break;
                }
            } catch (BibliotecaException e) { System.out.println("ERROR: " + e.getMessage()); }
            catch (Exception e) { System.out.println("SISTEMA: " + e.getMessage()); }
        }
        scanner.close();
    }
}