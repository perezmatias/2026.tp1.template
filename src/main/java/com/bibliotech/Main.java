package com.bibliotech;

import com.bibliotech.exception.BibliotecaException;
import com.bibliotech.model.*;
import com.bibliotech.repository.InMemoryPrestamoRepository;
import com.bibliotech.repository.InMemoryRecursoRepository;
import com.bibliotech.service.PrestamoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 1. Instanciar repositorios y servicios (Inyección de dependencias manual)
        InMemoryRecursoRepository recursoRepo = new InMemoryRecursoRepository();
        InMemoryPrestamoRepository prestamoRepo = new InMemoryPrestamoRepository();
        PrestamoService prestamoService = new PrestamoService(recursoRepo, prestamoRepo);

        // 2. Lista para mantener el historial de transacciones
        List<String> historial = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        boolean salir = false;

        // 3. Cargar datos de prueba
        recursoRepo.guardar(new Libro("123", "Sistemas Operativos Modernos", "Tanenbaum", 2008, "Tecnología", 800));
        Socio estudianteActivo = new Estudiante("11223344", "Matias", "matias@mail.com");

        System.out.println("=====================================");
        System.out.println("   BIENVENIDO AL SISTEMA BIBLIOTECH  ");
        System.out.println("=====================================");

        // 4. Bucle principal del CLI
        while (!salir) {
            System.out.println("\nMenú de Opciones:");
            System.out.println("1. Ver recursos disponibles");
            System.out.println("2. Registrar préstamo");
            System.out.println("3. Registrar devolución");
            System.out.println("4. Ver historial de transacciones");
            System.out.println("5. Salir");
            System.out.print("Elegí una opción: ");

            String opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1":
                        System.out.println("\n--- Recursos en el sistema ---");
                        recursoRepo.buscarTodos().forEach(r ->
                                System.out.println("- " + r.titulo() + " | Autor: " + r.autor() + " | ISBN: " + r.isbn())
                        );
                        break;

                    case "2":
                        System.out.print("Ingresá el ISBN del recurso a solicitar: ");
                        String isbnPrestamo = scanner.nextLine();
                        // Ejecutamos la lógica de negocio
                        Prestamo p = prestamoService.registrarPrestamo(estudianteActivo, isbnPrestamo);
                        // Guardamos en el historial
                        historial.add("PRÉSTAMO: El socio " + estudianteActivo.nombre() + " retiró el recurso [" + isbnPrestamo + "]");
                        System.out.println("¡Préstamo registrado con éxito!");
                        break;

                    case "3":
                        System.out.print("Ingresá el ISBN del recurso a devolver: ");
                        String isbnDevolucion = scanner.nextLine();
                        // Ejecutamos la devolución
                        long diasRetraso = prestamoService.gestionarDevolucion(isbnDevolucion);
                        // Guardamos en el historial
                        String msjDevolucion = "DEVOLUCIÓN: Recurso [" + isbnDevolucion + "] devuelto. Días de retraso: " + diasRetraso;
                        historial.add(msjDevolucion);
                        System.out.println(msjDevolucion);
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
                }
            } catch (BibliotecaException e) {
                System.out.println("ERROR DE NEGOCIO: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR INESPERADO: " + e.getMessage());
            }
        }

        scanner.close();
    }
}