package com.bibliotech.util;

import com.bibliotech.model.*;
import com.bibliotech.repository.*;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {
    private static final String ARCHIVO_LIBROS = "libros.csv";
    private static final String ARCHIVO_SOCIOS = "socios.csv";
    private static final String ARCHIVO_PRESTAMOS = "prestamos.csv";

    // --- LIBROS ---
    public static void guardarLibros(List<Recurso> recursos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_LIBROS))) {
            for (Recurso r : recursos) {
                if (r instanceof Libro l) {
                    writer.println(l.isbn() + "," + l.titulo() + "," + l.autor() + "," +
                            l.anio() + "," + l.categoria() + "," + l.paginas());
                }
            }
        } catch (IOException e) { System.out.println("Error al guardar libros: " + e.getMessage()); }
    }

    public static List<Recurso> cargarLibros() {
        List<Recurso> recursos = new ArrayList<>();
        File archivo = new File(ARCHIVO_LIBROS);
        if (!archivo.exists()) return recursos;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] d = linea.split(",");
                if (d.length == 6) {
                    recursos.add(new Libro(d[0], d[1], d[2], Integer.parseInt(d[3]), d[4], Integer.parseInt(d[5])));
                }
            }
        } catch (IOException | NumberFormatException e) { System.out.println("Error al cargar libros: " + e.getMessage()); }
        return recursos;
    }

    // --- SOCIOS ---
    public static void guardarSocios(List<Socio> socios) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_SOCIOS))) {
            for (Socio s : socios) {
                String tipo = (s instanceof Estudiante) ? "E" : "D";
                writer.println(s.dni() + "," + s.nombre() + "," + s.email() + "," + tipo);
            }
        } catch (IOException e) { System.out.println("Error al guardar socios: " + e.getMessage()); }
    }

    public static List<Socio> cargarSocios() {
        List<Socio> socios = new ArrayList<>();
        File archivo = new File(ARCHIVO_SOCIOS);
        if (!archivo.exists()) return socios;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] d = linea.split(",");
                if (d.length == 4) {
                    if (d[3].equals("E")) socios.add(new Estudiante(d[0], d[1], d[2]));
                    else socios.add(new Docente(d[0], d[1], d[2]));
                }
            }
        } catch (IOException e) { System.out.println("Error al cargar socios: " + e.getMessage()); }
        return socios;
    }

    // --- PRÉSTAMOS ---
    public static void guardarPrestamos(List<Prestamo> prestamos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_PRESTAMOS))) {
            for (Prestamo p : prestamos) {
                // Guardamos los IDs que nos permiten reconstruir la relación luego
                writer.println(p.id() + "," + p.socio().dni() + "," + p.recurso().isbn() + "," +
                        p.fechaPrestamo().toString() + "," + p.fechaVencimiento().toString());
            }
        } catch (IOException e) { System.out.println("Error al guardar préstamos: " + e.getMessage()); }
    }

    public static List<Prestamo> cargarPrestamos(InMemorySocioRepository socioRepo, InMemoryRecursoRepository recursoRepo) {
        List<Prestamo> prestamos = new ArrayList<>();
        File archivo = new File(ARCHIVO_PRESTAMOS);
        if (!archivo.exists()) return prestamos;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] d = linea.split(",");
                if (d.length == 5) {
                    // Reconstruimos la relación buscando en los repositorios
                    Socio socio = socioRepo.buscarPorId(d[1]).orElse(null);
                    Recurso recurso = recursoRepo.buscarPorId(d[2]).orElse(null);

                    if (socio != null && recurso != null) {
                        prestamos.add(new Prestamo(d[0], socio, recurso, LocalDate.parse(d[3]), LocalDate.parse(d[4])));
                    }
                }
            }
        } catch (IOException e) { System.out.println("Error al cargar préstamos: " + e.getMessage()); }
        return prestamos;
    }
}