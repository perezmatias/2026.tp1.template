package com.bibliotech.util;

import com.bibliotech.model.Libro;
import com.bibliotech.model.Recurso;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {
    private static final String ARCHIVO_LIBROS = "libros.csv";

    // Método para guardar la lista de recursos en el CSV
    public static void guardarLibros(List<Recurso> recursos) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_LIBROS))) {
            for (Recurso r : recursos) {
                if (r instanceof Libro l) {
                    // Guardamos los datos separados por coma
                    writer.println(l.isbn() + "," + l.titulo() + "," + l.autor() + "," +
                            l.anio() + "," + l.categoria() + "," + l.paginas());
                }
            }
        } catch (IOException e) {
            System.out.println("Error al guardar los libros: " + e.getMessage());
        }
    }

    // Método para leer el CSV y devolver la lista de recursos
    public static List<Recurso> cargarLibros() {
        List<Recurso> recursos = new ArrayList<>();
        File archivo = new File(ARCHIVO_LIBROS);

        if (!archivo.exists()) return recursos;

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length == 6) {
                    recursos.add(new Libro(datos[0], datos[1], datos[2],
                            Integer.parseInt(datos[3]), datos[4], Integer.parseInt(datos[5])));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar los libros: " + e.getMessage());
        }
        return recursos;
    }
}